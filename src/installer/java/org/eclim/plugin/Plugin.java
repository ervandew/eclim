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
package org.eclim.plugin;

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
