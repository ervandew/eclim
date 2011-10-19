/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

/**
 * Dummy workbench advisor that allows the workbench to be created, but never
 * shown.
 *
 * @author Eric Van Dewoestine
 */
public class WorkbenchAdvisor
  extends org.eclipse.ui.application.WorkbenchAdvisor
{
  /**
   * {@inheritDoc}
   */
  public String getInitialWindowPerspectiveId()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean openWindows()
  {
    return true;
  }
}
