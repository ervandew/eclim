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
import java.io.IOException;

import com.martiansoftware.nailgun.NGServer;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.logging.Logger;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.runtime.Platform;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import org.osgi.framework.Bundle;

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
    int exitCode = 0;
    try{
      onStart();

      // load plugins.
      boolean pluginsLoaded = load();

      if (pluginsLoaded){
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
      }else{
        exitCode = 1;
      }
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
    return new Integer(exitCode);
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
   * Loads the core bundle which in turn loads the eclim plugins.
   */
  private boolean load()
    throws Exception
  {
    logger.info("Loading plugin org.eclim");
    Services.DefaultPluginResources defaultResources =
      new Services.DefaultPluginResources();
    defaultResources.initialize("org.eclim");
    defaultResources.registerCommand(ReloadCommand.class);

    logger.info("Loading plugin org.eclim.core");

    Bundle bundle = Platform.getBundle("org.eclim.core");
    if(bundle == null){
      String diagnoses = EclimPlugin.getDefault().diagnose("org.eclim.core");
      logger.error(Services.getMessage(
            "plugin.load.failed", "org.eclim.core", diagnoses));
      return false;
    }

    bundle.start();

    logger.info("Loaded plugin org.eclim.core");
    return true;
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

  @Command(name = "reload")
  public static class ReloadCommand
    implements org.eclim.command.Command
  {
    /**
     * {@inheritDoc}
     * @see org.eclim.command.Command#execute(CommandLine)
     */
    public String execute(CommandLine commandLine)
      throws Exception
    {
      Bundle bundle = Platform.getBundle("org.eclim.core");
      bundle.update();
      bundle.start();
      return "";
    }
  }
}
