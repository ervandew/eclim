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
package org.eclim.eclipse;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.osgi.framework.BundleContext;

/**
 * The main plugin class.
 *
 * @author Eric Van Dewoestine
 */
public class EclimPlugin
  extends Plugin
{
  //The shared instance.
  private static EclimPlugin plugin;

  private static final String FILE_PREFIX = "file:";
  private static final String PLUGIN_XML = "plugin.xml";

  /**
   * The constructor.
   */
  public EclimPlugin ()
  {
    plugin = this;
  }

  @Override
  public void start(BundleContext context)
    throws Exception
  {
    super.start(context);
    URL url = FileLocator.toFileURL(getBundle().getResource(PLUGIN_XML));
    String home = url.toString();
    home = home.substring(
        FILE_PREFIX.length(), home.length() - PLUGIN_XML.length());
    // handle windows edge case
    home = home.replaceFirst("^/([A-Za-z]:)", "$1");
    System.setProperty("eclim.home", home);
  }

  @Override
  public void stop(BundleContext context)
    throws Exception
  {
    super.stop(context);
    plugin = null;
  }

  /**
   * Returns the shared instance.
   *
   * @return EclimPlugin instance.
   */
  public static EclimPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Gets the shell to use.
   *
   * @return The Shell.
   */
  public static Shell getShell()
  {
    Display display = Display.getDefault();
    Shell shell = display.getActiveShell();
    if (shell != null){
      return shell;
    }

    // should only be necessary for headed eclimd
    Shell[] shells = display.getShells();
    if (shells.length > 0){
      return shells[0];
    }

    // hopefully shouldn't happen
    return null;
  }
}
