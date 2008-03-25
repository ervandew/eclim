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
package org.eclim.plugin.pydev;

import org.osgi.framework.BundleContext;

/**
 * Installer plugin.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class Plugin
  extends org.eclipse.core.runtime.Plugin
{
  //The shared instance.
  private static Plugin plugin;

  /**
   * The constructor.
   */
  public Plugin ()
  {
    plugin = this;
  }

  /**
   * This method is called upon plug-in activation
   *
   * @param _context The bundle context.
   */
  public void start (BundleContext _context)
    throws Exception
  {
    super.start(_context);
  }

  /**
   * This method is called when the plug-in is stopped
   *
   * @param _context The bundle context.
   */
  public void stop (BundleContext _context)
    throws Exception
  {
    super.stop(_context);
    plugin = null;
  }

  /**
   * Returns the shared instance.
   */
  public static Plugin getDefault ()
  {
    return plugin;
  }
}
