/*
 * FileHyperlink.java
 *
 * Copyright (C) 2022 by RStudio, PBC
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
package org.rstudio.core.client.hyperlink;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import org.rstudio.core.client.FilePosition;
import org.rstudio.core.client.ResultCallback;
import org.rstudio.core.client.StringUtil;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.server.ServerError;
import org.rstudio.studio.client.workbench.views.source.SourceColumnManager;
import org.rstudio.studio.client.workbench.views.source.editors.EditingTarget;

public class FileHyperlink extends Hyperlink 
{
    public FileHyperlink(String url, String params, String text, String clazz)
    {
        super(url, params, text, clazz);

        filename = url.replaceFirst("file://", "");
        line = -1;
        col = -1;

        if (params_.containsKey("line"))
            line = StringUtil.parseInt(params_.get("line"), -1);
        
        if (params_.containsKey("col"))
            col = StringUtil.parseInt(params_.get("col"), -1);    
    }

    @Override
    public void onClick()
    {
        final SourceColumnManager columnManager = RStudioGinjector.INSTANCE.getSourceColumnManager(); 
        
        columnManager.editFile(filename, new ResultCallback<EditingTarget, ServerError>()
        {
            @Override
            public void onSuccess(final EditingTarget result)
            {
                if (line != -1) 
                {
                    // give ace time to render before scrolling to position
                    Scheduler.get().scheduleDeferred(() ->
                    {
                        FilePosition position = FilePosition.create(line, Math.max(col, 1));
                        columnManager.scrollToPosition(position, true, new Command(){

                            @Override
                            public void execute() {}
                            
                        } );
                    });
                }
            }
        });
    }

    @Override
    public Widget getPopupContent()
    {
        final VerticalPanel panel = new VerticalPanel();
        
        Label label = new Label(filename);
        label.setStyleName(styles_.code());
        panel.add(label);
        
        return panel;
    }
    
    private String filename;
    private int line;
    private int col;

}
