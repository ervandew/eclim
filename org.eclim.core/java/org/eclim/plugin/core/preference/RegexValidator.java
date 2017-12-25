/**
 * Copyright (C) 2011 - 2017  Eric Van Dewoestine
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

import org.eclim.Services;

/**
 * Option validator that validates options using regex.
 *
 * @author Eric Van Dewoestine
 */
public class RegexValidator
  implements Validator
{
  private String regex;

  public RegexValidator(String regex)
  {
    this.regex = regex;
  }

  @Override
  public boolean isValid(Object value)
  {
    return value != null && ((String)value).matches(regex);
  }

  @Override
  public String getMessage(String name, Object value)
  {
    return Services.getMessage("setting.invalid.regex", value, regex);
  }
}
