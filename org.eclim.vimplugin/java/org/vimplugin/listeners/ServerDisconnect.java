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

import java.io.IOException;

import org.vimplugin.VimEvent;
import org.vimplugin.VimException;
import org.vimplugin.VimPlugin;
import org.vimplugin.VimServer;
import org.vimplugin.editors.VimEditor;

/**
 * Closes the editor window if the server was closed.
 */
public class ServerDisconnect implements IVimListener {

  /**
   * Disposes the {@link org.vimplugin.editors.VimEditor ViMEditor} on
   * "disconnect" or killed.
   *
   * @throws VimException when the VimConnection could not be closed (wraps IOException).
   */
  public void handleEvent(VimEvent ve) throws VimException {
    String event = ve.getEvent();

    if (event.equals("disconnect") || event.equals("killed")) {
      VimPlugin plugin = VimPlugin.getDefault();
      VimServer server = plugin.getVimserver(ve.getConnection().getVimID());

      for (final VimEditor veditor : server.getEditors()) {
        if (veditor != null) {
          if (event.equals("disconnect") ||
              veditor.getBufferID() == ve.getBufferID())
          {
            veditor.forceDispose();
          }
        }
      }

      if (event.equals("disconnect") || server.getEditors().size() == 0){
        try {
          ve.getConnection().close();
        } catch (IOException e) {
          throw new VimException("could not close the vim connection", e);
        }

        plugin.stopVimServer(server.getID());
      }
    }
  }
}
