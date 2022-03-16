/*
 * UnhandledError.java
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

package org.rstudio.studio.client.common.debugging.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class UnhandledError extends JavaScriptObject
{
   protected UnhandledError() {}
   
   public final native String getErrorMessage() /*-{
      return this.message;
   }-*/;
   
   public final native JsArray<ErrorFrame> getErrorFrames() /*-{
      return this.frames;
   }-*/;

   public final native JsArrayString getTrace() /*-{
      return this.trace;
   }-*/;
}
