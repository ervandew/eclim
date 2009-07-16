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
package org.eclim.plugin.cdt;

import org.eclim.eclipse.AbstractEclimApplication;

import org.eclim.logging.Logger;

import org.eclim.plugin.Plugin;

import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Plugin class to load eclim cdt required resources on startup.
 *
 * @author Eric Van Dewoestine
 */
public class CdtPlugin
  extends Plugin
{
  private static final Logger logger = Logger.getLogger(CdtPlugin.class);

  /**
   * {@inheritDoc}
   * @see Plugin#start(BundleContext)
   */
  @Override
  public void start(BundleContext context)
    throws Exception
  {
    super.start(context);

    // force a couple cdt ui plugins to start so that we can access their
    // internal classes.  This seems to only be necessary on the non-classic
    // eclipse distributions.
    AbstractEclimApplication app = AbstractEclimApplication.getInstance();
    if (app != null && !app.isHeaded()){
      String[] names =
        {"org.eclipse.cdt.ui", "org.eclipse.cdt.managedbuilder.ui"};
      for (String name : names){
        Bundle bundle = Platform.getBundle(name);
        try{
          bundle.start();
        }catch(Exception e){
          logger.warn("Failed to start " + name);
        }
      }
    }
  }
}
