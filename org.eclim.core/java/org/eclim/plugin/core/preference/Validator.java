/**
 * Copyright (C) 2011 - 2021  Eric Van Dewoestine
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
 * Interface for defining option validators.
 *
 * @author Eric Van Dewoestine
 */
public interface Validator
{
  /**
   * Determines if the supplied option value is valid or not.
   *
   * @param value the option value.
   *
   * @return true if valid, false otherwise.
   */
  public boolean isValid(Object value);

  /**
   * Gets the message to use for an invalid option value.
   *
   * @param name the option name.
   * @param value the option value.
   *
   * @return the message to display to the user.
   */
  public String getMessage(String name, Object value);

  /**
   * Construct an OptionInstance for the supplied Option and value.
   *
   * @param option Option instance.
   * @param value The option value.
   *
   * @return An OptionInstance
   */
  public OptionInstance optionInstance(Option option, String value);
}
