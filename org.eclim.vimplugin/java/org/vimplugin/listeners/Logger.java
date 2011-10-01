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

/**
 * Writes all events to the debug console.
 */
public class Logger implements IVimListener {
  private static final org.eclim.logging.Logger logger =
    org.eclim.logging.Logger.getLogger(Logger.class);

  /**
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   */
  public void handleEvent(VimEvent ve) {
    logger.debug(ve.getLine());
  }
}
