package org.vimplugin.handlers;

import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IParameter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * calls a {@link Command Eclipse Command }.
 */
public class EclipseCommand implements IHandler {

  /** the id of the command to execute */
  private final String id;

  /** sets the command id */
  public EclipseCommand(String id) {
    this.id = id;
  }

  /**
   * calls simplest eclipse commands, no parametrisation possible by now (for
   * example for code complete handlers).
   *
   * @see org.vimplugin.handlers.IHandler#handle(java.lang.Object[])
   * @param params ignored.
   */
  public void handle(Object... params) {
    ICommandService cservice = (ICommandService) PlatformUI.getWorkbench()
        .getService(ICommandService.class);
    Command c = cservice.getCommand(id);
    printCommandInfo(c);

    //TODO: How can we pass parameters/context etc here? What is expected by commands?
    ExecutionEvent ee = new ExecutionEvent(c, new HashMap<String, String>(),
        null, null);
    try {
      c.executeWithChecks(ee);
    } catch (Exception ex) {
      //TODO: Exception Handling!
      ex.printStackTrace();
    }
  }

  /**
   * Print Useful info about a command.
   *
   * @param c The command
   */
  @SuppressWarnings("unused")
  private void printCommandInfo(Command c) {
    try {
      System.out.println("Info about command "+c.getId());
      System.out.println("Defined: " + c.isDefined());
      System.out.println("Handled: " + c.isHandled());
      System.out.println("Enabled: " + c.isEnabled());

      IParameter[] params = c.getParameters();
      System.out.println("Params NULL: " + (params == null));

      if (params == null)
        return;

      System.out.println("# of Params: " + params.length);
      for (IParameter p : params) {
        System.out.println("ID: " + p.getId());
        System.out.println("NAME: " + p.getName());
        System.out.println("VALUES: " + p.getValues());
        System.out.println("OPTIONAL: " + p.isOptional());
      }
    } catch (Exception e) {
      // since this looks like debugging, just print the stacktrace in
      // case of errors.
      e.printStackTrace();
    }
  }
}
