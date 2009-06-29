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

    if (event.equals("disconnect") == true
        || event.equals("killed") == true) {
      for (final VimEditor veditor : VimPlugin.getDefault()
          .getVimserver(ve.getConnection().getVimID()).getEditors()) {
        if (veditor != null) {
          veditor.forceDispose();
        }
      }

      try {
        ve.getConnection().close();
      } catch (IOException e) {
        throw new VimException("could not close the vimconnection", e);
      }
    }
  }
}
