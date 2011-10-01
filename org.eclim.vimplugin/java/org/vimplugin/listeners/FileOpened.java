/*
 * Vimplugin
 *
 * Copyright (c) 2008 - 2011 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin.listeners;

import org.vimplugin.VimEvent;
import org.vimplugin.VimException;
import org.vimplugin.VimPlugin;
import org.vimplugin.VimServer;
import org.vimplugin.editors.VimEditor;

/**
 * The File was opened, set Titles.
 */
public class FileOpened implements IVimListener {

  /**
   * reacts to "fileOpened" by opening the file in the
   * {@link org.vimplugin.editors.VimEditor VimEditor}.
   *
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   */
  public void handleEvent(VimEvent ve) throws VimException {
    String event = ve.getEvent();
    if (event.equals("fileOpened") == true) {
      String filePath = ve.getArgument(0);
      filePath = filePath.substring(1, filePath.length() - 1);
      VimServer server = VimPlugin.getDefault()
        .getVimserver(ve.getConnection().getVimID());
      for (VimEditor veditor : server.getEditors()) {
        if (veditor.getBufferID() == ve.getBufferID() || !server.isExternalTabbed()) {
          veditor.setTitleTo(filePath);
        }
      }
    }
  }
}
