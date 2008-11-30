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

import org.vimplugin.VimEvent;
import org.vimplugin.VimException;
import org.vimplugin.VimPlugin;
import org.vimplugin.editors.AbstractVimEditor;

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
    if (event.equals("save") == true || event.equals("unmodified") == true) {
      for (AbstractVimEditor veditor : VimPlugin.getDefault()
          .getVimserver(ve.getConnection().getVimID()).getEditors()) {
        if (veditor.getBufferID() == ve.getBufferID())
          veditor.setDirty(false);
      }
    }
  }
}
