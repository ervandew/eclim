/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.BindException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.logging.Logger;

import org.eclim.plugin.PluginResources;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.Platform;

import org.eclipse.core.runtime.adaptor.LocationManager;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import org.eclipse.osgi.internal.baseadaptor.AdaptorUtil;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

import com.martiansoftware.nailgun.NGContext;
import com.martiansoftware.nailgun.NGServer;

/**
 * Abstract base class containing shared functionality used by implementations
 * of an eclim application.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractEclimApplication
  implements IApplication, FrameworkListener
{
  private static final Logger logger =
    Logger.getLogger(AbstractEclimApplication.class);

  private static final String CORE = "org.eclim.core";
  private static AbstractEclimApplication instance;

  private String workspace;
  private NGServer server;
  private boolean starting;
  private boolean stopping;
  private boolean registered;

  /**
   * {@inheritDoc}
   * @see IApplication#start(IApplicationContext)
   */
  public Object start(IApplicationContext context)
    throws Exception
  {
    workspace = ResourcesPlugin
      .getWorkspace().getRoot().getRawLocation().toOSString().replace('\\', '/');
    logger.info("Workspace: " + workspace);

    starting = true;
    logger.info("Starting eclim...");
    instance = this;
    int exitCode = 0;

    String host = Services.getPluginResources("org.eclim")
      .getProperty("nailgun.server.host");
    String portString = Services.getPluginResources("org.eclim")
      .getProperty("nailgun.server.port");

    try{
      if (!onStart()){
        return EXIT_OK;
      }

      // load plugins.
      boolean pluginsLoaded = load();

      if (pluginsLoaded){
        // add shutdown hook.
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        // register that server is running to external processes.
        registered = registerInstance();

        // start nailgun
        int port = Integer.parseInt(portString);
        logger.info("Eclim Server Started on: " + host + ':' + port);
        InetAddress address = InetAddress.getByName(host);
        server = new NGServer(address, port, getExtensionClassLoader());
        server.setCaptureSystemStreams(false);
        starting = false;
        server.run();
      }else{
        exitCode = 1;
      }
    }catch(NumberFormatException nfe){
      logger.error("Error starting eclim:",
          new RuntimeException("Invalid port number: '" + portString + "'"));
      return new Integer(1);
    }catch(BindException be){
      logger.error("Error starting eclim on " + host + ':' + portString + ":", be);
      return new Integer(1);
    }catch(Throwable t){
      logger.error("Error starting eclim:", t);
      return new Integer(1);
    }finally{
      starting = false;
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
      if(server != null && server.isRunning()){
        server.shutdown(false /* exit vm */);
      }
    }catch(Exception e){
      logger.error("Error shutting down.", e);
    }
  }

  /**
   * Invoked during application startup, allowing subclasses to perform any
   * necessary startup initialization.
   *
   * @return true if the application should continue to start, false otherwise.
   */
  public boolean onStart()
    throws Exception
  {
    return true;
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
   * Determines if this application is in the process of starting.
   *
   * @return True if starting, false if stopped or finished starting.
   */
  public boolean isStarting()
  {
    return starting;
  }

  /**
   * Determines if this application is in the process of stopping.
   *
   * @return True if stopping, false if stopped or finished stopping.
   */
  public boolean isStopping()
  {
    return stopping;
  }

  /**
   * Determines if the underlying nailgun server is running or not.
   *
   * @return True if the nailgun server is running, false otherwise.
   */
  public boolean isRunning()
  {
    return server != null && server.isRunning();
  }

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
  private synchronized boolean load()
    throws Exception
  {
    logger.info("Loading plugin org.eclim");
    PluginResources defaultResources = Services.getPluginResources("org.eclim");
    defaultResources.registerCommand(ReloadCommand.class);

    logger.info("Loading plugin org.eclim.core");

    Bundle bundle = Platform.getBundle(CORE);
    if(bundle == null){
      String diagnosis = EclimPlugin.getDefault().diagnose(CORE);
      logger.error(Services.getMessage("plugin.load.failed", CORE, diagnosis));
      return false;
    }

    bundle.start();
    bundle.getBundleContext().addFrameworkListener(this);

    // wait up to 10 seconds for bundles to activate.
    wait(10000);

    return true;
  }

  /**
   * Shuts down the eclim server.
   */
  private synchronized void shutdown()
    throws Exception
  {
    if(!stopping){
      stopping = true;
      logger.info("Shutting down eclim...");

      logger.info("Stopping plugin " + CORE);
      Bundle bundle = Platform.getBundle(CORE);
      if (bundle != null){
        try{
          bundle.stop();
        }catch(IllegalStateException ise){
          // thrown because eclipse osgi BaseStorage attempt to register a
          // shutdown hook during shutdown.
        }
      }

      EclimPlugin plugin = EclimPlugin.getDefault();
      if(plugin != null){
        plugin.stop(null);
      }

      unregisterInstance();

      onStop();

      logger.info("Clean osgi config...");
      File osgiConfig = LocationManager.getOSGiConfigurationDir();
      AdaptorUtil.rm(osgiConfig);

      logger.info("Eclim stopped.");
    }
  }

  /**
   * Register the current instance in the eclimd instances file for use by vim.
   *
   * @return true if the instance was registered, false otherwise.
   */
  private boolean registerInstance()
    throws Exception
  {
    File instances = new File(FileUtils.concat(
          System.getProperty("user.home"), ".eclim/.eclimd_instances"));

    FileOutputStream out = null;
    try{
      List<String> entries = readInstances();
      if (entries == null){
        return false;
      }

      String port = Services.getPluginResources("org.eclim")
        .getProperty("nailgun.server.port");
      String instance = workspace + ':' + port;
      if (!entries.contains(instance)){
        entries.add(0, instance);
        out = new FileOutputStream(instances);
        IOUtils.writeLines(entries, out);
      }
      return true;
    }catch(IOException ioe){
      logger.error(
          "\nError writing to eclimd instances file: " + ioe.getMessage() +
          "\n" + instances);
    }finally{
      IOUtils.closeQuietly(out);
    }
    return false;
  }

  /**
   * Unregister the current instance in the eclimd instances file for use by vim.
   */
  private void unregisterInstance()
    throws Exception
  {
    if (!registered){
      return;
    }

    File instances = new File(FileUtils.concat(
          System.getProperty("user.home"), ".eclim/.eclimd_instances"));

    FileOutputStream out = null;
    try{
      List<String> entries = readInstances();
      if (entries == null){
        return;
      }

      String port = Services.getPluginResources("org.eclim")
        .getProperty("nailgun.server.port");
      String instance = workspace + ':' + port;
      entries.remove(instance);

      if (entries.size() == 0){
        if (!instances.delete()){
          logger.error("Error deleting eclimd instances file: " + instances);
        }
      }else{
        out = new FileOutputStream(instances);
        IOUtils.writeLines(entries, out);
      }
    }catch(IOException ioe){
      logger.error(
          "\nError writing to eclimd instances file: " + ioe.getMessage() +
          "\n" + instances);
      return;
    }finally{
      IOUtils.closeQuietly(out);
    }
  }

  private List<String> readInstances()
    throws Exception
  {
    File doteclim =
      new File(FileUtils.concat(System.getProperty("user.home"), ".eclim"));
    if (!doteclim.exists()){
      if (!doteclim.mkdirs()){
        logger.error("Error creating ~/.eclim directory: " + doteclim);
        return null;
      }
    }
    File instances = new File(FileUtils.concat(
          doteclim.getAbsolutePath(), ".eclimd_instances"));
    if (!instances.exists()){
      try{
        instances.createNewFile();
      }catch(IOException ioe){
        logger.error(
            "\nError creating eclimd instances file: " + ioe.getMessage() +
            "\n" + instances);
        return null;
      }
    }

    FileInputStream in = null;
    try{
      in = new FileInputStream(instances);
      List<String> entries = IOUtils.readLines(in);
      return entries;
    }finally{
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Builds the classloader used for third party nailgun extensions dropped into
   * eclim's ext dir.
   *
   * @return The classloader.
   */
  private ClassLoader getExtensionClassLoader()
    throws Exception
  {
    File extdir = new File(FileUtils.concat(Services.DOT_ECLIM, "resources/ext"));
    if (extdir.exists()){
      FileFilter filter = new FileFilter(){
        public boolean accept(File file){
          return file.isDirectory() || file.getName().endsWith(".jar");
        }
      };

      ArrayList<URL> urls = new ArrayList<URL>();
      listFileUrls(extdir, filter, urls);
      return new URLClassLoader(
          urls.toArray(new URL[urls.size()]),
          this.getClass().getClassLoader());
    }
    return null;
  }

  private void listFileUrls(File dir, FileFilter filter, ArrayList<URL> results)
    throws Exception
  {
    File[] files = dir.listFiles(filter);
    for (File file : files) {
      if(file.isFile()){
        results.add(file.toURL());
      }else{
        listFileUrls(file, filter, results);
      }
    }
  }

  /**
   * {@inheritDoc}
   * @see FrameworkListener#frameworkEvent(FrameworkEvent)
   */
  public synchronized void frameworkEvent(FrameworkEvent event)
  {
    // We are using a framework INFO event to announce when all the eclim
    // plugins bundles have been started (but not necessarily activated yet).
    Bundle bundle = event.getBundle();
    if (event.getType() == FrameworkEvent.INFO &&
        CORE.equals(bundle.getSymbolicName()))
    {
      logger.info("Loaded plugin org.eclim.core");
      notify();
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
    private NGContext context;

    /**
     * {@inheritDoc}
     * @see org.eclim.command.Command#execute(CommandLine)
     */
    public String execute(CommandLine commandLine)
      throws Exception
    {
      Bundle bundle = Platform.getBundle(CORE);
      bundle.update();
      bundle.start();
      return Services.getMessage("plugins.reloaded");
    }

    /**
     * {@inheritDoc}
     * @see org.eclim.command.Command#getContext()
     */
    public NGContext getContext()
    {
      return context;
    }

    /**
     * {@inheritDoc}
     * @see org.eclim.command.Command#setContext(NGContext)
     */
    public void setContext(NGContext context)
    {
      this.context = context;
    }
  }
}
