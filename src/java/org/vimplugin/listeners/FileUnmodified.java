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
package org.vimplugin.listeners;

import org.vimplugin.VimConnection;
import org.vimplugin.VimEvent;
import org.vimplugin.VimException;
import org.vimplugin.VimPlugin;
import org.vimplugin.VimServer;
import org.vimplugin.editors.VimEditor;

/**
 * The File became unmodified.
 */
public class FileUnmodified implements IVimListener {

  /**
   * Removes the star from the editors title on "save" or "unmodified".
   *
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   */
  public void handleEvent(VimEvent ve) throws VimException {
    String event = ve.getEvent();
    // for some reason the "unmodified" event is commented out in the vim code.
    // since that event, and not the "save" event, is the one we need, as a
    // workaround eclim includes an autocommand which sends an equivalent
    // keyCommand.
    if (event.equals("unmodified") ||
        (event.equals("keyCommand") && ve.getArgument(0).equals("\"unmodified\""))){
      VimPlugin plugin = VimPlugin.getDefault();
      VimConnection vc = ve.getConnection();
      VimServer server = vc != null ? plugin.getVimserver(vc.getVimID()) : null;
      if (server != null){
        for (VimEditor editor : server.getEditors()){
          if (editor != null && editor.getBufferID() == ve.getBufferID()){
            editor.setDirty(false);
          }
        }
      }
    }
  }
}
