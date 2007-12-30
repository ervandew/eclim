/**
 * Copyright (c) 2005 - 2008
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
package org.eclim.preference;

import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * Defines methods for persisting and retrieving preferences / options.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public interface OptionHandler
{
  /**
   * Gets the nature that this handler supports.
   *
   * @return The project nature id.
   */
  public String getNature ();

  /**
   * Gets the manage options as a Map.
   *
   * @return Map of option names to option values, or null if none.
   */
  public Map<String,String> getOptionsAsMap ()
    throws Exception;

  /**
   * Gets the manage options as a Map for the supplied project.
   *
   * @param _project The project.
   * @return Map of option names to option values, or null if none.
   */
  public Map<String,String> getOptionsAsMap (IProject _project)
    throws Exception;

  /**
   * Sets the supplied option.
   *
   * @param _name The option name.
   * @param _value The option value.
   */
  public void setOption (String _name, String _value)
    throws Exception;

  /**
   * Sets the supplied option for the specified project.
   *
   * @param _project The project.
   * @param _name The option name.
   * @param _value The option value.
   */
  public void setOption (IProject _project, String _name, String _value)
    throws Exception;
}
