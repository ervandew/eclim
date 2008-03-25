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
 * Represents an instance of an Option with a value.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class OptionInstance
  extends Option
{
  private String value;

  /**
   * Constructs a new instance.
   *
   * @param _option The option.
   * @param _value The option value.
   */
  public OptionInstance (Option _option, String _value)
  {
    this.value = _value;
    setPath(_option.getPath());
    setName(_option.getName());
  }

  /**
   * {@inheritDoc}
   */
  public void setName (String _name)
  {
    super.setName(_name);
    super.setDescription(_name);
  }

  /**
   * Get value.
   *
   * @return value as String.
   */
  public String getValue ()
  {
    return this.value;
  }

  /**
   * Set value.
   *
   * @param _value the value to set.
   */
  public void setValue (String _value)
  {
    this.value = _value;
  }
}
