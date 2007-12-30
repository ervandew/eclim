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

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.WorkbenchException;

import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Extension to eclipse WorkbenchPage.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class EclimWorkbenchPage
  extends WorkbenchPage
{
  private IEditorPart editor;

  public EclimWorkbenchPage (WorkbenchWindow window, IAdaptable input)
    throws WorkbenchException
  {
    super(window, input);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchPage#getActiveEditor()
   */
  public IEditorPart getActiveEditor ()
  {
    return editor;
  }

  public void setActiveEditor (IEditorPart editor)
  {
    this.editor = editor;
  }
}
