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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import java.net.URL;

import java.util.Properties;

import com.martiansoftware.nailgun.NGServer;

import java.util.Set;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.logging.Logger;

import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.PluginResources;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import org.scannotation.AnnotationDB;

/**
 * Abstract base class containing shared functionality used by implementations
 * of an eclim application.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractEclimApplication
  implements IApplication
{
  private static final Logger logger =
    Logger.getLogger(AbstractEclimApplication.class);
  private static AbstractEclimApplication instance;

  private NGServer server;
  private boolean shuttingDown = false;

  /**
   * {@inheritDoc}
   * @see IApplication#start(IApplicationContext)
   */
  public Object start(IApplicationContext context)
    throws Exception
  {
    logger.info("Starting eclim...");
    instance = this;
    try{
      onStart();

      // load plugins.
      loadPlugins();

      // add shutdown hook.
      Runtime.getRuntime().addShutdownHook(new ShutdownHook());

      // create marker file indicating that eclimd is up
      File marker = new File(
          FileUtils.concat(System.getProperty("eclim.home"), ".available"));
      try{
        marker.createNewFile();
        marker.deleteOnExit();
      }catch(IOException ioe){
        logger.error(
            "\nError creating eclimd marker file: " + ioe.getMessage() +
            "\n" + marker);
      }

      // start nailgun
      String portString = Services.getPluginResources("org.eclim")
        .getProperty("nailgun.server.port");
      int port = Integer.parseInt(portString);
      logger.info("Eclim Server Started on port " + port + '.');
      server = new NGServer(null, port);
      server.run();
    }catch(NumberFormatException nfe){
      String p = Services.getPluginResources("org.eclim")
        .getProperty("nailgun.server.port");
      logger.error("Error starting eclim:",
          new RuntimeException("Invalid port number: '" + p + "'"));
      return new Integer(1);
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
  public void stop()
  {
    try{
      shutdown();
      if(server.isRunning()){
        server.shutdown(false /* exit vm */);
      }
    }catch(Exception e){
      logger.error("Error shutting down.", e);
    }
  }

  /**
   * Invoked during application startup, allowing subclasses to perform any
   * necessary startup initialization.
   */
  public void onStart()
    throws Exception
  {
  }

  /**
   * Invoked during application shutdown, allowing subclasses to perform any
   * necessary shutdown cleanup.
   */
  public void onStop()
    throws Exception
  {
  }

  /**
   * Test for "headed" environment.
   *
   * @return true if running in "headed" environment.
   */
  public abstract boolean isHeaded();

  /**
   * Gets the running instance of this application.
   *
   * @return The AbstractEclimApplication instance.
   */
  public static AbstractEclimApplication getInstance()
  {
    return instance;
  }

  /**
   * Shuts down the eclim server.
   */
  private synchronized void shutdown()
    throws Exception
  {
    if(!shuttingDown){
      shuttingDown = true;
      logger.info("Shutting down eclim...");

      onStop();
      Services.close();

      EclimPlugin plugin = EclimPlugin.getDefault();
      if(plugin != null){
        plugin.stop(null);
      }

      logger.info("Eclim stopped.");
    }
  }

  /**
   * Loads any eclim plugins found.
   */
  private void loadPlugins()
  {
    logger.info("Loading plugin org.eclim");

    Bundle bundle = Platform.getBundle("org.eclim");

    // should only happen when restarting eclimd inside of eclipse.
    if (bundle.getState() != Bundle.STARTING ||
        bundle.getState() != Bundle.ACTIVE)
    {
      try{
        bundle.start();
      }catch(BundleException be){
        logger.error("Error loading org.eclim bundle.", be);
      }
    }

    Services.DefaultPluginResources defaultResources =
      new Services.DefaultPluginResources();
    defaultResources.initialize("org.eclim");
    loadCommands(bundle, defaultResources);

    logger.info("Loading eclim plugins...");
    String pluginsDir =
      System.getProperty("eclim.home") + File.separator + ".." + File.separator;

    File root = new File(pluginsDir);
    String[] plugins = root.list(new FilenameFilter(){
      public boolean accept(File dir, String name)
      {
        if(name.startsWith("org.eclim.") &&
          name.indexOf("installer") == -1 &&
          name.indexOf("vimplugin") == -1){
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
      String pluginName = plugins[ii].substring(0, plugins[ii].lastIndexOf('_'));

      logger.info("Loading plugin " + pluginName);

      bundle = Platform.getBundle(pluginName);
      if(bundle == null){
        IPath log = ResourcesPlugin.getWorkspace().getRoot().getRawLocation()
          .append(".metadata").append(".log");
        throw new RuntimeException(
            "Could not load bundle for plugin '" + plugins[ii] + "'\n" +
            "Please see " + log.toOSString() + " for more info.");
      }

      try{
        bundle.start();

        PluginResources resources = (PluginResources)
          bundle.loadClass(resourceClass).newInstance();
        if(resources instanceof AbstractPluginResources){
          ((AbstractPluginResources)resources).initialize(pluginName);
        }

        loadCommands(bundle, resources);
      }catch(Exception e){
        throw new RuntimeException(e);
      }
      logger.info("Loaded plugin {}.", plugins[ii]);
    }
  }

  /**
   * Given plugin resources instance, finds and loads all commands.
   *
   * @param bundle The Bundle.
   * @param resources The PluginResources.
   */
  private void loadCommands(Bundle bundle, PluginResources resources)
  {
    try{
      Class<?> rclass = resources.getClass();
      ClassLoader classloader = rclass.getClassLoader();
      String name = rclass.getName().replace('.', '/') + ".class";
      URL resource = classloader.getResource(name);
      String url = resource.toString();
      url = url.substring(0, url.indexOf(name));

      String jarName = resources.getName().substring("org.".length()) + ".jar";
      URL jarUrl = FileLocator.toFileURL(
          FileLocator.find(bundle, new Path(jarName), null));

      AnnotationDB db = new AnnotationDB();
      db.setScanClassAnnotations(true);
      db.setScanFieldAnnotations(false);
      db.setScanMethodAnnotations(false);
      db.setScanParameterAnnotations(false);
      db.scanArchives(jarUrl);
      Set<String> commandClasses =
        db.getAnnotationIndex().get(Command.class.getName());
      if(commandClasses != null){
        for (String commandClass : commandClasses){
          Class cclass = classloader.loadClass(commandClass);
          resources.registerCommand(cclass);
        }
      }
    }catch(Exception e){
      logger.error("Unable to load commands.", e);
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
    public void run()
    {
      try{
        shutdown();
      }catch(Exception e){
        logger.error("Error running shutdown hook.", e);
      }
    }
  }
}
