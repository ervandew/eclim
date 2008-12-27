/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclipse.swt.widgets;

import java.lang.reflect.Field;

//import org.eclim.eclipse.ui.internal.EclimWorkbenchWindow;

import org.eclipse.swt.widgets.Display;

//import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Giant hack to get some of the eclipse features that are too closely tied to
 * the ui to work in a headless environment.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class EclimDisplay
  extends Display
{
  private static final String THREAD = "thread";
  //private static Shell shell;

  /**
   * Force the display to think that it's tied to the supplied thread.
   */
  public void setThread (Thread _thread)
  {
    try{
      Field thread = Display.class.getDeclaredField(THREAD);
      thread.setAccessible(true);
      thread.set(this, _thread);

      // set up some default workspace environment components.
      /*if (shell == null){
        shell = new Shell();
        WorkbenchWindow window = new EclimWorkbenchWindow(shell);
        shell.setData(window);
      }*/
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   * @see Display#getActiveShell()
   */
  /*@Override
  public Shell getActiveShell ()
  {
    return shell;
  }*/

  /**
   * {@inheritDoc}
   * @see Display#getActiveShell()
   */
  @Override
  public void setSynchronizer (Synchronizer synchronizer) {
    // don't let eclipse set its UISynchronizer.
  }
}
