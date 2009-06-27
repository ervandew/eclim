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

  /**
   * {@inheritDoc}
   * @see org.osgi.framework.BundleActivator#start(BundleContext)
   */
  public void start(BundleContext context)
    throws Exception
  {
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

    Bundle bundle = this.getBundle();
    try{
      PluginResources resources = (PluginResources)
        bundle.loadClass(resourceClass).newInstance();
      resources.initialize(bundle.getSymbolicName());
      Services.addPluginResources(resources);

      loadCommands(bundle, resources);
    }catch(Exception e){
      logger.error(
          "Error starting plugin: " + this.getBundle().getSymbolicName(), e);
    }
  }

  /**
   * {@inheritDoc}
   * @see org.osgi.framework.BundleActivator#stop(BundleContext)
   */
  public void stop(BundleContext context)
    throws Exception
  {
    super.stop(context);

    Services.removePluginResources(
        Services.getPluginResources(context.getBundle().getSymbolicName()));
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
      Set<String> commandClasses = db.getAnnotationIndex()
        .get(org.eclim.annotation.Command.class.getName());
      if(commandClasses != null){
        for (String commandClass : commandClasses){
          @SuppressWarnings("unchecked")
          Class<? extends Command> cclass = (Class<? extends Command>)
            classloader.loadClass(commandClass);
          resources.registerCommand(cclass);
        }
      }
    }catch(Exception e){
      logger.error("Unable to load commands.", e);
    }
  }

  /**
   * {@inheritDoc}
   * @see BundleListener#bundleChanged(BundleEvent)
   */
  public void bundleChanged(BundleEvent event)
  {
    Bundle bundle = event.getBundle();
    if (bundle.getState() == Bundle.ACTIVE &&
        this.getBundle().getSymbolicName().equals(bundle.getSymbolicName()))
    {
      this.activate(bundle.getBundleContext());
    }
  }
}
