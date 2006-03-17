/**
 * Copyright (c) 2004 - 2006
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

import java.util.ResourceBundle;

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
   * Gets a service by name and type.
   *
   * @param _name The name of the service.
   * @param _type The type of the service.
   *
   * @return The service instance.
   */
  public Object getService (String _name, Class _type);

  /**
   * Determines if this instance contains a service with the supplied name.
   *
   * @param _name The name of the service.
   * @return true if this instance contains the service, false otherwise.
   */
  public boolean containsService (String _name);

  /**
   * Gets a message that can be formated with the optional array of arguments
   * provided.
   *
   * @param _key The key of the message to retrieve.
   * @param _args The (possibly null) array of argument to format the message
   *  with.
   * @return The message.
   */
  public String getMessage (String _key, Object[] _args);

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
   * Closes this resource instance and releases any held resources.
   */
  public void close ();

  /**
   * Gets the name of the plugin resources (ant, jdt, etc.).
   *
   * @return The name.
   */
  public String getName ();
}
