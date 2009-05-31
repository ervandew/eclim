/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.dltk.util;

import java.util.ArrayList;

/**
 * Utility methods for working with dltk projects.
 *
 * @author Eric Van Dewoestine
 */
public class DltkUtils
{
  private static ArrayList<String> natures = new ArrayList<String>();

  /**
   * Add the supplied nature to the list of known dltk based natures.
   *
   * @param nature The nature id.
   */
  public static void addDltkNature(String nature)
  {
    if (!natures.contains(nature)){
      natures.add(nature);
    }
  }

  /**
   * Gets an array of registered dltk natures.
   *
   * @return An array of natures ids.
   */
  public static String[] getDltkNatures()
  {
    return natures.toArray(new String[natures.size()]);
  }
}
