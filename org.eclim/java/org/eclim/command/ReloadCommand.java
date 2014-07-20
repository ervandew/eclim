/**
 * Copyright (C) 2012 - 2014  Eric Van Dewoestine
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
package org.eclim.command;

import java.util.ArrayList;

import org.eclim.Services;

import org.eclim.plugin.PluginResources;

import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkListener;

import org.osgi.framework.wiring.FrameworkWiring;

import com.martiansoftware.nailgun.NGContext;

/**
 * Command to reload eclim core and language bundles.
 *
 * @author Eric Van Dewoestine
 */
@org.eclim.annotation.Command(name = "reload")
public class ReloadCommand
  implements Command
{
  private NGContext context;

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    Bundle system = Platform.getBundle("system.bundle");
    FrameworkWiring framework =
      (FrameworkWiring)system.adapt(FrameworkWiring.class);

    ArrayList<Bundle> bundles = new ArrayList<Bundle>();
    bundles.add(Platform.getBundle("org.eclim.core"));
    framework.refreshBundles(bundles, new FrameworkListener[0]);

    // avoid Services.getMessage due to race condition on reload which can
    // result in a ConcurrentModificationException.
    PluginResources resources = Services.getPluginResources("org.eclim");
    return resources.getMessage("plugins.reloaded");
  }

  @Override
  public NGContext getContext()
  {
    return context;
  }

  @Override
  public void setContext(NGContext context)
  {
    this.context = context;
  }

  @Override
  public void cleanup(CommandLine commandLine)
  {
  }
}
