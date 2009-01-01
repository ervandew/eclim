package org.vimplugin;

import org.eclim.logging.Logger;

import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.Point;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * Utility functions for working with the eclipse display.
 */
public class DisplayUtils
{
  private static final Logger logger = Logger.getLogger(DisplayUtils.class);

  /**
   * Performs a click by first moving the mouse to the specified x,y coordinates
   * and then issuing a left button mouse click.
   *
   * @param display The Display instance.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param restore boolean indicating whether or not the cursor should be
   * restored to its original position after the click is performed.
   */
  public static void doClick(
      final Display display, final int x, final int y, boolean restore)
  {
    // save the original cursor location.
    final int[] orig = {0, 0};
    if (restore){
      display.syncExec(new Runnable(){
        public void run()
        {
          Point point = display.getCursorLocation();
          orig[0] = point.x;
          orig[1] = point.y;
        }
      });
    }

    Event event = new Event();
    event.x = x;
    event.y = y;
    event.type = SWT.MouseMove;
    post(display, event);

    // wait for the cursor to move.
    final boolean[] moved = {false};
    Thread check = new Thread(){
      public void run(){
        final int[] cursor = {0, 0};
        while(!isInterrupted() && cursor[0] != x || cursor[1] != y){
          display.syncExec(new Runnable(){
            public void run()
            {
              Point point = display.getCursorLocation();
              cursor[0] = point.x;
              cursor[1] = point.y;
              if (point.x == x && point.y == y){
                moved[0] = true;
              }
            }
          });
          try{
            Thread.sleep(25);
          }catch(InterruptedException ie){
            break;
          }
        }
      }
    };
    check.start();
    try{
      check.join(2000);
      check.interrupt();
    }catch(InterruptedException ie){
      logger.debug("interrupted while waiting", ie);
    }

    if (moved[0]){
      event = new Event();
      event.button = 1;
      event.type = SWT.MouseDown;
      post(display, event);

      event.type = SWT.MouseUp;
      post(display, event);
    }

    // restore cursor to original position.
    if (restore){
      event = new Event();
      event.x = orig[0];
      event.y = orig[1];
      event.type = SWT.MouseMove;
      post(display, event);
    }
  }

  /**
   * Perform a keypress using the supplied key and optional modifiers.
   *
   * @param display The Display instance.
   * @param key The SWT key code.
   * @param modifiers Optional one or more SWT key modifiers
   */
  public static void doKeypress(Display display, int key, int ... modifiers)
  {
    for (int modifier : modifiers){
      Event event = new Event();
      event.type = SWT.KeyDown;
      event.keyCode = modifier;
      post(display, event);
    }

    Event event = new Event();
    event.keyCode = key;

    event.type = SWT.KeyDown;
    post(display, event);

    event.type = SWT.KeyUp;
    post(display, event);

    for (int modifier : modifiers){
      event = new Event();
      event.type = SWT.KeyUp;
      event.keyCode = modifier;
      post(display, event);
    }
  }

  /**
   * Perform a keypress using the supplied key and optional modifiers.
   *
   * @param display The Display instance.
   * @param key The SWT character key.
   * @param modifiers Optional one or more SWT key modifiers
   */
  public static void doKeypress(Display display, char key, int ... modifiers)
  {
    for (int modifier : modifiers){
      Event event = new Event();
      event.type = SWT.KeyDown;
      event.keyCode = modifier;
      post(display, event);
    }

    Event event = new Event();
    event.character = key;

    event.type = SWT.KeyDown;
    post(display, event);

    event.type = SWT.KeyUp;
    post(display, event);

    for (int modifier : modifiers){
      event = new Event();
      event.type = SWT.KeyUp;
      event.keyCode = modifier;
      post(display, event);
    }
  }

  private static void post(Display display, Event event)
  {
    if (!display.post(event)){
      throw new RuntimeException("Failed to post event.");
    }
  }
}
