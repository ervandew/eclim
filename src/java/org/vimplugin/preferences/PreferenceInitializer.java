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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.vimplugin.VimPlugin;

/**
 * Initializes default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

  /**
   * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
   */
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = VimPlugin.getDefault().getPreferenceStore();
    store.setDefault(PreferenceConstants.P_PORT, 3219);
    store.setDefault(PreferenceConstants.P_HOST, "localhost");
    store.setDefault(PreferenceConstants.P_PASS, "changeme");

    //Platform specific code
    if( Platform.getOS().equals(Platform.OS_WIN32) ) {
      store.setDefault(PreferenceConstants.P_GVIM, "!!TODO: typical location!!");
    } else if( Platform.getOS().equals(Platform.OS_LINUX) ) {
      store.setDefault(PreferenceConstants.P_GVIM, "/usr/bin/gvim");
    }

    store.setDefault(PreferenceConstants.P_DEBUG, true);
    store.setDefault(PreferenceConstants.P_EMBD, true);
  }
}
