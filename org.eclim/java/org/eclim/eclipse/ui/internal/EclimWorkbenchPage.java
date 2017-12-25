/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.WorkbenchException;

import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Extension to eclipse WorkbenchPage.
 *
 * @author Eric Van Dewoestine
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

  @Override
  public IEditorPart getActiveEditor()
  {
    return editor;
  }

  public void setActiveEditor(IEditorPart editor)
  {
    this.editor = editor;
  }
}
