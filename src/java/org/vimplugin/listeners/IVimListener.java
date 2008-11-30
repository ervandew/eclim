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

/**
 * reacts to {@link org.vimplugin.VimEvent events} issued by a vim instance.
 * Details on what messages are possible can be found in vim's documentation
 * of the protocol.
 *
 * @see <a href="http://www.vim.org/htmldoc/netbeans.html#netbeans-protocol">Protocol specification</a>
 *
 */
public interface IVimListener {

  /**
   * The VimEvent contains the specific messages we can react to. To do so, we
   * typically get the editor of the underlying
   * {@link org.vimplugin.VimConnection VimConnection}, and perform some
   * actions on it (like insterting text or similar ...).
   *
   * @param ve the event we react to.
   * @throws VimException if anything goes wrong.
   */
  public void handleEvent(VimEvent ve) throws VimException;

}
