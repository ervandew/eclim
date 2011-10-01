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
package org.eclim.plugin.dltk.preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.ScriptRuntime;

/**
 * Class used to store and translate interpreter type aliases to the actual
 * dltk intepreter type.
 *
 * @author Eric Van Dewoestine
 */
public class DltkInterpreterTypeManager
{
  private static Map<String, String> interpreterTypeAliases =
    new HashMap<String, String>();

  /**
   * Registers a new alias mapping to an interpreter type with the supplied id.
   *
   * @param alias The alias to use.
   * @param nature The eclipse nature.
   * @param typeId The intepreter type id.
   */
  public static void addInterpreterType(
      String alias, String nature, String typeId)
  {
    interpreterTypeAliases.put(nature + '.' + alias, typeId);
  }

  /**
   * Given an interpreter type alias and an eclipse project nature, find and
   * return the associated IInterpreterInstallType instance.
   *
   * @param alias The interpreter type alias.
   * @param nature The eclipse nature.
   * @return The IInterpreterInstallType instance or null if not found.
   */
  public static IInterpreterInstallType getInterpreterInstallType(
      String alias, String nature)
  {
    String typeId = interpreterTypeAliases.get(nature + '.' + alias);
    if(typeId != null){
      IInterpreterInstallType[] types =
        ScriptRuntime.getInterpreterInstallTypes(nature);
      for (IInterpreterInstallType iit : types){
        if (typeId.equals(iit.getId())){
          return iit;
        }
      }
    }
    return null;
  }

  /**
   * Gets array of registered interpreter type aliases for the supplied eclipse
   * project nature.
   *
   * @param nature The eclipse project nature.
   * @return Array of aliases.
   */
  public static String[] getIntepreterTypeAliases(String nature)
  {
    ArrayList<String> aliases = new ArrayList<String>();
    for (String alias : interpreterTypeAliases.keySet()){
      if (alias.startsWith(nature + '.')){
        aliases.add(alias);
      }
    }
    return aliases.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
  }
}
