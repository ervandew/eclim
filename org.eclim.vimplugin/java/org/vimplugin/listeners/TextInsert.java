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
import org.vimplugin.editors.VimEditor;

/**
 * Some text has been inserted, so modify document also.
 */
public class TextInsert implements IVimListener {

  /**
   * reacts to "insert" by inserting the corresponding text into the editor.
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   */
  public void handleEvent(VimEvent ve) throws VimException {
    String event = ve.getEvent();
    if (event.equals("insert") == true) {
      int length = Integer.parseInt(ve.getArgument(0));
      String text = ve.getArgument(1);
      text = text.substring(1, text.length() - 1);
      for (VimEditor veditor : VimPlugin.getDefault()
          .getVimserver(ve.getConnection().getVimID()).getEditors()) {
        if (veditor.getBufferID() == ve.getBufferID()) {
          veditor.insertDocumentText(text, length);
        }
      }
    }
  }
}
