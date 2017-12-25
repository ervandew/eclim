/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import java.util.Collection;
import java.util.ResourceBundle;

import org.eclim.command.Command;

/**
 * Interface that every plugin must provide an implemtation of, which will be
 * used to locate services, messages, etc, that the plugin provides.
 *
 * @author Eric Van Dewoestine
 */
public interface PluginResources
{
  /**
   * Initialize the plugin resources.
   *
   * @param name The plugin name.
   */
  public void initialize(String name);

  /**
   * Registers the supplied command.
   *
   * @param command The command class.
   */
  public void registerCommand(Class<? extends Command> command);

  /**
   * Gets a command by name.
   *
   * @param name The name of the service.
   *
   * @return The command instance.
   */
  public Command getCommand(String name);

  /**
   * Determines if this instance contains the command with the supplied name.
   *
   * @param name The name of the command.
   * @return true if this instance contains the command, false otherwise.
   */
  public boolean containsCommand(String name);

  /**
   * Gets a message that can be formated with the optional array of arguments
   * provided.
   *
   * @param key The key of the message to retrieve.
   * @param args The (possibly null) array of argument to format the message
   *  with.
   * @return The message.
   */
  public String getMessage(String key, Object... args);

  /**
   * Gets the underlying resource bundle.
   *
   * @return The ResourceBundle.
   */
  public ResourceBundle getResourceBundle();

  /**
   * Gets a property by name.
   * Properties defined by plugin.properties at the root of the plugin.
   *
   * @param name The property name.
   * @return The property or null if not found.
   */
  public String getProperty(String name);

  /**
   * Gets a property by name.
   * Properties defined by plugin.properties at the root of the plugin.
   *
   * @param name The property name.
   * @param defaultValue The value to return if no value found.
   * @return The property value or the supplied default value.
   */
  public String getProperty(String name, String defaultValue);

  /**
   * Gets the url to a plugin resource.
   *
   * @param resource The plugin resource to get.
   * @return The url or null if resource not found.
   */
  public URL getResource(String resource);

  /**
   * Gets an input stream to read a plugin resource.
   *
   * @param resource The plugin resource to get an input stream for.
   * @return The input stream or null if the resource was not found.
   */
  public InputStream getResourceAsStream(String resource);

  /**
   * Closes this resource instance and releases any held resources.
   */
  public void close();

  /**
   * Gets the name of the plugin resources (org.eclim.ant, org.eclim.jdt, etc.).
   *
   * @return The name.
   */
  public String getName();

  /**
   * Gets all commands defined in the system.
   * @return A list of all existing commands
   */
  public Collection<? extends Class<? extends Command>> getCommandClasses();
}
