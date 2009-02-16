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
package org.eclim.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclim.eclipse.EclimPlugin;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Extension to eclipse WorkbenchWindow.
 *
 * @author Eric Van Dewoestine
 */
public class EclimWorkbenchWindow
  extends WorkbenchWindow
{
  private IWorkbenchPage page;
  private Composite composite;

  public EclimWorkbenchWindow()
  {
    super(1);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchWindow#getActivePage()
   */
  public IWorkbenchPage getActivePage()
  {
    try{
      if(page == null){
        page = new EclimWorkbenchPage(
            this, ResourcesPlugin.getWorkspace().getRoot());
      }
      return page;
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchWindow#getShell()
   */
  public Shell getShell()
  {
    return EclimPlugin.getShell();
  }


  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchWindow#run(boolean,boolean,IRunnableWithProgress)
   */
  public void run(
      boolean fork, boolean cancelable, IRunnableWithProgress runnable)
    throws InvocationTargetException,
           InterruptedException
  {
    // no-op to prevent super class from running.
  }

  /**
   * {@inheritDoc}
   * @see WorkbenchWindow#fillActionBars(int)
   */
  @Override
  public void fillActionBars(int flags)
  {
    // no-op, needed for running inside of headed eclipse.
  }

  /**
   * {@inheritDoc}
   * @see WorkbenchWindow#getPageComposite()
   */
  @Override
  protected Composite getPageComposite()
  {
    if(composite == null){
      composite = new Composite(getShell(), SWT.NONE);
    }
    return composite;
  }
}
