/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import java.net.URL;

import java.util.Properties;

import com.martiansoftware.nailgun.NGServer;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.PluginResources;

import org.eclim.util.IOUtils;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.Platform;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import org.eclipse.swt.widgets.EclimDisplay;

import org.osgi.framework.Bundle;

/**
 * This class controls all aspects of the application's execution
 */
public class EclimApplication
  implements IApplication
{
  private static final Logger logger = Logger.getLogger(EclimApplication.class);

  private boolean shuttingDown = false;

  /**
   * {@inheritDoc}
   * @see IApplication#start(IApplicationContext)
   */
  public Object start (IApplicationContext _context)
    throws Exception
  {
    logger.info("Starting eclim...");
    try{
      // create the eclipse workbench.
      org.eclipse.ui.PlatformUI.createAndRunWorkbench(
          new EclimDisplay(),//org.eclipse.ui.PlatformUI.createDisplay()),
          new WorkbenchAdvisor());

      NGServer server = (NGServer)Services.getService(NGServer.class);

      // load plugins.
      loadPlugins();

      // add shutdown hook.
      Runtime.getRuntime().addShutdownHook(new ShutdownHook());

      // start nail gun
      logger.info("Eclim Server Started.");
      server.run();
    }catch(Throwable t){
      logger.error("Error starting eclim:", t);
      return new Integer(1);
    }

    shutdown();
    return new Integer(0);
  }

  /**
   * {@inheritDoc}
   * @see IApplication#stop()
   */
  public void stop ()
  {
    try{
      shutdown();
    }catch(Exception e){
      logger.error("Error shutting down.", e);
    }
  }

  /**
   * Shuts down the eclim server.
   */
  private synchronized void shutdown ()
    throws Exception
  {
    if(!shuttingDown){
      shuttingDown = true;
      logger.info("Shutting down eclim...");

      // Saving workspace MUST be before closing of service contexts.
      saveWorkspace();
      Services.close();

      EclimPlugin plugin = EclimPlugin.getDefault();
      if(plugin != null){
        plugin.stop(null);
      }

      // when shutdown normally, eclipse will handle this.
      /*ResourcesPlugin.getPlugin().shutdown();
        ResourcesPlugin.getPlugin().stop(null);*/

      logger.info("Eclim stopped.");
    }
  }

  /**
   * Save the workspace.
   */
  private void saveWorkspace ()
    throws Exception
  {
    logger.info("Saving workspace...");

    try{
      ResourcesPlugin.getWorkspace().save(true, null);
    }catch(Exception e){
      logger.warn("Error saving workspace.", e);
    }
    logger.info("Workspace saved.");
  }

  /**
   * Loads any eclim plugins found.
   */
  private void loadPlugins ()
  {
    logger.info("Loading eclim plugins...");
    String pluginsDir =
      System.getProperty("eclim.home") + File.separator + ".." + File.separator;

    File root = new File(pluginsDir);
    String[] plugins = root.list(new FilenameFilter(){
      public boolean accept (File _dir, String _name){
        if(_name.startsWith("org.eclim.") &&
          _name.indexOf("installer") == -1)
        {
          return true;
        }
        return false;
      }
    });

    for(int ii = 0; ii < plugins.length; ii++){
      Properties properties = new Properties();
      FileInputStream in = null;
      try{
        in = new FileInputStream(
            pluginsDir + plugins[ii] + File.separator + "plugin.properties");
        properties.load(in);
      }catch(Exception e){
        throw new RuntimeException(e);
      }finally{
        IOUtils.closeQuietly(in);
      }

      String resourceClass = properties.getProperty("eclim.plugin.resources");
      String resourceFile = properties.getProperty("eclim.plugin.resources.file");
      String pluginName = plugins[ii].substring(0, plugins[ii].lastIndexOf('_'));

      logger.info("Loading plugin " + pluginName);

      Bundle bundle = Platform.getBundle(pluginName);
      if(bundle == null){
        throw new RuntimeException(
            "Could not load bundle for plugin '" + plugins[ii] + "'");
      }

      try{
        bundle.start();

        PluginResources resources = (PluginResources)
          bundle.loadClass(resourceClass).newInstance();
        if(resources instanceof AbstractPluginResources){
          URL resourceUrl = bundle.getResource(resourceFile);
          ((AbstractPluginResources)resources)
            .initialize(pluginName, resourceUrl);
        }
        Services.addPluginResources(resources);
      }catch(Exception e){
        throw new RuntimeException(e);
      }
      logger.info("Loaded plugin {}.", plugins[ii]);
    }
  }

  /**
   * Shutdown hook for non-typical shutdown.
   */
  private class ShutdownHook
    extends Thread
  {
    /**
     * Runs the shutdown hook.
     */
    public void run ()
    {
      try{
        shutdown();
      }catch(Exception e){
        logger.error("Error running shutdown hook.", e);
      }
    }
  }
}
