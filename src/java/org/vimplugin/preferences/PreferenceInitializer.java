/*
 * Vimplugin
 *
 * Copyright (c) 2007 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.vimplugin.VimPlugin;

/**
 * Initializes default preference values.
 */
public class PreferenceInitializer
  extends AbstractPreferenceInitializer
{
  /**
   * @see AbstractPreferenceInitializer#initializeDefaultPreferences()
   */
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = VimPlugin.getDefault().getPreferenceStore();
    store.setDefault(PreferenceConstants.P_PORT, 3219);
    store.setDefault(PreferenceConstants.P_EMBED,
        "true".equals(VimPlugin.getDefault().getProperty("gvim.embed.default")));
    store.setDefault(PreferenceConstants.P_TABBED, true);
    store.setDefault(PreferenceConstants.P_FOCUS_AUTO_CLICK, true);
    store.setDefault(PreferenceConstants.P_START_ECLIMD, true);
    store.setDefault(PreferenceConstants.P_GVIM,
        VimPlugin.getDefault().getProperty("gvim.default"));
  }
}
