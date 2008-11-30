/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.vimplugin;
// TODO : Incompatible License with GPL, need to remove

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class VimPluginStrings {

  private static final String RESOURCE_BUNDLE = "org.vimplugin.VimPluginStrings";

  private static ResourceBundle fgResourceBundle = ResourceBundle
      .getBundle(RESOURCE_BUNDLE);

  private VimPluginStrings() {
  }

  public static String getString(String key) {
    try {
      return fgResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      return "!" + key + "!";
    }
  }

  public static ResourceBundle getResourceBundle() {
    return fgResourceBundle;
  }
}
