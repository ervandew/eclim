/**
 * Copyright (C) 2011  Eric Van Dewoestine
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
package org.eclim.eclipse;

import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

public class EclimClasspathContainer
  implements IClasspathContainer
{
  private IPath path;
  private IClasspathEntry[] entries;

  public EclimClasspathContainer(IPath path, IClasspathEntry[] entries)
  {
    this.path = path;
    this.entries = entries;
  }

  /**
   * {@inheritDoc}
   * @see IClasspathContainer#getClasspathEntries()
   */
  public IClasspathEntry[] getClasspathEntries()
  {
    return entries;
  }

  /**
   * {@inheritDoc}
   * @see IClasspathContainer#getDescription()
   */
  public String getDescription()
  {
    return "eclim dependencies";
  }

  /**
   * {@inheritDoc}
   * @see IClasspathContainer#getKind()
   */
  public int getKind()
  {
    return IClasspathContainer.K_DEFAULT_SYSTEM;
  }

  /**
   * {@inheritDoc}
   * @see IClasspathContainer#getPath()
   */
  public IPath getPath()
  {
    return path;
  }
}
