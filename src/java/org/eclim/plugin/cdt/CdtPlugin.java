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
  /**
   * {@inheritDoc}
   * @see Plugin#start(BundleContext)
   */
  @Override
  public void start(BundleContext context)
    throws Exception
  {
    super.start(context);
    // force the cdt ui plugin to start so that we can access its internal
    // classes.  This seems to only be necessary on the non-classic eclipse
    // distributions.
    Bundle bundle = Platform.getBundle("org.eclipse.cdt.ui");
    bundle.start();
  }
}
