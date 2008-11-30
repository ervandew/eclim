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
package org.vimplugin.editors;

import org.vimplugin.VimPlugin;

/**
 * Vim Editor class that tries to open a new buffer in a default Vim instance
 * if it exists and if it doesn't it creates it.
 *
 */
public class VimEditor extends AbstractVimEditor {

  public VimEditor() {
    super();
    this.serverID = VimPlugin.getDefault().getDefaultVimServer();
  }
}
