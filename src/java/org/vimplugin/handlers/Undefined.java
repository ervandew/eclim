package org.vimplugin.handlers;

/**
 * Trivial Handler for unassigned KeyAtPosition-Objects.
 */
public class Undefined implements IHandler {

  /** a message to display */
  private final String message;

  /** sets message to "Undefined!" */
  public Undefined() {
    message = "Undefined!";
  }

  /** sets message */
  public Undefined(String m) {
    message = m;
  }

  /** sysouts message */
  public void handle(Object... params){
    System.out.println(message);
  }
}
