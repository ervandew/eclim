/**
 * Copyright (c) 2005 - 2008
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
package org.eclim.eclipse.ui.internal;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Extension to eclipse WorkbenchWindow.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class EclimWorkbenchWindow
  extends WorkbenchWindow
{
  private Shell shell;
  private IWorkbenchPage page;
  private Composite composite;

  public EclimWorkbenchWindow (Shell shell){
    super(1);
    this.shell = shell;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchWindow#getActivePage()
   */
  public IWorkbenchPage getActivePage ()
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
  public Shell getShell ()
  {
    return shell;
  }

  /**
   * {@inheritDoc}
   * @see WorkbenchWindow#getPageComposite()
   */
  @Override
  protected Composite getPageComposite ()
  {
    if(composite == null){
      composite = new Composite(shell, SWT.NONE);
    }
    return composite;
  }
}
