/*
 * SessionInitEvent.java
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
package org.rstudio.studio.client.workbench.events;

import com.google.gwt.event.shared.GwtEvent;

public class SessionInitEvent extends GwtEvent<SessionInitHandler>
{
   public static final GwtEvent.Type<SessionInitHandler> TYPE =
      new GwtEvent.Type<SessionInitHandler>();
   
   public SessionInitEvent()
   {
   }
   
   @Override
   protected void dispatch(SessionInitHandler handler)
   {
      handler.onSessionInit(this);
   }

   @Override
   public GwtEvent.Type<SessionInitHandler> getAssociatedType()
   {
      return TYPE;
   }
}