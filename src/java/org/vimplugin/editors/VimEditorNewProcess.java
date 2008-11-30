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
 * A VimEditor class intended to open a new Vim instance each time its
 * called.
 *
 */
public class VimEditorNewProcess extends AbstractVimEditor {

  public VimEditorNewProcess() {
    super();
    serverID = VimPlugin.getDefault().createVimServer();
  }
}
