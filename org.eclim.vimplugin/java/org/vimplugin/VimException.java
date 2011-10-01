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
package org.vimplugin;

/**
 * A simple Exception that identifies Exceptions produced by vimplugin.
 */
public class VimException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a VimException with a message that explains what happened.
   *
   * @param message the short and concise description.
   */
  public VimException(String message) {
    super(message);
  }

  /**
   * Constructs a VimException with a message that explains what happened, and
   * an exception which caused this exception.
   *
   * @param message the short and concise description.
   * @param cause the cause of this exception
   */
  public VimException(String message, Exception cause) {
    super(message,cause);
  }
}
