package org.vimplugin.handlers;

import org.vimplugin.listeners.KeyAtPosition;

/**
 * Encapsulates the actual behavior of a command (in design pattern terms). The
 * command is in this case: {@link KeyAtPosition}.
 */
public interface IHandler {

  /**
   * Called by KeyAtPosition.
   *
   * @param params a list of 0 to n parameters
   * @see http://java.sun.com/j2se/1.5.0/docs/guide/language/varargs.html
   */
  public void handle(Object... params);
}
