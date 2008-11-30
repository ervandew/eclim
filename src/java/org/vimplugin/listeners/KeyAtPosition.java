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
import org.vimplugin.handlers.IHandler;
import org.vimplugin.handlers.Undefined;

/**
 * Executes an {@link IHandler}, when a special key is pressed inside eclipse.
 * This class together with the Handlers resembles the command design pattern.
 *
 * TODO: reuse Eclipse-Commands facilities
 */
public class KeyAtPosition implements IVimListener {

  /** the key to react to */
  private final String key;

  /** the handler to execute */
  private IHandler handler;

  /**
   * Constructor sets the key and defaults the handler to "Undefined". There
   * should be only one KeyAtPosition Object per key.
   */
  public KeyAtPosition(String key) {
    this.key = key;
    handler = new Undefined();
  }

  /**
   * Reacts to "keyAtPos" by calling {@link #handler} with the VimEvent.
   *
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   * @param the event contains information about the corresponding
   *        {@link AbstractVimEditor} and the position in the file.
   */
  public void handleEvent(VimEvent ve) throws VimException {
    String event = ve.getEvent();
    if (event.equals("keyAtPos")) {

      String keySeq = ve.getArgument(0);
      keySeq = keySeq.substring(1, keySeq.length() - 1);

      for (AbstractVimEditor veditor : VimPlugin.getDefault()
          .getVimserver(ve.getConnection().getVimID()).getEditors()) {
        if (veditor.getBufferID() == ve.getBufferID() && keySeq.equals(key)) {
          handler.handle(ve);
        }

      }

    }
  }

  /**
   * Simple Getter.
   * @return the current handler.
   */
  public IHandler getHandler() {
    return handler;
  }

  /**
   * Simple Setter to be called when preferences change.
   *
   * @param handler the new handler for this keyAtPos-event
   */
  public void setHandler(IHandler handler) {
    this.handler = handler;
  }

  /**
   * Simple getter to lookup the key of a specific KeyAtPosition Listener object.
   * @return
   */
  public String getKey() {
    return key;
  }
}
