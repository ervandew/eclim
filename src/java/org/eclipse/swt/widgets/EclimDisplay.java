/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
