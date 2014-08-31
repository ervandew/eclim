/**
 * Copyright (C) 2014  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.debug.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility methods to create a UI view.
 */
public class ViewUtils
{
  public static final String UNKNOWN = "<Unknown>";

  public static final String NO_EXPLICIT_RETURN_VALUE = "No explicit return value";

  public static final String STRING_CLASS_NAME = "java.lang.String";

  public static String getQualifiedName(String name)
  {
    return removeQualifierFromGenericName(name);
  }

  /**
   * Returns the simple generic name from a qualified generic name.
   */
  private static String removeQualifierFromGenericName(String qualifiedName)
  {
    if (qualifiedName.endsWith("...")) {
      // handle variable argument name
      return removeQualifierFromGenericName(
          qualifiedName.substring(0, qualifiedName.length() - 3)) + "...";
    }
    if (qualifiedName.endsWith("[]")) {
      // handle array type
      return removeQualifierFromGenericName(
          qualifiedName.substring(0, qualifiedName.length() - 2)) + "[]";
    }
    // check if the type has parameters
    int parameterStart = qualifiedName.indexOf('<');
    if (parameterStart == -1) {
      return getSimpleName(qualifiedName);
    }
    // get the list of the parameters and generates their simple name
    List<String> parameters = getNameList(qualifiedName.substring(
          parameterStart + 1,
          qualifiedName.length() - 1));
    StringBuffer name = new StringBuffer(getSimpleName(
          qualifiedName.substring(0, parameterStart)));
    name.append('<');
    Iterator<String> iterator = parameters.iterator();
    if (iterator.hasNext()) {
      name.append(removeQualifierFromGenericName(iterator.next()));
      while (iterator.hasNext()) {
        name.append(',').append(removeQualifierFromGenericName(iterator.next()));
      }
    }
    name.append('>');
    return name.toString();
  }

  /**
   * Return the simple name from a qualified name (non-generic)
   */
  private static String getSimpleName(String qualifiedName)
  {
    int index = qualifiedName.lastIndexOf('.');
    if (index >= 0) {
      return qualifiedName.substring(index + 1);
    }
    return qualifiedName;
  }

  /**
   * Decomposes a comma separated list of generic names (String) to a list of
   * generic names (List).
   */
  private static List<String> getNameList(String listName)
  {
    List<String> names = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(listName, ",<>", true);
    int enclosingLevel = 0;
    int startPos = 0;
    int currentPos = 0;
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      switch (token.charAt(0)) {
        case ',':
          if (enclosingLevel == 0) {
            names.add(listName.substring(startPos, currentPos));
            startPos = currentPos + 1;
          }
          break;
        case '<':
          enclosingLevel++;
          break;
        case '>':
          enclosingLevel--;
          break;
      }
      currentPos += token.length();
    }
    names.add(listName.substring(startPos));
    return names;
  }
}
