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
package org.eclim.plugin.core;

import java.io.File;
import java.io.FilenameFilter;

import org.eclim.Services;

import org.eclim.eclipse.EclimDaemon;

import org.eclim.logging.Logger;

import org.eclim.plugin.Plugin;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;

/**
 * Plugin to load eclim core.
 *
 * @author Eric Van Dewoestine
 */
public class CorePlugin
  extends Plugin
  implements IResourceChangeListener
{
  private static final Logger logger = Logger.getLogger(CorePlugin.class);

  private String[] plugins;
  private boolean building;

  //The shared instance.
  private static CorePlugin plugin;

  public CorePlugin ()
  {
    plugin = this;
  }

  /**
   * Returns the shared instance.
   *
   * @return CorePlugin instance.
   */
  public static CorePlugin getDefault()
  {
    return plugin;
  }

  @Override
  public void activate(BundleContext context)
  {
    super.activate(context);

    logger.info("Loading eclim plugins...");
    String pluginsDir =
      System.getProperty("eclim.home") + File.separator + ".." + File.separator;

    String[] pluginDirs = new File(pluginsDir).list(new FilenameFilter(){
      public boolean accept(File dir, String name)
      {
        if (name.startsWith("org.eclim.") &&
            name.indexOf("core") == -1 &&
            name.indexOf("installer") == -1 &&
            name.indexOf("vimplugin") == -1)
        {
          return true;
        }
        return false;
      }
    });

    plugins = new String[pluginDirs.length];
    for (int ii = 0; ii < pluginDirs.length; ii++){
      plugins[ii] = pluginDirs[ii].substring(0, pluginDirs[ii].lastIndexOf('_'));
    }

    for(String plugin : plugins){
      logger.info("Loading plugin " + plugin);

      Bundle bundle = Platform.getBundle(plugin);
      if(bundle == null){
        String message = Services.getMessage("plugin.load.failed", plugin);
        logger.error(message);
        //throw new RuntimeException(message);
      }else{
        try{
          bundle.start(Bundle.START_TRANSIENT);
        }catch(BundleException be){
          logger.error("Failed to load plugin: " + bundle.getSymbolicName(), be);
        }
      }
    }

    ResourcesPlugin.getWorkspace().addResourceChangeListener(
        this, IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.POST_BUILD);

    logger.info("Plugins loaded.");
    EclimDaemon.getInstance().frameworkEvent(
        new FrameworkEvent(FrameworkEvent.INFO, getBundle(), null));
  }

  @Override
  public void stop(BundleContext context)
    throws Exception
  {
    super.stop(context);

    for(String plugin : plugins){
      logger.info("Stopping plugin " + plugin);

      Bundle bundle = Platform.getBundle(plugin);
      if(bundle != null){
        bundle.stop();
      }
    }

    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
  }

  @Override
  public void resourceChanged(IResourceChangeEvent event)
  {
    int type = event.getType();
    if (type == IResourceChangeEvent.PRE_BUILD){
      logger.debug("Received PRE_BUILD event.");
      building = true;
    }else if (type == IResourceChangeEvent.POST_BUILD){
      logger.debug("Received POST_BUILD event.");
      building = false;
    }
  }

  public boolean isBuildRunning()
  {
    return building;
  }
}
