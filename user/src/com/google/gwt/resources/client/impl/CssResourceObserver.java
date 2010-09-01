/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.resources.client.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.DebugInfo;

import java.util.Map;

/**
 * Enables CssResources to be intercepted before injection. The base
 * implementation is a no-op.
 * <p>
 * <em>This is an internal implementation API that is subject to change.</em>
 */
public class CssResourceObserver {
  private static final CssResourceObserver IMPL = GWT.create(CssResourceObserver.class);

  /**
   * An implementation of CssResourceObserver that records CssResource
   * obfuscation data into a JavaScriptObject accessible on the main application
   * window as <code>$wnd.gwtCssResource['moduleName']</code>.
   * <p>
   * The keys on this object will be of the form
   * <code>&lt;ClientBundle type name>.&lt;CssResource method name>.&lt;Raw css class selector></code>
   * . An example key might be
   * <code>com.example.Resources.superButton.button-outer-div</code>. The value
   * associated with the key is the (possibly obfuscated) CSS class selector
   * used in the injected code.
   */
  public static class Mapper extends CssResourceObserver {
    private final JavaScriptObject myMap;

    public Mapper() {
      myMap = ensureMap();
    }

    @Override
    protected <T extends CssResource> T registerImpl(T resource) {
      DebugInfo info = resource.getDebugInfo();
      if (info != null) {
        String prefix = info.getOwnerType() + "." + info.getMethodName() + ".";
        for (Map.Entry<String, String> entry : info.getClassMap().entrySet()) {
          set(myMap, prefix + entry.getKey(), entry.getValue());
        }
      }
      return resource;
    }

    /**
     * Creates or returns the module-specific CssResource map.
     */
    private native JavaScriptObject ensureMap() /*-{
      var topMap = $wnd.gwtCssResource;
      if (!topMap) {
        topMap = $wnd.gwtCssResource = {};
      }
      var moduleName = @com.google.gwt.core.client.GWT::getModuleName()();
      var myMap = topMap[moduleName];
      if (!myMap) {
          myMap = topMap[moduleName] = {};
      }
      return myMap;
    }-*/;

    /**
     * Trivial JS associative array assignment.
     */
    private native void set(JavaScriptObject map, String key, String value) /*-{
      map[key] = value;
    }-*/;
  }

  /**
   * Register a CssResource. This method is called from code generated by
   * CssResourceGenerator.
   */
  public static <T extends CssResource> T register(T resource) {
    return IMPL.registerImpl(resource);
  }

  /**
   * No-op.
   */
  protected <T extends CssResource> T registerImpl(T resource) {
    return resource;
  }
}
