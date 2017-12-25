package org.vimplugin.listeners;

import java.util.ArrayList;

import org.eclim.logging.Logger;

import org.eclim.util.StringUtils;

import org.eclipse.jface.bindings.keys.KeyStroke;

import org.eclipse.swt.graphics.Point;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.internal.Workbench;

import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard;

import org.eclipse.ui.keys.IBindingService;

import org.vimplugin.DisplayUtils;
import org.vimplugin.VimEvent;
import org.vimplugin.VimException;

/**
 *  Listen for feed keys request from vim.
 */
public class FeedKeys
  implements IVimListener
{
  private static final Logger logger = Logger.getLogger(FeedKeys.class);

  @Override
  public void handleEvent(VimEvent ve)
    throws VimException
  {
    String argument = null;
    if (ve.getEvent().equals("keyCommand") &&
        (argument = ve.getArgument(0)).startsWith("\"feedkeys "))
    {
      String keys = argument.substring(10, argument.length() - 1);
      // strip surrounding quotes if necessary.
      keys = keys.replaceAll("^\'(.*)\'$", "$1");
      keys = keys.replaceAll("^\"(.*)\"$", "$1");
      feedKeys(keys);
    }
  }

  private void feedKeys(final String keys)
  {
    try{
      final Workbench workbench = (Workbench)PlatformUI.getWorkbench();
      final Display display = workbench.getDisplay();

      // first issue a click on the eclipse workbench, to prevent gvim from
      // retaining focus after key stroke is processed.
      final int[] bounds = new int[2];
      display.syncExec(new Runnable(){
        public void run()
        {
          Shell shell = display.getActiveShell();
          if (shell == null){ // happens on windows
            shell = display.getShells()[0];
          }
          Point point = shell.toDisplay(1, 1);
          bounds[0] = point.x;
          bounds[1] = point.y;
        }
      });
      DisplayUtils.doClick(display, bounds[0] + 5, bounds[1] + 5, false);

      final String[] sequences = StringUtils.split(keys, ',');

      // process the actual key stroke.
      display.asyncExec(new Runnable(){
        public void run(){
          BindingService bindingService = (BindingService)
            PlatformUI.getWorkbench().getAdapter(IBindingService.class);
          WorkbenchKeyboard keyboard = bindingService.getKeyboard();
          for(String sequence : sequences){
            try{
              if (sequence.equals("refocus")){
                logger.debug("refocus gvim");
                IWorkbenchPage page = PlatformUI.getWorkbench()
                  .getActiveWorkbenchWindow().getActivePage();
                IEditorPart editor = page.getActiveEditor();
                if (editor != null){
                  editor.setFocus();
                }
                continue;
              }

              KeyStroke keyStroke = KeyStroke.getInstance(sequence);

              Event event = new Event();
              event.widget = display.getActiveShell();

              ArrayList<KeyStroke> keyStrokes = new ArrayList<KeyStroke>();
              keyStrokes.add(keyStroke);
              keyboard.press(keyStrokes, event);
              logger.debug("key strokes processed: " + sequence);
            }catch(Throwable t){
              logger.error("Error feeding keys.", t);
            }
          }
        }
      });
    }catch(Throwable t){
      logger.error("Error feeding keys.", t);
    }
  }
}
