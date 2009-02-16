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
package org.eclim.eclipse;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.osgi.framework.BundleContext;

/**
 * The main plugin class.
 */
public class EclimPlugin
  extends Plugin
{
  //The shared instance.
  private static EclimPlugin plugin;

  private static Shell shell;

  private static final String FILE_PREFIX = "file:";
  private static final String PLUGIN_XML = "plugin.xml";

  /**
   * The constructor.
   */
  public EclimPlugin ()
  {
    plugin = this;
  }

  /**
   * This method is called upon plug-in activation
   *
   * @param context The bundle context.
   */
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

  /**
   * This method is called when the plug-in is stopped
   *
   * @param context The bundle context.
   */
  public void stop(BundleContext context)
    throws Exception
  {
    super.stop(context);
    plugin = null;
  }

  /**
   * Returns the shared instance.
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
    if(shell == null){
      final Display display = Display.getDefault();
      final Shell[] result = new Shell[1];
      // obtaining via synExec required when running inside of headed eclipse.
      display.syncExec(new Runnable(){
        public void run()
        {
          result[0] = new Shell(display);
        }
      });
      shell = result[0];
    }
    return shell;
  }
}
