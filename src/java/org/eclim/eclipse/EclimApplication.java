/**
 * Copyright (c) 2004 - 2005
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

import java.util.Hashtable;
import java.util.Properties;

import com.martiansoftware.nailgun.NGServer;

import org.apache.commons.io.IOUtils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.command.Command;
import org.eclim.command.admin.ShutdownCommand;

import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.PluginResources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.widgets.EclimDisplay;

import org.osgi.framework.Bundle;

/**
 * This class controls all aspects of the application's execution
 */
public class EclimApplication
  implements IPlatformRunnable
{
  private static final Logger logger = Logger.getLogger(EclimApplication.class);

  /**
   * Runs this runnable with the supplied args.
   *
   * @param _args The arguments (typically a String[]).
   * @return The result (returned Integer is treated as exit code).
   */
  public Object run (Object _args)
    throws Exception
  {
    logger.info("Starting eclim...");
    try{
      // create the eclipse workbench.
      org.eclipse.ui.PlatformUI.createAndRunWorkbench(
          new EclimDisplay(),//org.eclipse.ui.PlatformUI.createDisplay()),
          new WorkbenchAdvisor());

      NGServer server = (NGServer)Services.getService(NGServer.class);

      IProject[] projects =
        ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for(int ii = 0; ii < projects.length; ii++){
        logger.info("Opening project '{}'", projects[ii].getName());
        projects[ii].open(null);
      }

      // load plugins.
      loadPlugins();

      // start nail gun
      logger.info("Eclim Server Started.");
      server.run();
    }catch(Throwable t){
      logger.error("Error starting eclim:", t);
      return Integer.valueOf(1);
    }

    logger.info("Shutting down eclim...");
    Services.close();
    closePlugins();

    logger.info("Eclim stopped.");
    return Integer.valueOf(0);
  }

  /**
   * Shutdown core plugins.
   */
  protected void closePlugins ()
    throws Exception
  {
    logger.info("Shutting down core plugins.");
    EclimPlugin.getDefault().stop(null);
    ResourcesPlugin.getWorkspace().save(true, null);
    // when shutdown normally, eclipse will handle this.
    /*ResourcesPlugin.getPlugin().shutdown();
      ResourcesPlugin.getPlugin().stop(null);*/
  }

  /**
   * Loads any eclim plugins found.
   */
  protected void loadPlugins ()
  {
    logger.info("Loading eclim plugins...");
    String pluginsDir =
      System.getProperty("eclim.home") + File.separator + ".." + File.separator;

    File root = new File(pluginsDir);
    String[] plugins = root.list(new FilenameFilter(){
      public boolean accept (File _dir, String _name){
        if(_name.startsWith("org.eclim.")){
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
}
