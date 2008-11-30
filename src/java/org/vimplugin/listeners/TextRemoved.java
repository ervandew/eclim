/*
 * Vimplugin
 *
 * Copyright (c) 2008 by The Vimplugin Project.
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
import org.vimplugin.editors.AbstractVimEditor;

/**
 * Some text has been removed.. so remove that text in document also.
 */
public class TextRemoved implements IVimListener {

  /**
   * reacts on "remove" by removing the text from the editor.
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   */
  public void handleEvent(VimEvent ve) throws VimException {
    String event = ve.getEvent();
    if (event.equals("remove") == true) {
      int offset = Integer.parseInt(ve.getArgument(0));
      int length = Integer.parseInt(ve.getArgument(1));
      for (AbstractVimEditor veditor : VimPlugin.getDefault()
          .getVimserver(ve.getConnection().getVimID()).getEditors()) {
        if (veditor.getBufferID() == ve.getBufferID()) {
          veditor.removeDocumentText(offset, length);
        }
      }
    }
  }
}
