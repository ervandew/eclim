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

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.InvocationTargetException;

import java.net.URL;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.eclim.Services;

import org.eclim.command.Command;

import org.eclim.logging.Logger;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.runtime.FileLocator;

/**
 * Abstract implementation of {@link PluginResources}.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractPluginResources
  implements PluginResources
{
  private static final Logger logger =
    Logger.getLogger(AbstractPluginResources.class);

  private static final String PLUGIN_PROPERTIES = "/plugin.properties";

  private String name;
  private String pluginName;
  private Properties properties;
  private ResourceBundle bundle;
  private List<String> missingResources = new ArrayList<String>();

  private HashMap<String, Class<? extends Command>> commands =
    new HashMap<String, Class<? extends Command>>();

  /**
   * Initializes this instance.
   *
   * @param name The plugin name.
   */
  public void initialize(String name)
  {
    this.name = name;
    int index = name.lastIndexOf('.');
    pluginName = index != -1 ? name.substring(index + 1) : name;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public Collection<? extends Class<? extends Command>> getCommandClasses()
  {
    return commands.values();
  }

  @Override
  public Command getCommand(String name)
  {
    if(!containsCommand(name)){
      throw new RuntimeException(
          Services.getMessage("command.not.found", name));
    }
    Class<? extends Command> cc = commands.get(name);
    try{
      return cc.getDeclaredConstructor().newInstance();
    }catch(
        IllegalAccessException |
        InstantiationException |
        InvocationTargetException |
        NoSuchMethodException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean containsCommand(String name)
  {
    return commands.containsKey(name);
  }

  @Override
  public String getMessage(String key, Object... args)
  {
    ResourceBundle bundle = getResourceBundle();
    String message = bundle.getString(key);
    return MessageFormat.format(message, args);
  }

  @Override
  public ResourceBundle getResourceBundle()
  {
    if (bundle == null){
      bundle = ResourceBundle.getBundle(
          getBundleBaseName(), Locale.getDefault(), getClass().getClassLoader());
    }
    return bundle;
  }

  @Override
  public String getProperty(String name)
  {
    return getProperty(name, null);
  }

  @Override
  public String getProperty(String name, String defaultValue)
  {
    if (properties == null){
      properties = new Properties();
      try{
        properties.load(getClass().getResourceAsStream(PLUGIN_PROPERTIES));
      }catch(Exception e){
        logger.warn(
            "Error loading plugin.properties for plugin '" + getName() + "'", e);
      }
    }
    return System.getProperty(name, properties.getProperty(name, defaultValue));
  }

  @Override
  public URL getResource(String resource)
  {
    // short circuit resources we know are missing
    if (missingResources.contains(resource)){
      return null;
    }

    try{
    // try vim resources first
    // Ex: ~/.eclim/resources/jdt/templates/logger.gst

      String localResource = resource;

      // inject the pluginName ("jdt", "wst", etc)
      int index = localResource.indexOf("resources");
      if(index != -1){
        localResource = FileUtils.concat(
            localResource.substring(0, index + 9),
            pluginName,
            localResource.substring(index + 9));
      }
      String file = FileUtils.concat(Services.DOT_ECLIM, localResource);
      if (new File(file).exists()){
        return new URL("file://" + FileUtils.separatorsToUnix(file));
      }

    // next try plugin resources
      URL url = getClass().getResource(resource);
      if (url != null){
        // convert any eclipse specific url to a native java one.
        return FileLocator.resolve(url);
      }

      // not found
      missingResources.add(resource);
      logger.debug(
          "Unable to locate resource in '" + getName() + "': " + resource);
      return null;
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public InputStream getResourceAsStream(String resource)
  {
    try{
      URL url = getResource(resource);
      if (url != null){
        return url.openStream();
      }
      return null;
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close()
  {
  }

  @Override
  public void registerCommand(Class<? extends Command> command)
  {
    org.eclim.annotation.Command info = (org.eclim.annotation.Command)
      command.getAnnotation(org.eclim.annotation.Command.class);
    if(info != null){
      commands.put(info.name(), command);
    }else{
      logger.error(Services.getMessage("command.missing.annotation", command));
    }
  }

  /**
   * Gets the base name used to lookup the plugin's ResourceBundle.
   *
   * @return The ResourceBundle base name.
   */
  protected abstract String getBundleBaseName();

  @Override
  public boolean equals(Object other)
  {
    return ((PluginResources)other).getName().equals(getName());
  }

  @Override
  public int hashCode()
  {
    return getName().hashCode();
  }
}
