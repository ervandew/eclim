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

/**
 * Represents a preference which is similar to an option, except under non
 * eclipse node keys, new preference keys may be created.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class Preference
  extends Option
{
  private String defaultValue;

  /**
   * Get defaultValue.
   *
   * @return defaultValue as String.
   */
  public String getDefaultValue ()
  {
    return this.defaultValue != null ? this.defaultValue : "";
  }

  /**
   * Set defaultValue.
   *
   * @param _defaultValue the value to set.
   */
  public void setDefaultValue (String _defaultValue)
  {
    this.defaultValue = _defaultValue;
  }
}
