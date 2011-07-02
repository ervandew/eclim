/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

import org.eclim.eclipse.AbstractEclimApplication;
import org.eclim.eclipse.EclimPlugin;

import org.eclim.eclipse.ui.internal.EclimWorkbenchWindow;

import org.eclipse.swt.SWT;

//import org.eclim.eclipse.ui.internal.EclimWorkbenchWindow;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.internal.WorkbenchWindow;

//import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Giant hack to get some of the eclipse features that are too closely tied to
 * the ui to work in a headless environment.
 *
 * @author Eric Van Dewoestine
 */
public class EclimDisplay
  extends Display
{
  private static final String THREAD = "thread";
  private static Shell shell;

  /**
   * Force the display to think that it's tied to the supplied thread.
   */
  public void setThread(Thread _thread)
  {
    try{
      Field thread = Display.class.getDeclaredField(THREAD);
      thread.setAccessible(true);
      thread.set(this, _thread);

      // set up some default workspace environment components.
      if (shell == null){
        if (!AbstractEclimApplication.getInstance().isStopping()){
          shell = EclimPlugin.getShell();
          WorkbenchWindow window = new EclimWorkbenchWindow();
          shell.setData(window);
        }
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   * @see Display#getShells()
   */
  @Override
  public Shell[] getShells()
  {
    if (AbstractEclimApplication.getInstance().isHeaded()){
      return super.getShells();
    }
    return new Shell[]{EclimPlugin.getShell()};
  }

  /**
   * {@inheritDoc}
   * @see Display#getActiveShell()
   */
  @Override
  public Shell getActiveShell()
  {
    return shell;
  }

  /**
   * {@inheritDoc}
   * @see Display#setSynchronizer(Synchronizer)
   */
  @Override
  public void setSynchronizer(Synchronizer synchronizer) {
    // don't let eclipse set its UISynchronizer.
  }

  protected void checkDevice() {
    Thread thread;
    try{
      Field thread_ = Display.class.getDeclaredField(THREAD);
      thread_.setAccessible(true);
      thread = (Thread)thread_.get(this);
    }catch(Exception e){
      throw new RuntimeException(e);
    }

    if (thread == null) error (SWT.ERROR_WIDGET_DISPOSED);
    // since eclipse 3.7: disabling this check since it prevents
    // org.eclipse.ui.ide from loading:
    //   at org.eclipse.swt.widgets.Display.checkDevice(:752)
    //   at org.eclipse.swt.widgets.Display.timerExec(:4110)
    //   at org.eclipse.ui.internal.ide.IDEWorkbenchPlugin.createProblemsViews(:390)
    //   at org.eclipse.ui.internal.ide.IDEWorkbenchPlugin.start(:351)
    // I'm justifying this as a continuation of the setThread hack which exists
    // because the nailgun requests never run the on the main thread.
    //if (thread != Thread.currentThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
    if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
  }
}
