/**
 * Copyright (C) 2005 - 2021  Eric Van Dewoestine
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
package org.eclim.plugin.core.preference;

/**
 * Represents an instance of an Option with a value.
 *
 * @author Eric Van Dewoestine
 */
public class OptionInstance
  extends Option
{
  private Object value;

  /**
   * Constructs a new instance.
   *
   * @param option The option.
   * @param value The option value.
   */
  public OptionInstance(Option option, Object value)
  {
    this.value = value;
    setPath(option.getPath());
    setName(option.getName());
  }

  @Override
  public void setName(String name)
  {
    super.setName(name);
    super.setDescription(name);
  }

  /**
   * Get value.
   *
   * @return the option value.
   */
  public Object getValue()
  {
    return this.value;
  }

  /**
   * Set value.
   *
   * @param value the value to set.
   */
  public void setValue(String value)
  {
    this.value = value;
  }
}
