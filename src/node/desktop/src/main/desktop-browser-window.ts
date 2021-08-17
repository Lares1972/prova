/*
 * desktop-browser-window.ts
 *
 * Copyright (C) 2021 by RStudio, PBC
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

import { BrowserWindow, shell, WebContents } from 'electron';
import { IpcMainEvent } from 'electron/main';

import fs from 'fs';
import os from 'os';
import path from 'path';

import { EventEmitter } from 'stream';
import { URL } from 'url';
import { logger } from '../core/logger';
import { appState } from './app-state';
import { showContextMenu } from './context-menu';
import { executeJavaScript, isSafeHost } from './utils';

/**
 * Base class for browser-based windows. Subclasses include GwtWindow, SecondaryWindow,
 * SatelliteWindow, and MainWindow.
 * 
 * Porting note: This corresponds to a combination of the QMainWindow/BrowserWindow and 
 * QWebEngineView/WebView in the Qt desktop app.
 */
export class DesktopBrowserWindow extends EventEmitter {
  static WINDOW_DESTROYED = 'desktop-browser-window_destroyed';
  static CLOSE_WINDOW_SHORTCUT = 'desktop-browser-close_window_shortcut';

  window: BrowserWindow;
  private tempDirs: string[] = [];

  // if loading fails and emits `did-fail-load` it will be followed by a 
  // 'did-finish-load'; use this bool to differentiate
  private failLoad = false;

  constructor(
    private showToolbar: boolean,
    private adjustTitle: boolean,
    protected name: string,
    readonly baseUrl?: string,
    private parent?: DesktopBrowserWindow,
    private opener?: WebContents,
    private allowExternalNavigate = false,
    addApiKeys: string[] = [],
    existingWindow?: BrowserWindow
  ) {
    super();
    const apiKeys = [['desktopInfo', ...addApiKeys].join('|')];
    if (existingWindow) {
      this.window = existingWindow;
    } else {
      this.window = new BrowserWindow({
        // https://github.com/electron/electron/blob/master/docs/faq.md#the-font-looks-blurry-what-is-this-and-what-can-i-do
        backgroundColor: '#fff',
        webPreferences: {
          enableRemoteModule: false,
          nodeIntegration: false,
          contextIsolation: true,
          sandbox: false,
          nativeWindowOpen: true,
          additionalArguments: apiKeys,
          preload: path.join(__dirname, '../renderer/preload.js'),
        },
        show: false
      });

      // register context menu (right click) handler
      this.window.webContents.on('context-menu', (event, params) => {
        showContextMenu(event as IpcMainEvent, params);
      });

      // Uncomment to have all windows show dev tools by default
      // this.window.webContents.openDevTools();

    }

    this.window.webContents.on('before-input-event', (event, input) => {
      this.keyPressEvent(event, input);
    });

    this.window.webContents.setWindowOpenHandler((details) => {
      // check if this is target="_blank" from an IDE window
      if (this.baseUrl && (details.disposition === 'foreground-tab' || details.disposition === 'background-tab')) {
        // TODO: validation/restrictions on the URLs?
        void shell.openExternal(details.url);
        return { action: 'deny' };
      }

      // configure window creation; we'll associate the resulting BrowserWindow with our 
      // window wrapper type via 'did-create-window' below
      return appState().windowOpening();
    });

    this.window.webContents.on('did-create-window', (newWindow) => {
      appState().windowCreated(newWindow, this.window.webContents, this.baseUrl);
    });

    this.window.webContents.on('will-navigate', (event, url) => {

      // TODO: this is a partial implementation of DesktopWebPage.cpp::acceptNavigationRequest;
      // all the other details need to be implemented

      let targetUrl: URL;
      try {
        targetUrl = new URL(url);
      } catch (err) {
        // malformed URL will cause exception
        logger().logError(err);
        event.preventDefault();
        return;
      }

      // determine if this is a local request (handle internally only if local)
      const host = targetUrl.hostname;
      const isLocal = host === 'localhost' || host == '127.0.0.1' || host == '::1';
      if (isLocal) {
        return;
      }

      if (!this.allowExternalNavigate) {
        try {
          const targetUrl: URL = new URL(url);
          if (!isSafeHost(targetUrl.host)) {
            // when not allowing external navigation, open an external browser
            // to view the URL
            event.preventDefault();
            void shell.openExternal(url);
          }
        } catch (err) {
          // malformed URL will cause exception
          logger().logError(err);
          event.preventDefault();
        }
      }
    });

    this.window.webContents.on('page-title-updated', (event, title, explicitSet) => {
      this.adjustWindowTitle(title, explicitSet);
    });
    this.window.webContents.on('did-finish-load', () => {
      if (!this.failLoad) {
        this.finishLoading(true);
      } else {
        this.failLoad = false;
      }
    });
    this.window.webContents.on('did-fail-load', () => {
      this.failLoad = true;
      this.finishLoading(false);
    });
    this.window.on('close', (event: Electron.Event) => {
      this.closeEvent(event);
    });
    this.window.on('closed', () => {
      this.emit(DesktopBrowserWindow.WINDOW_DESTROYED);
    });

    // set zoom factor
    // TODO: double zoomLevel = options().zoomLevel();
    const zoomLevel = 1.0;
    this.window.webContents.setZoomFactor(zoomLevel);

    if (this.showToolbar) {
      logger().logDebug('toolbar NYI');
      // TODO: add another BrowserView to hold an HTML-based toolbar?
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  closeEvent(event: Electron.Event): void {
    if (!this.opener) {
      // if we don't know where we were opened from, check window.opener
      // (note that this could also be empty)
      const cmd =
        `if (window.opener && window.opener.unregisterDesktopChildWindow)
           window.opener.unregisterDesktopChildWindow('${this.name}');`;
      this.executeJavaScript(cmd).catch((error) => {
        logger().logError(error);
      });
    } else {
      // if we do know where we were opened from and it has the appropriate
      // handlers, let it know we're closing
      const cmd =
        `if (window.unregisterDesktopChildWindow)
           window.unregisterDesktopChildWindow('${this.name}');`;
      this.executeJavaScript(cmd).catch((error) => {
        logger().logError(error);
      });
    }
  }

  adjustWindowTitle(title: string, explicitSet: boolean): void {
    if (this.adjustTitle && explicitSet) {
      this.window.setTitle(title);
    }
  }

  syncWindowTitle(): void {
    if (this.adjustTitle) {
      this.window.setTitle(this.window.webContents.getTitle());
    }
  }

  finishLoading(succeeded: boolean): void {
    if (succeeded) {
      this.syncWindowTitle();

      // TODO: Qt version sets up a tiny resize of the window here in response to the
      // window being shown on a different screen. Need to test if this is necessary.

      const cmd =
        `if (window.opener && window.opener.registerDesktopChildWindow)
         window.opener.registerDesktopChildWindow('${this.name}', window);`;
      this.executeJavaScript(cmd).catch((error) => {
        logger().logError(error);
      });
      this.cleanupTmpFiles(false);
    }
  }

  avoidMoveCursorIfNecessary(): void {
    if (process.platform === 'darwin') {
      this.executeJavaScript('document.body.className = document.body.className + \' avoid-move-cursor\'')
        .catch((error) => {
          logger().logError(error);
        });
    }
  }

  /**
   * Execute javascript in this window's page
   * 
   * @param cmd javascript to execute in this window
   * @returns promise with result of execution
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async executeJavaScript(cmd: string): Promise<any> {
    return executeJavaScript(this.window.webContents, cmd);
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  setViewerUrl(url: string): void {
    // TODO: in the Qt version this is implemented in webPage()
  }

  loadHtml(html: string): void {
    const prefix = path.join(os.tmpdir(), 'rstudioTmpPage');
    const uniqueDir = fs.mkdtempSync(prefix);
    const uniqueFile = path.join(uniqueDir, 'tmp.html');
    fs.writeFileSync(uniqueFile, html);
    this.window.loadFile(uniqueFile).catch(logger().logError);

    // Track the temporary directory for later cleanup; can't delete
    // while it is loaded
    this.tempDirs.push(uniqueDir);
  }

  /**
   * Cleanup pages written to temp dir by loadHtml method.
   * 
   * @param keepLast don't delete most recently loaded tmpfile (can't delete the currently
   * loaded page)
   */
  cleanupTmpFiles(keepLast: boolean): void {
    let i = keepLast ? this.tempDirs.length - 2 : this.tempDirs.length - 1;
    while ( i >= 0) {
      try {
        fs.rmSync(this.tempDirs[i], { force: true, recursive: true });
        this.tempDirs.pop();
      } catch (err) {
        logger().logDebug(`Failed to delete ${this.tempDirs[i]}`);
      }
      i--;
    }
  }

  keyPressEvent(event: Electron.Event, input: Electron.Input): void {
    if (process.platform === 'darwin') {
      if (input.meta && input.key.toLowerCase() === 'w') {
        // on macOS, intercept Cmd+W and emit the window close signal
        this.emit(DesktopBrowserWindow.CLOSE_WINDOW_SHORTCUT);
      }
    }
  }
}