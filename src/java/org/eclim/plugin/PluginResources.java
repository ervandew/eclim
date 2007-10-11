/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.plugin;

import java.io.InputStream;

import java.net.URL;

import java.util.ResourceBundle;

import org.eclim.command.Command;

/**
 * Interface that every plugin must provide an implemtation of, which will be
 * used to locate services, messages, etc, that the plugin provides.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public interface PluginResources
{
  /**
   * Gets a command by name.
   *
   * @param _name The name of the service.
   *
   * @return The command instance.
   */
  public Command getCommand (String _name)
    throws Exception;

  /**
   * Determines if this instance contains the command with the supplied name.
   *
   * @param _name The name of the command.
   * @return true if this instance contains the command, false otherwise.
   */
  public boolean containsCommand (String _name);

  /**
   * Gets a message that can be formated with the optional array of arguments
   * provided.
   *
   * @param _key The key of the message to retrieve.
   * @param _args The (possibly null) array of argument to format the message
   *  with.
   * @return The message.
   */
  public String getMessage (String _key, Object... _args);

  /**
   * Gets the underlying resource bundle.
   * <p/>
   * Used for obtaining the usage strings of every command, but may be used for
   * more in the future.
   *
   * @return The ResourceBundle.
   */
  public ResourceBundle getResourceBundle ();

  /**
   * Gets a property by name.
   * <p/>
   * Properties defined by plugin.properties at the root of the plugin.
   *
   * @param _name The property name.
   * @return The property or null if not found.
   */
  public String getProperty (String _name);

  /**
   * Gets a property by name.
   * <p/>
   * Properties defined by plugin.properties at the root of the plugin.
   *
   * @param _name The property name.
   * @param _default The value to return if no value found.
   * @return The property value or the supplied default value.
   */
  public String getProperty (String _name, String _default);

  /**
   * Gets the url to a plugin resource.
   *
   * @param _resource The plugin resource to get.
   * @return The url or null if resource not found.
   */
  public URL getResource (String _resource);

  /**
   * Gets an input stream to read a plugin resource.
   *
   * @param _resource The plugin resource to get an input stream for.
   * @return The input stream or null if the resource was not found.
   */
  public InputStream getResourceAsStream (String _resource);

  /**
   * Closes this resource instance and releases any held resources.
   */
  public void close ()
    throws Exception;

  /**
   * Gets the name of the plugin resources (org.eclim.ant, org.eclim.jdt, etc.).
   *
   * @return The name.
   */
  public String getName ();
}
