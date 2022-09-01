/*
 * RStudioGinjector.java
 *
 * Copyright (C) 2022 by Posit Software, PBC
 *
 * Unless you have received this program directly from Posit Software pursuant
 * to the terms of a commercial license agreement with Posit Software, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

import org.rstudio.core.client.HtmlMessageListener;
import org.rstudio.core.client.VirtualConsole;
import org.rstudio.core.client.VirtualConsoleFactory;
import org.rstudio.core.client.command.AddinCommandBinding;
import org.rstudio.core.client.command.ApplicationCommandManager;
import org.rstudio.core.client.command.EditorCommandManager;
import org.rstudio.core.client.command.ShortcutManager;
import org.rstudio.core.client.command.ShortcutViewer;
import org.rstudio.core.client.command.UserCommandManager;
import org.rstudio.core.client.files.filedialog.OpenProjectDialog;
import org.rstudio.core.client.files.filedialog.PathBreadcrumbWidget;
import org.rstudio.core.client.theme.WindowFrame;
import org.rstudio.core.client.widget.CaptionWithHelp;
import org.rstudio.core.client.widget.LocalRepositoriesWidget;
import org.rstudio.core.client.widget.ModifyKeyboardShortcutsWidget;
import org.rstudio.core.client.widget.RStudioThemedFrame;
import org.rstudio.core.client.widget.ToolbarPopupMenu;
import org.rstudio.studio.client.application.Application;
import org.rstudio.studio.client.application.ApplicationInterrupt;
import org.rstudio.studio.client.application.AriaLiveService;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.application.ui.AboutDialog;
import org.rstudio.studio.client.application.ui.ProjectPopupMenu;
import org.rstudio.studio.client.application.ui.addins.AddinsToolbarButton;
import org.rstudio.studio.client.application.ui.impl.DesktopApplicationHeader;
import org.rstudio.studio.client.application.ui.impl.WebApplicationHeader;
import org.rstudio.studio.client.application.ui.impl.WebApplicationHeaderOverlay;
import org.rstudio.studio.client.common.FileDialogs;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.common.dependencies.DependencyManager;
import org.rstudio.studio.client.common.fileexport.FileExport;
import org.rstudio.studio.client.common.filetypes.FileTypeRegistry;
import org.rstudio.studio.client.common.filetypes.NewFileMenu;
import org.rstudio.studio.client.common.impl.DesktopFileDialogs;
import org.rstudio.studio.client.common.latex.LatexProgramRegistry;
import org.rstudio.studio.client.common.r.roxygen.RoxygenHelper;
import org.rstudio.studio.client.common.repos.SecondaryReposDialog;
import org.rstudio.studio.client.common.repos.SecondaryReposWidget;
import org.rstudio.studio.client.common.rnw.RnwWeaveRegistry;
import org.rstudio.studio.client.common.rnw.RnwWeaveSelectWidget;
import org.rstudio.studio.client.common.rpubs.ui.RPubsUploadDialog;
import org.rstudio.studio.client.common.rstudioapi.RStudioAPI;
import org.rstudio.studio.client.common.satellite.Satellite;
import org.rstudio.studio.client.common.satellite.SatelliteManager;
import org.rstudio.studio.client.common.spelling.RealtimeSpellChecker;
import org.rstudio.studio.client.common.spelling.ui.SpellingCustomDictionariesWidget;
import org.rstudio.studio.client.htmlpreview.HTMLPreviewApplication;
import org.rstudio.studio.client.notebook.CompileNotebookOptionsDialog;
import org.rstudio.studio.client.panmirror.PanmirrorWidget;
import org.rstudio.studio.client.panmirror.dialogs.PanmirrorDialogs;
import org.rstudio.studio.client.panmirror.dialogs.PanmirrorEditImageDialog;
import org.rstudio.studio.client.panmirror.dialogs.PanmirrorEditRawDialog;
import org.rstudio.studio.client.panmirror.dialogs.PanmirrorInsertCiteDialog;
import org.rstudio.studio.client.panmirror.outline.PanmirrorOutlineWidget;
import org.rstudio.studio.client.panmirror.pandoc.PanmirrorPandocServer;
import org.rstudio.studio.client.panmirror.server.PanmirrorCrossrefServer;
import org.rstudio.studio.client.panmirror.server.PanmirrorDOIServer;
import org.rstudio.studio.client.panmirror.server.PanmirrorDataCiteServer;
import org.rstudio.studio.client.panmirror.server.PanmirrorEnvironmentServer;
import org.rstudio.studio.client.panmirror.server.PanmirrorPubMedServer;
import org.rstudio.studio.client.panmirror.server.PanmirrorXRefServer;
import org.rstudio.studio.client.panmirror.server.PanmirrorZoteroServer;
import org.rstudio.studio.client.panmirror.ui.PanmirrorUIDisplay;
import org.rstudio.studio.client.panmirror.ui.PanmirrorUIPrefs;
import org.rstudio.studio.client.plumber.PlumberAPI;
import org.rstudio.studio.client.plumber.PlumberAPISatellite;
import org.rstudio.studio.client.plumber.ui.PlumberViewerTypePopupMenu;
import org.rstudio.studio.client.projects.model.ProjectTemplateRegistryProvider;
import org.rstudio.studio.client.projects.ui.newproject.CodeFilesList;
import org.rstudio.studio.client.projects.ui.newproject.NewDirectoryPage;
import org.rstudio.studio.client.projects.ui.newproject.NewPackagePage;
import org.rstudio.studio.client.projects.ui.newproject.NewQuartoProjectPage;
import org.rstudio.studio.client.projects.ui.prefs.ProjectPreferencesPane;
import org.rstudio.studio.client.projects.ui.prefs.buildtools.BuildToolsPackagePanel;
import org.rstudio.studio.client.quarto.ui.NewQuartoDocumentDialog;
import org.rstudio.studio.client.rmarkdown.RmdOutputSatellite;
import org.rstudio.studio.client.rmarkdown.ui.RmdOutputFramePane;
import org.rstudio.studio.client.rmarkdown.ui.RmdOutputFrameSatellite;
import org.rstudio.studio.client.rsconnect.ui.RSConnectDeploy;
import org.rstudio.studio.client.rsconnect.ui.RSConnectPublishButton;
import org.rstudio.studio.client.server.Server;
import org.rstudio.studio.client.shiny.ShinyApplication;
import org.rstudio.studio.client.shiny.ShinyApplicationSatellite;
import org.rstudio.studio.client.shiny.ui.ShinyGadgetDialog;
import org.rstudio.studio.client.shiny.ui.ShinyViewerTypePopupMenu;
import org.rstudio.studio.client.shiny.ui.ShinyTestPopupMenu;
import org.rstudio.studio.client.vcs.VCSApplication;
import org.rstudio.studio.client.workbench.BrowseAddinsDialog;
import org.rstudio.studio.client.workbench.addins.Addins.AddinExecutor;
import org.rstudio.studio.client.workbench.addins.AddinsCommandManager;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.model.RemoteFileSystemContext;
import org.rstudio.studio.client.workbench.model.Session;
import org.rstudio.studio.client.workbench.model.SessionOpener;
import org.rstudio.studio.client.workbench.prefs.model.UserPrefs;
import org.rstudio.studio.client.workbench.prefs.model.UserState;
import org.rstudio.studio.client.workbench.prefs.views.PythonPreferencesPaneBase;
import org.rstudio.studio.client.workbench.snippets.SnippetHelper;
import org.rstudio.studio.client.workbench.snippets.ui.EditSnippetsDialog;
import org.rstudio.studio.client.workbench.ui.ConsoleTabPanel;
import org.rstudio.studio.client.workbench.ui.polyfill.FocusVisiblePolyfill;
import org.rstudio.studio.client.workbench.views.connections.ui.ConnectionCodePanel;
import org.rstudio.studio.client.workbench.views.connections.ui.ConnectionExplorer;
import org.rstudio.studio.client.workbench.views.connections.ui.NewConnectionInstallOdbcHost;
import org.rstudio.studio.client.workbench.views.connections.ui.NewConnectionInstallPackagePage;
import org.rstudio.studio.client.workbench.views.connections.ui.NewConnectionPreInstallOdbcHost;
import org.rstudio.studio.client.workbench.views.connections.ui.NewConnectionShinyHost;
import org.rstudio.studio.client.workbench.views.connections.ui.NewConnectionSnippetDialog;
import org.rstudio.studio.client.workbench.views.connections.ui.NewConnectionSnippetHost;
import org.rstudio.studio.client.workbench.views.connections.ui.NewConnectionWizard;
import org.rstudio.studio.client.workbench.views.connections.ui.ObjectBrowser;
import org.rstudio.studio.client.workbench.views.connections.ui.ObjectBrowserModel;
import org.rstudio.studio.client.workbench.views.console.ConsoleInterpreterVersion;
import org.rstudio.studio.client.workbench.views.console.shell.assist.CompletionManagerBase;
import org.rstudio.studio.client.workbench.views.console.shell.assist.CompletionRequester;
import org.rstudio.studio.client.workbench.views.console.shell.assist.HelpStrategy;
import org.rstudio.studio.client.workbench.views.console.shell.assist.PythonCompletionManager;
import org.rstudio.studio.client.workbench.views.console.shell.assist.RCompletionManager;
import org.rstudio.studio.client.workbench.views.jobs.events.JobsPresenterEventHandlersImpl;
import org.rstudio.studio.client.workbench.views.jobs.model.JobManager;
import org.rstudio.studio.client.workbench.views.jobs.view.JobItemFactory;
import org.rstudio.studio.client.workbench.views.jobs.view.JobsDisplayImpl;
import org.rstudio.studio.client.workbench.views.output.lint.LintManager;
import org.rstudio.studio.client.workbench.views.packages.ui.CheckForUpdatesDialog;
import org.rstudio.studio.client.workbench.views.source.DocsMenu;
import org.rstudio.studio.client.workbench.views.source.DocumentOutlineWidget;
import org.rstudio.studio.client.workbench.views.source.NewPlumberAPI;
import org.rstudio.studio.client.workbench.views.source.NewShinyWebApplication;
import org.rstudio.studio.client.workbench.views.source.SourceColumn;
import org.rstudio.studio.client.workbench.views.source.SourceColumnManager;
import org.rstudio.studio.client.workbench.views.source.SourceSatellite;
import org.rstudio.studio.client.workbench.views.source.SourceWindow;
import org.rstudio.studio.client.workbench.views.source.SourceWindowManager;
import org.rstudio.studio.client.workbench.views.source.editors.EditingTargetCodeExecution;
import org.rstudio.studio.client.workbench.views.source.editors.EditingTargetInlineChunkExecution;
import org.rstudio.studio.client.workbench.views.source.editors.explorer.view.ObjectExplorerDataGrid;
import org.rstudio.studio.client.workbench.views.source.editors.explorer.view.ObjectExplorerEditingTargetWidget;
import org.rstudio.studio.client.workbench.views.source.editors.text.AceEditor;
import org.rstudio.studio.client.workbench.views.source.editors.text.AceEditorIdleCommands;
import org.rstudio.studio.client.workbench.views.source.editors.text.AceEditorMixins;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetCommentHeaderHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetIdleMonitor;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetJSHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetRHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetPackageDependencyHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetSqlHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.AceEditorWidget;
import org.rstudio.studio.client.workbench.views.source.editors.text.ChunkSatellite;
import org.rstudio.studio.client.workbench.views.source.editors.text.ChunkWindowManager;
import org.rstudio.studio.client.workbench.views.source.editors.text.ScopeTreeManager;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetChunks;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetCompilePdfHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetCppHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetPresentationHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetQuartoHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.TextEditingTargetRMarkdownHelper;
import org.rstudio.studio.client.workbench.views.source.editors.text.ace.AceBackgroundHighlighter;
import org.rstudio.studio.client.workbench.views.source.editors.text.ace.AceEditorBackgroundLinkHighlighter;
import org.rstudio.studio.client.workbench.views.source.editors.text.ace.AceEditorCoverageHighlighter;
import org.rstudio.studio.client.workbench.views.source.editors.text.cpp.CppCompletionManager;
import org.rstudio.studio.client.workbench.views.source.editors.text.cpp.CppCompletionRequest;
import org.rstudio.studio.client.workbench.views.source.model.CppCompletion;
import org.rstudio.studio.client.workbench.views.terminal.TerminalInfoDialog;
import org.rstudio.studio.client.workbench.views.terminal.TerminalList;
import org.rstudio.studio.client.workbench.views.terminal.TerminalPopupMenu;
import org.rstudio.studio.client.workbench.views.terminal.TerminalSession;
import org.rstudio.studio.client.workbench.views.source.editors.text.r.SignatureToolTipManager;
import org.rstudio.studio.client.workbench.views.source.editors.text.rmd.TextEditingTargetNotebook;
import org.rstudio.studio.client.workbench.views.source.editors.text.rmd.display.ChunkOptionsPopupPanel;
import org.rstudio.studio.client.workbench.views.source.editors.text.rmd.display.SetupChunkOptionsPopupPanel;
import org.rstudio.studio.client.workbench.views.source.editors.text.themes.AceThemes;
import org.rstudio.studio.client.workbench.views.source.editors.text.visualmode.VisualMode;
import org.rstudio.studio.client.workbench.views.source.editors.text.visualmode.VisualModePanmirrorContext;
import org.rstudio.studio.client.workbench.views.source.editors.text.visualmode.VisualModeConfirm;
import org.rstudio.studio.client.workbench.views.source.editors.text.visualmode.VisualModePanmirrorFormat;
import org.rstudio.studio.client.workbench.views.source.editors.text.visualmode.VisualModeSpelling;
import org.rstudio.studio.client.workbench.views.source.editors.text.yaml.YamlEditorToolsProviderQuarto;
import org.rstudio.studio.client.workbench.views.source.editors.text.visualmode.VisualModeMarkdownWriter;
import org.rstudio.studio.client.workbench.views.source.editors.text.visualmode.VisualModeNavigation;
import org.rstudio.studio.client.workbench.views.vcs.svn.SVNCommandHandler;
import org.rstudio.studio.client.workbench.views.viewer.quarto.QuartoConnection;
import org.rstudio.studio.client.workbench.views.environment.ClearAllDialog;
import org.rstudio.studio.client.workbench.views.environment.dataimport.DataImport;
import org.rstudio.studio.client.workbench.views.environment.dataimport.DataImportDialog;
import org.rstudio.studio.client.workbench.views.environment.dataimport.DataImportFileChooser;
import org.rstudio.studio.client.workbench.views.environment.dataimport.DataImportOptionsUiCsv;
import org.rstudio.studio.client.workbench.views.environment.dataimport.DataImportOptionsUiCsvLocale;

@GinModules(RStudioGinModuleOverlay.class)
public interface RStudioGinjector extends Ginjector
{
   void injectMembers(NewFileMenu newFileMenu);
   void injectMembers(NewDirectoryPage newDirectoryPage);
   void injectMembers(DocsMenu docsMenu);
   void injectMembers(DesktopApplicationHeader desktopApplicationHeader);
   void injectMembers(WebApplicationHeader webApplicationHeader);
   void injectMembers(AceEditor aceEditor);
   void injectMembers(DesktopFileDialogs desktopFileDialogs);
   void injectMembers(CompletionManagerBase completionManagerBase);
   void injectMembers(RCompletionManager rCompletionManager);
   void injectMembers(PythonCompletionManager pythonCompletionManager);
   void injectMembers(ScopeTreeManager scopeTreeManager);
   void injectMembers(SVNCommandHandler svnCommandHandler);
   void injectMembers(CaptionWithHelp captionWithHelp);
   void injectMembers(RnwWeaveSelectWidget selectWidget);
   void injectMembers(TextEditingTargetCompilePdfHelper compilePdfHelper);
   void injectMembers(RealtimeSpellChecker realtimeSpellChecker);
   void injectMembers(SpellingCustomDictionariesWidget widget);
   void injectMembers(FileExport fileExport);
   void injectMembers(RPubsUploadDialog uploadDialog);
   void injectMembers(CompileNotebookOptionsDialog notebookOptionsDialog);
   void injectMembers(ProjectPreferencesPane projectPrefsPane);
   void injectMembers(BuildToolsPackagePanel buildToolsPackagePanel);
   void injectMembers(CodeFilesList codeFilesList);
   void injectMembers(ToolbarPopupMenu toolbarPopupMenu);
   void injectMembers(ProjectPopupMenu projectPopupMenu);
   void injectMembers(ClearAllDialog clearAllDialog);
   void injectMembers(TextEditingTargetPresentationHelper presHelper);
   void injectMembers(TextEditingTargetRMarkdownHelper rmarkdownHelper);
   void injectMembers(TextEditingTargetCommentHeaderHelper commentHeaderHelper);
   void injectMembers(TextEditingTargetCppHelper cppHelper);
   void injectMembers(TextEditingTargetJSHelper jsHelper);
   void injectMembers(TextEditingTargetRHelper rHelper);
   void injectMembers(TextEditingTargetSqlHelper sqlHelper);
   void injectMembers(TextEditingTargetChunks chunks);
   void injectMembers(TextEditingTargetPackageDependencyHelper packageDependencyHelper);
   void injectMembers(TextEditingTargetQuartoHelper quartoHelper);
   void injectMembers(EditingTargetCodeExecution codeExecution);
   void injectMembers(LocalRepositoriesWidget localRepositoriesWidget);
   void injectMembers(CppCompletionRequest request);
   void injectMembers(CppCompletionManager completionManager);
   void injectMembers(SignatureToolTipManager manager);
   void injectMembers(PathBreadcrumbWidget pathBreadcrumbWidget);
   void injectMembers(LintManager manager);
   void injectMembers(CompletionRequester requester);
   void injectMembers(EditSnippetsDialog dialog);
   void injectMembers(RoxygenHelper helper);
   void injectMembers(SnippetHelper helper);
   void injectMembers(RSConnectPublishButton publishButton);
   void injectMembers(RSConnectDeploy deploy);
   void injectMembers(AceEditorWidget widget);
   void injectMembers(WebApplicationHeaderOverlay headerOverlay);
   void injectMembers(DocumentOutlineWidget widget);
   void injectMembers(SetupChunkOptionsPopupPanel panel);
   void injectMembers(SourceSatellite satellite);
   void injectMembers(ShinyApplicationSatellite satellite);
   void injectMembers(ModifyKeyboardShortcutsWidget widget);
   void injectMembers(ShortcutManager manager);
   void injectMembers(UserCommandManager manager);
   void injectMembers(EditorCommandManager manager);
   void injectMembers(ApplicationCommandManager manager);
   void injectMembers(TextEditingTargetNotebook notebook);
   void injectMembers(WindowFrame frame);
   void injectMembers(ShinyGadgetDialog dialog);
   void injectMembers(NewShinyWebApplication dialog);
   void injectMembers(NewPlumberAPI dialog);
   void injectMembers(AddinCommandBinding binding);
   void injectMembers(BrowseAddinsDialog dialog);
   void injectMembers(AddinExecutor addinExecutor);
   void injectMembers(DataImportDialog dataImportDialog);
   void injectMembers(DataImport dataImport);
   void injectMembers(DataImportOptionsUiCsv dataImportOptionsUiCsv);
   void injectMembers(DataImportOptionsUiCsvLocale dataImportOptionsUiCsvLocale);
   void injectMembers(CppCompletion completion);
   void injectMembers(ConsoleTabPanel consoleTabPanel);
   void injectMembers(VirtualConsole console);
   void injectMembers(NewConnectionShinyHost newConnectionShinyHost);
   void injectMembers(ConnectionCodePanel connectionCodePanel);
   void injectMembers(ConnectionExplorer connectionExplorer);
   void injectMembers(ObjectBrowser tableBrowser);
   void injectMembers(ObjectBrowserModel tableBrowserModel);
   void injectMembers(ChunkOptionsPopupPanel panel);
   void injectMembers(ChunkSatellite satellite);
   void injectMembers(AceEditorBackgroundLinkHighlighter highlighter);
   void injectMembers(AceEditorCoverageHighlighter highlighter);
   void injectMembers(TextEditingTargetIdleMonitor monitor);
   void injectMembers(AceEditorIdleCommands commands);
   void injectMembers(EditingTargetInlineChunkExecution executor);
   void injectMembers(DataImportFileChooser dataImportFileChooser);
   void injectMembers(ProjectTemplateRegistryProvider provider);
   void injectMembers(TerminalSession widget);
   void injectMembers(TerminalPopupMenu menu);
   void injectMembers(TerminalList terminalList);
   void injectMembers(NewConnectionWizard newConnectionWizard);
   void injectMembers(RStudioAPI rstudioAPI);
   void injectMembers(NewConnectionSnippetHost newConnectionSnippetHost);
   void injectMembers(NewConnectionSnippetDialog newConnectionSnippetDialog);
   void injectMembers(NewPackagePage page);
   void injectMembers(NewConnectionInstallPackagePage newConnectionInstallPackagePage);
   void injectMembers(ObjectExplorerEditingTargetWidget widget);
   void injectMembers(ObjectExplorerDataGrid widget);
   void injectMembers(TerminalInfoDialog infoDialog);
   void injectMembers(AceEditorMixins mixins);
   void injectMembers(RStudioThemedFrame frame);
   void injectMembers(AceBackgroundHighlighter highlighter);
   void injectMembers(AddinsToolbarButton button);
   void injectMembers(AboutDialog aboutDialog);
   void injectMembers(NewConnectionInstallOdbcHost newConnectionInstallOdbcHost);
   void injectMembers(NewConnectionPreInstallOdbcHost NewConnectionPreInstallOdbcHost);
   void injectMembers(SecondaryReposWidget widget);
   void injectMembers(SecondaryReposDialog widget);
   void injectMembers(CheckForUpdatesDialog dialog);
   void injectMembers(JobsPresenterEventHandlersImpl jobPresenterBaseImpl);
   void injectMembers(JobsDisplayImpl jobDisplayBaseImpl);
   void injectMembers(PanmirrorPandocServer panmirrorPandocServer);
   void injectMembers(PanmirrorCrossrefServer panmirrorCrossrefServer);
   void injectMembers(PanmirrorDataCiteServer panmirrorDataCiteServer);
   void injectMembers(PanmirrorPubMedServer panmirrorPubMedServer);
   void injectMembers(PanmirrorDOIServer panmirrorDOIServer);
   void injectMembers(PanmirrorXRefServer panmirrorXRefServer);
   void injectMembers(PanmirrorZoteroServer panmirrorZoteroServer);
   void injectMembers(PanmirrorEnvironmentServer panmirrorEnvironmentServer);
   void injectMembers(PanmirrorDialogs panmirrorEditorUI);
   void injectMembers(PanmirrorWidget panmirrorWidget);
   void injectMembers(PanmirrorOutlineWidget panmirrorOutlineWidget);
   void injectMembers(PanmirrorEditRawDialog panmirrorEditRawDialog);
   void injectMembers(PanmirrorInsertCiteDialog panmirrorInsertCiteDialog);
   void injectMembers(PanmirrorEditImageDialog panmirrorEditImageDialog);
   void injectMembers(PanmirrorUIDisplay panmirrorUIDisplay);
   void injectMembers(PanmirrorUIPrefs panmirrorUIPrefs);
   void injectMembers(VisualMode visualMode);
   void injectMembers(VisualModeNavigation visualModeNavigation);
   void injectMembers(VisualModePanmirrorContext visualModePanmirrorContext);
   void injectMembers(VisualModePanmirrorFormat visualModePanmirrorFormat);
   void injectMembers(VisualModeMarkdownWriter visualModeMarkdownWriter);
   void injectMembers(VisualModeSpelling visualModeSpelling);
   void injectMembers(VisualModeConfirm visualModeConfirm);
   void injectMembers(OpenProjectDialog dialog);
   void injectMembers(SourceColumn column);
   void injectMembers(SourceColumnManager columnManager);
   void injectMembers(PythonPreferencesPaneBase<?> pane);
   void injectMembers(ConsoleInterpreterVersion version);
   void injectMembers(NewQuartoDocumentDialog newQuartoDocumentDialog);
   void injectMembers(NewQuartoProjectPage newProjectPage);
   void injectMembers(QuartoConnection quartoMessageBus);
   void injectMembers(YamlEditorToolsProviderQuarto yamlCompletionSourceQuarto);


   public static final RStudioGinjector INSTANCE = GWT.create(RStudioGinjector.class);

   Application getApplication();
   ApplicationInterrupt getApplicationInterrupt();
   VCSApplication getVCSApplication();
   HTMLPreviewApplication getHTMLPreviewApplication();
   ShinyApplication getShinyApplication();
   ShinyViewerTypePopupMenu getShinyViewerTypePopupMenu();
   RmdOutputSatellite getRmdOutputSatellite();
   RmdOutputFramePane getRmdOutputFramePane();
   RmdOutputFrameSatellite getRmdOutputFrameSatellite();
   EventBus getEventBus();
   GlobalDisplay getGlobalDisplay();
   RemoteFileSystemContext getRemoteFileSystemContext();
   FileDialogs getFileDialogs();
   FileTypeRegistry getFileTypeRegistry();
   RnwWeaveRegistry getRnwWeaveRegistry();
   LatexProgramRegistry getLatexProgramRegistry();
   Commands getCommands();
   UserPrefs getUserPrefs();
   UserState getUserState();
   Session getSession();
   HelpStrategy getHelpStrategy();
   ShortcutViewer getShortcutViewer();
   SourceColumnManager getSourceColumnManager();
   Satellite getSatellite();
   SatelliteManager getSatelliteManager();
   SourceWindowManager getSourceWindowManager();
   SourceWindow getSourceWindow();
   Server getServer();
   ChunkWindowManager getChunkWindowManager();
   ProjectTemplateRegistryProvider getProjectTemplateRegistryProvider();
   AceThemes getAceThemes();
   AddinsCommandManager getAddinsCommandManager();
   DependencyManager getDependencyManager();
   ShinyTestPopupMenu getShinyTestPopupMenu();
   HtmlMessageListener getHtmlMessageListener();
   PlumberViewerTypePopupMenu getPlumberViewerTypePopupMenu();
   PlumberAPISatellite getPlumberAPISatellite();
   PlumberAPI getPlumberAPI();
   JobManager getJobManager();
   SessionOpener getSessionOpener();
   VirtualConsoleFactory getVirtualConsoleFactory();
   JobItemFactory getJobItemFactory();
   FocusVisiblePolyfill getFocusVisiblePolyfill();
   AriaLiveService getAriaLiveService();
   
   // Pro-only below here
}
