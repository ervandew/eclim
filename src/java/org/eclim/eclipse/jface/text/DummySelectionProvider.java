/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.eclipse.jface.text;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Dummy ISelectionProvider implementation that provides basic functionality for
 * eclipse classes that require an ITextViewer.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class DummySelectionProvider
  implements ISelectionProvider
{
  private ISelection selection;

  public DummySelectionProvider (ISelection _selection)
  {
    selection = _selection;
  }

  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
  }

  public ISelection getSelection()
  {
    return selection;
  }

  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
  }

  public void setSelection(ISelection selection)
  {
    this.selection = selection;
  }
}
