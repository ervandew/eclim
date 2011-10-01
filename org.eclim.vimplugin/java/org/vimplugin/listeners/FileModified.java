/*
 * Vimplugin
 *
 * Copyright (c) 2007 - 2011 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin.listeners;

import org.vimplugin.VimConnection;
import org.vimplugin.VimEvent;
import org.vimplugin.VimException;
import org.vimplugin.VimPlugin;
import org.vimplugin.VimServer;
import org.vimplugin.editors.VimEditor;

/**
 * The File became modified.
 */
public class FileModified implements IVimListener {

  /**
   * Marks the editors as modified on "modified" event.
   *
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   */
  public void handleEvent(VimEvent ve) throws VimException {
    String event = ve.getEvent();
    // workaround for not using buggy startDocumentListen support of vim's
    // netbeans protocol.
    if (event.equals("modified") ||
        (event.equals("keyCommand") && ve.getArgument(0).equals("\"modified\""))){
      VimPlugin plugin = VimPlugin.getDefault();
      VimConnection vc = ve.getConnection();
      VimServer server = vc != null ? plugin.getVimserver(vc.getVimID()) : null;
      if (server != null){
        for (VimEditor editor : server.getEditors()){
          if (editor != null && editor.getBufferID() == ve.getBufferID()){
            editor.setDirty(true);
          }
        }
      }
    }
  }
}
