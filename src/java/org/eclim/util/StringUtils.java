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
package org.eclim.util;

import java.util.Map;

/**
 * Extension to commons lang StringUtils that adds some additional useful
 * utility methods.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class StringUtils
  extends org.apache.commons.lang.StringUtils
{
  private static final String PLACEHOLDER_PREFIX = "${";
  private static final String PLACEHOLDER_SUFFIX = "}";

  /**
   * Replaces placeholders of the form ${key} in the supplied string using
   * key / value pairs from the Map provided.
   *
   * @param _string the String to evaluate.
   * @return The evaluation result.
   */
  public static String replacePlaceholders (String _string, Map _values)
  {
    if(_string == null || _values == null){
      return _string;
    }

    StringBuffer buffer = new StringBuffer(_string);

    int start = buffer.indexOf(PLACEHOLDER_PREFIX);
    int end = buffer.indexOf(PLACEHOLDER_SUFFIX);
    while(start != -1 && end != -1){
      String placeholder =
        buffer.substring(start + PLACEHOLDER_PREFIX.length(), end);

      Object value = _values.get(placeholder);
      if(value != null){
        buffer.replace(start, end + 1, value.toString());
      }

      start = buffer.indexOf(PLACEHOLDER_PREFIX, start + 1);
      end = buffer.indexOf(PLACEHOLDER_SUFFIX, start + 1);
    }

    return buffer.toString();
  }
}
