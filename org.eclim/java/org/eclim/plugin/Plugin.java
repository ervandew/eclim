/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin;

import java.io.InputStream;

import java.net.URL;

import java.util.Properties;
import java.util.Set;

import org.eclim.Services;

import org.eclim.command.Command;

import org.eclim.logging.Logger;

import org.eclim.util.IOUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import org.scannotation.AnnotationDB;

/**
 * Activator for eclim plugins.
 *
 * @author Eric Van Dewoestine
 */
public class Plugin
  extends org.eclipse.core.runtime.Plugin
  implements BundleListener
{
  private static final Logger logger = Logger.getLogger(Plugin.class);

  //The shared instance.
  private static Plugin plugin;

  public Plugin ()
  {
    plugin = this;
  }

  /**
   * Returns the shared instance.
   *
   * @return Plugin instance.
   */
  public static Plugin getDefault()
  {
    return plugin;
  }

  @Override
  public void start(BundleContext context)
    throws Exception
  {
    logger.debug("{}: start", context.getBundle().getSymbolicName());
    super.start(context);

    // start is called at startup regardless of whether Bundle-ActivationPolicy
    // is lazy or not, but the bundle remains in the STARTING state and isn't
    // activated until needed.  So, to avoid loading eclim resouces at eclipse
    // startup, we listen for the bundle to be activated and then load our
    // resources.
    context.addBundleListener(this);
  }

  /**
   * Invoked when the bundle is activated by the bundle listener registered
   * during start(BundleContext).
   *
   * @param context the BundleContext.
   */
  public void activate(BundleContext context)
  {
    String name = context.getBundle().getSymbolicName();
    logger.debug("{}: activate", name);

    logger.debug("{}: loading plugin.properties", name);
    Properties properties = new Properties();
    InputStream in = null;
    try{
      in = context.getBundle().getResource("plugin.properties").openStream();
      properties.load(in);
    }catch(Exception e){
      logger.error("Unable to load plugin.properties.", e);
      //throw new RuntimeException(e);
    }finally{
      IOUtils.closeQuietly(in);
    }

    String resourceClass = properties.getProperty("eclim.plugin.resources");
    logger.debug("{}: loading resources: {}", name, resourceClass);

    Bundle bundle = this.getBundle();
    try{
      PluginResources resources = (PluginResources)
        bundle.loadClass(resourceClass).getDeclaredConstructor().newInstance();
      logger.debug("{}: initializing resources", name);
      resources.initialize(name);
      Services.addPluginResources(resources);

      loadCommands(bundle, resources);
    }catch(Exception e){
      logger.error("Error starting plugin: " + name, e);
    }
  }

  @Override
  public void stop(BundleContext context)
    throws Exception
  {
    super.stop(context);

    PluginResources resources = Services.removePluginResources(
        Services.getPluginResources(context.getBundle().getSymbolicName()));
    resources.close();
  }

  /**
   * Given plugin resources instance, finds and loads all commands.
   *
   * @param bundle The Bundle.
   * @param resources The PluginResources.
   */
  private void loadCommands(Bundle bundle, PluginResources resources)
  {
    String name = this.getBundle().getSymbolicName();
    try{
      Class<?> rclass = resources.getClass();
      ClassLoader classloader = rclass.getClassLoader();
      String resourceName = rclass.getName().replace('.', '/') + ".class";
      URL resource = classloader.getResource(resourceName);
      String url = resource.toString();
      url = url.substring(0, url.indexOf(resourceName));

      String jarName = resources.getName().substring("org.".length()) + ".jar";
      URL jarUrl = FileLocator.toFileURL(
          FileLocator.find(bundle, new Path(jarName), null));

      logger.debug("{}: loading commands", name);

      AnnotationDB db = new AnnotationDB();
      db.setScanClassAnnotations(true);
      db.setScanFieldAnnotations(false);
      db.setScanMethodAnnotations(false);
      db.setScanParameterAnnotations(false);
      db.scanArchives(jarUrl);
      Set<String> commandClasses = db.getAnnotationIndex()
        .get(org.eclim.annotation.Command.class.getName());
      if(commandClasses != null){
        for (String commandClass : commandClasses){
          logger.debug("{}: loading command: {}", name, commandClass);
          @SuppressWarnings("unchecked")
          Class<? extends Command> cclass = (Class<? extends Command>)
            classloader.loadClass(commandClass);
          resources.registerCommand(cclass);
        }
      }else{
        logger.debug("{}: no commands found", name);
      }
    }catch(Throwable t){
      logger.error("Unable to load commands.", t);
    }
  }

  @Override
  public void bundleChanged(BundleEvent event)
  {
    Bundle bundle = event.getBundle();
    if (this.getBundle().getSymbolicName().equals(bundle.getSymbolicName())){
      if (logger.isDebugEnabled()){
        String state = "unknown";
        switch(bundle.getState()){
          case Bundle.ACTIVE:
            state = "active";
            break;
          case Bundle.INSTALLED:
            state = "installed";
            break;
          case Bundle.RESOLVED:
            state = "resolved";
            break;
          case Bundle.STARTING:
            state = "starting";
            break;
          case Bundle.STOPPING:
            state = "starting";
            break;
          case Bundle.UNINSTALLED:
            state = "starting";
            break;
        }
        logger.debug("{}: bundleChanged: {}", bundle.getSymbolicName(), state);
      }

      if (bundle.getState() == Bundle.ACTIVE) {
        this.activate(bundle.getBundleContext());
      }
    }
  }
}
