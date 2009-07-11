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

/**
 * Extension of default Shell class for running headless eclipse.
 *
 * @author Eric Van Dewoestine
 */
public class EclimShell
  extends Shell
{
  private boolean enabled;

  /**
   * @see org.eclipse.swt.widgets.Shell#Shell(Display)
   */
  public EclimShell(Display display)
  {
    super(display);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.swt.widgets.Shell#isEnabled()
   */
  @Override
  public boolean isEnabled()
  {
    return this.enabled;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.swt.widgets.Shell#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean enabled)
  {
    // override super class to avoid deadlock on windows.
    this.enabled = enabled;
  }
}
