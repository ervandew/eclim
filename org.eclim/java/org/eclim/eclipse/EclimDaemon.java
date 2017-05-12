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
package org.eclim.eclipse;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.net.BindException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclim.Services;

import org.eclim.command.ReloadCommand;
import org.eclim.http.HTTPServer;
import org.eclim.logging.Logger;

import org.eclim.plugin.PluginResources;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

import com.google.gson.Gson;

import com.martiansoftware.nailgun.NGServer;

/**
 * Class which handles starting/stopping of the eclim nailgun daemon along with
 * the loading/unloading of eclim plugins.
 *
 * @author Eric Van Dewoestine
 */
public class EclimDaemon
  implements FrameworkListener
{
  private static final Logger logger =
    Logger.getLogger(EclimDaemon.class);

  private static String BASE = "org.eclim";
  private static String CORE = "org.eclim.core";
  private static EclimDaemon instance = new EclimDaemon();

  private boolean started;
  private boolean starting;
  private boolean stopping;
  private NGServer server;

  private HTTPServer httpServer;

  private EclimDaemon()
  {
  }

  public static EclimDaemon getInstance()
  {
    return instance;
  }

  public void start()
  {
    if (started || starting){
        return;
    }

    String host = Services.getPluginResources("org.eclim")
      .getProperty("nailgun.server.host");
    String portString = Services.getPluginResources("org.eclim")
      .getProperty("nailgun.server.port");
    try{
      String workspace = getWorkspace();
      logger.info("Workspace: " + workspace);

      String home = getHome();
      logger.info("Home: " + home);

      starting = true;
      logger.info("Starting eclim...");

      int port = Integer.parseInt(portString);

      InetAddress address = InetAddress.getByName(host);
      server = new NGServer(address, port, getExtensionClassLoader());
      server.setCaptureSystemStreams(false);

      startHTTPServer();

      logger.info("Loading plugin org.eclim");
      PluginResources defaultResources = Services.getPluginResources("org.eclim");
      defaultResources.registerCommand(ReloadCommand.class);

      logger.info("Loading plugin org.eclim.core");

      Bundle bundle = Platform.getBundle(CORE);
      if(bundle == null){
        logger.error(Services.getMessage("plugin.load.failed", CORE));
        return;
      }

      bundle.start(Bundle.START_TRANSIENT);
      bundle.getBundleContext().addFrameworkListener(this);

      // wait up to 10 seconds for bundles to activate.
      synchronized(this){
        wait(10000);
      }

      // headless
      if (EclimApplication.isEnabled()){
        logger.info("Waiting on running jobs before starting eclimd...");
        long timeout = System.currentTimeMillis() + (30 * 1000);
        IJobManager manager = Job.getJobManager();
        while(System.currentTimeMillis() < timeout &&
          jobsRunning(manager))
        {
          try{
            Thread.sleep(1000);
          }catch(InterruptedException ie){
            // ignore
          }
        }
      }

      starting = false;
      started = true;
      logger.info("Eclim Server Started on: {}:{}", host, port);
      registerInstance(home, workspace, port);
      server.run();
    }catch(NumberFormatException nfe){
      logger.error("Error starting eclim:",
          new RuntimeException("Invalid port number: '" + portString + "'"));
    }catch(BindException be){
      logger.error("Error starting eclim on {}:{}", host, portString, be);
    }catch(Throwable t){
      logger.error("Error starting eclim:", t);
    }finally{
      try{
        unregisterInstance();
      }catch(Exception e){
        throw new RuntimeException(e);
      }
      starting = false;
      started = false;
    }
  }

  private void startHTTPServer()
      throws IOException
  {
    if (httpServerEnabled()){
      String httpServerPort = Services.getPluginResources(BASE)
          .getProperty("http.server.port", "9998");
      String host = Services.getPluginResources(BASE)
        .getProperty("http.server.host", "localhost");
      httpServer = new HTTPServer();
      httpServer.start(host, Integer.parseInt(httpServerPort));
    }
  }

  private boolean httpServerEnabled()
  {
    String httpServerEnabled = Services.getPluginResources("org.eclim")
        .getProperty("http.server.enabled");
    return "true".equals(httpServerEnabled);
  }

  public void stop()
    throws Exception
  {
    if(started && !stopping){
      try{
        stopping = true;
        logger.info("Shutting down eclim...");

        if (httpServer != null) {
          httpServer.stop();
        }

        if(server != null && server.isRunning()){
          server.shutdown(false /* exit vm */);
        }

        unregisterInstance();

        logger.info("Stopping plugin " + CORE);
        Bundle bundle = Platform.getBundle(CORE);
        if (bundle != null){
          try{
            bundle.stop();
          }catch(IllegalStateException ise){
            // thrown because eclipse osgi BaseStorage attempts to register a
            // shutdown hook during shutdown.
          }
        }

        logger.info("Eclim stopped.");
      }finally{
        stopping = false;
      }
    }
  }

  /**
   * Register the current instance in the eclimd instances file for use by vim.
   */
  private void registerInstance(String home, String workspace, int port)
    throws Exception
  {
    File instances = new File(FileUtils.concat(
          System.getProperty("user.home"), ".eclim/.eclimd_instances"));

    Gson gson = new Gson();
    FileWriter out = null;
    try{
      List<Instance> entries = readInstances();
      if (entries != null){
        boolean updated = false;

        // remove any stale entries
        for (Iterator<Instance> i = entries.iterator(); i.hasNext();){
          Instance entry = i.next();
          if (!entry.exists()){
            i.remove();
            updated = true;
            logger.info("Removed stale instance entry: " +
                entry.home + ':' + entry.port);
          }
        }

        // register new instance
        Instance instance = new Instance(home, workspace, port);
        if (!entries.contains(instance)){
          entries.add(instance);
          updated = true;
        }

        // update the instances file
        if (updated){
          out = new FileWriter(instances);
          for (Instance entry : entries) {
            out.write(gson.toJson(entry) + '\n');
          }
        }
      }
    }catch(IOException ioe){
      logger.error(
          "\nError writing to eclimd instances file: " + ioe.getMessage() +
          "\n" + instances);
    }finally{
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * Unregister the current instance in the eclimd instances file for use by vim.
   */
  private synchronized void unregisterInstance()
    throws Exception
  {
    File instances = new File(FileUtils.concat(
          System.getProperty("user.home"), ".eclim/.eclimd_instances"));

    Gson gson = new Gson();
    FileWriter out = null;
    try{
      List<Instance> entries = readInstances();
      if (entries == null){
        return;
      }

      Instance instance = new Instance(getHome(), getWorkspace(), getPort());
      entries.remove(instance);

      if (entries.size() == 0){
        if (!instances.delete()){
          logger.error("Error deleting eclimd instances file: " + instances);
        }
      }else{
        out = new FileWriter(instances);
        for (Instance entry : entries) {
          out.write(gson.toJson(entry) + '\n');
        }
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

  @SuppressWarnings("unchecked")
  private List<Instance> readInstances()
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

    Gson gson = new Gson();
    FileInputStream in = null;
    try{
      in = new FileInputStream(instances);
      List<String> lines = IOUtils.readLines(in);
      List<Instance> entries = new ArrayList<Instance>();
      for (String line : lines){
        if (!line.startsWith("{")) {
          continue;
        }
        entries.add(gson.fromJson(line, Instance.class));
      }
      return entries;
    }finally{
      IOUtils.closeQuietly(in);
    }
  }

  private String getWorkspace()
  {
    return ResourcesPlugin.getWorkspace()
      .getRoot().getRawLocation().toOSString().replace('\\', '/');
  }

  private String getHome()
    throws IOException
  {
    Bundle bundle = Platform.getBundle(BASE);
    IPath p = Path.fromOSString(FileLocator.getBundleFile(bundle).getPath());
    return p.addTrailingSeparator().toOSString();
  }

  private int getPort()
  {
    String portString = Services.getPluginResources("org.eclim")
      .getProperty("nailgun.server.port");
    return Integer.parseInt(portString);
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
        results.add(file.toURI().toURL());
      }else{
        listFileUrls(file, filter, results);
      }
    }
  }

  @Override
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

  private boolean jobsRunning(IJobManager manager)
  {
    Job[] jobs = manager.find(null);
    for (Job job : jobs){
      if (job.getState() == Job.RUNNING){
        logger.debug("Waiting on job: " + job);
        return true;
      }
    }
    logger.info("Jobs finished.");
    return false;
  }

  @SuppressWarnings("unused")
  private class Instance
  {
    private String home;
    private String workspace;
    private int port;

    public Instance(String home, String workspace, int port)
    {
      this.home = home;
      this.workspace = workspace;
      this.port = port;
    }

    public boolean exists()
    {
      return new File(home).exists();
    }

    public boolean equals(Object other)
    {
      if (!(other instanceof Instance)){
        return false;
      }
      Instance otheri = (Instance)other;
      return workspace.equals(otheri.workspace) &&
        home.equals(otheri.home) &&
        port == otheri.port;
    }
  }
}
