/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.util;

import java.util.Map;

/**
 * Extension to commons lang StringUtils that adds some additional useful
 * utility methods.
 *
 * @author Eric Van Dewoestine
 */
public class StringUtils
  extends org.apache.commons.lang3.StringUtils
{
  private static final String PLACEHOLDER_PREFIX = "${";
  private static final String PLACEHOLDER_SUFFIX = "}";

  /**
   * Replaces placeholders of the form ${key} in the supplied string using
   * key / value pairs from the Map provided.
   *
   * @param string the String to evaluate.
   * @param values the values to use in the string.
   * @return The evaluation result.
   */
  @SuppressWarnings("rawtypes")
  public static String replacePlaceholders(String string, Map values)
  {
    if(string == null || values == null){
      return string;
    }

    StringBuffer buffer = new StringBuffer(string);

    int start = buffer.indexOf(PLACEHOLDER_PREFIX);
    int end = buffer.indexOf(PLACEHOLDER_SUFFIX);
    while(start != -1 && end != -1){
      String placeholder =
        buffer.substring(start + PLACEHOLDER_PREFIX.length(), end);

      Object value = values.get(placeholder);
      if(value != null){
        buffer.replace(start, end + 1, value.toString());
      }

      start = buffer.indexOf(PLACEHOLDER_PREFIX, start + 1);
      end = buffer.indexOf(PLACEHOLDER_SUFFIX, start + 1);
    }

    return buffer.toString();
  }
}
