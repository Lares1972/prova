/*
 * CopyPlotToClipboardDesktopDialog.java
 *
 * Copyright (C) 2009-17 by RStudio, Inc.
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
package org.rstudio.studio.client.workbench.exportplot.clipboard;

import org.rstudio.core.client.BrowseCap;
import org.rstudio.core.client.dom.ElementEx;
import org.rstudio.core.client.dom.WindowEx;
import org.rstudio.core.client.widget.Operation;
import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.core.client.widget.ProgressIndicator;
import org.rstudio.studio.client.application.Desktop;
import org.rstudio.studio.client.application.DesktopWebAction;
import org.rstudio.studio.client.workbench.exportplot.ExportPlotPreviewer;
import org.rstudio.studio.client.workbench.exportplot.ExportPlotSizeEditor;
import org.rstudio.studio.client.workbench.exportplot.model.ExportPlotOptions;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Command;

public class CopyPlotToClipboardDesktopDialog 
      extends CopyPlotToClipboardDesktopDialogBase
{

   public CopyPlotToClipboardDesktopDialog(
                           ExportPlotPreviewer previewer,
                           ExportPlotClipboard clipboard,
                           ExportPlotOptions options,
                           OperationWithInput<ExportPlotOptions> onClose)
   {
      super(options, previewer, onClose);
     
      indicator_ = addProgressIndicator(false);
      clipboard_ = clipboard;
   }
   
   protected void copyAsBitmap(final Operation onCompleted)
   {
      final ExportPlotSizeEditor sizeEditor = getSizeEditor();
      
      final Command completed = new Command()
      {
         @Override
         public void execute()
         {
            sizeEditor.setGripperVisible(true);
            onCompleted.execute();
         }
      };
      
      sizeEditor.setGripperVisible(false);
      
      sizeEditor.prepareForExport(() -> {
         if (BrowseCap.isMacintoshDesktop())
         {
            clipboard_.copyPlotToCocoaPasteboard(
                  sizeEditor.getImageWidth(),
                  sizeEditor.getImageHeight(),
                  completed);
         }
         else
         {
            WindowEx win = sizeEditor.getPreviewIFrame().getContentWindow();
            Document doc = win.getDocument();
            NodeList<Element> images = doc.getElementsByTagName("img");
            if (images.getLength() > 0)
            {
               indicator_.onProgress("Copying plot...");
               
               ElementEx img = images.getItem(0).cast();
               img.focus();
               Desktop.getFrame().performWebAction(DesktopWebAction.CopyImageToClipboard, () -> {
                  indicator_.onCompleted();
                  completed.execute();
               });
            }
            else
            {
               // shouldn't happen but make sure we clean up after
               completed.execute();
            }
         }
      });
   }
   
   protected final ExportPlotClipboard clipboard_;
   private final ProgressIndicator indicator_;
}
