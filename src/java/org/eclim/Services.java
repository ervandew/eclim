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
package org.eclim;

import java.io.InputStream;

import java.net.URL;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclim.command.Command;

import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.PluginResources;

/**
 * Class responsible for retrieving service implementations and provides access
 * to localized messages.
 *
 * @author Eric Van Dewoestine
 */
public class Services
{
  private static HashMap<String, PluginResources> pluginResources =
    new HashMap<String, PluginResources>();

  static {
    PluginResources defaultResources = new DefaultPluginResources();
    defaultResources.initialize("org.eclim");
    addPluginResources(defaultResources);
  }

  /**
   * Gets a command by name.
   *
   * @param name The command name.
   *
   * @return The command implementation.
   */
  public static Command getCommand(String name)
    throws Exception
  {
    for(PluginResources resources : pluginResources.values()){
      if(resources.containsCommand(name)){
        return resources.getCommand(name);
      }
    }
    return null;
  }

  /**
   * Retrieves and optionally formats a message for the supplied message key.
   *
   * @param key The message key.
   * @param args Optional message args used when formatting the message.
   *
   * @return The formatted message.
   */
  public static String getMessage(String key, Object... args)
  {
    for(PluginResources resources : pluginResources.values()){
      try{
        String message = resources.getMessage(key, args);
        return message;
      }catch(MissingResourceException nsme){
        // message not found in this plugin.
      }
    }
    return key;
  }

  /**
   * Gets the underlying resource bundle used for messages.
   *
   * @param plugin The plugin to get the resources for (org.eclim,
   * org.eclim.jdt, etc.).
   *
   * @return The ResourceBundle.
   */
  public static ResourceBundle getResourceBundle(String plugin)
  {
    if(plugin != null){
      PluginResources resources = (PluginResources)pluginResources.get(plugin);
      if(resources != null){
        return resources.getResourceBundle();
      }
    }
    return null;
  }

  /**
   * Gets a resource by searching the available plugins for it.
   *
   * @param resource The resource to find.
   * @return The URL of the resource or null if not found.
   */
  public static URL getResource(String resource)
  {
    for(PluginResources resources : pluginResources.values()){
      URL url = resources.getResource(resource);
      if(url != null){
        return url;
      }
    }
    return null;
  }

  /**
   * Gets a resource stream by searching the available plugins for it.
   *
   * @param resource The resource to find.
   * @return The resource stream or null if not found.
   */
  public static InputStream getResourceAsStream(String resource)
  {
    for(PluginResources resources : pluginResources.values()){
      InputStream stream = resources.getResourceAsStream(resource);
      if(stream != null){
        return stream;
      }
    }
    return null;
  }

  /**
   * Gets the PluginResources for the plugin with the specified name.
   *
   * @param plugin The plugin name.
   * @return The PluginResources or null if none found.
   */
  public static PluginResources getPluginResources(String plugin)
  {
    PluginResources resources = (PluginResources)pluginResources.get(plugin);
    if(resources == null){
      throw new IllegalArgumentException(
          Services.getMessage("plugin.resources.not.found", plugin));
    }
    return resources;
  }

  /**
   * Adds the supplied PluginResources instance to the list of instances that
   * are used to locate services, messages, etc.
   *
   * @param resources The PluginResources to add.
   */
  public static void addPluginResources(PluginResources resources)
  {
    pluginResources.put(resources.getName(), resources);
  }

  /**
   * Remove the supplied PluginResources.
   *
   * @param resources The PluginResources to remove.
   */
  public static PluginResources removePluginResources(PluginResources resources)
  {
    return pluginResources.remove(resources.getName());
  }

  /**
   * Implementation of PluginResources for the main eclim plugin.
   */
  public static class DefaultPluginResources
    extends AbstractPluginResources
  {
    /**
     * Name that can be used to lookup this PluginResources from
     * {@link Services#getPluginResources(String)}.
     */
    public static final String NAME = "org.eclim";

    /**
     * {@inheritDoc}
     * @see AbstractPluginResources#getBundleBaseName()
     */
    protected String getBundleBaseName()
    {
      return "org/eclim/messages";
    }

    /**
     * {@inheritDoc}
     * @see PluginResources#getName()
     */
    public String getName()
    {
      return NAME;
    }
  }
}
