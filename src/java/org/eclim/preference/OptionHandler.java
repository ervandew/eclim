/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
