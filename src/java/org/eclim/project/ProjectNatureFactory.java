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
package org.eclim.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import org.eclipse.core.resources.IProject;

/**
 * Factory for registering project natures.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ProjectNatureFactory
{
  public static String NONE = "none";

  private static Map<String,String> natureAliases = new HashMap<String,String>();

  /**
   * Registers a project nature.
   * @param _alias The nature alias for users.
   * @param _nature The actual nature name the alias maps to.
   */
  public static void addNature (String _alias, String _nature)
  {
    natureAliases.put(_alias, _nature);
  }

  /**
   * Gets array of registered nature aliases.
   *
   * @return Array of aliases.
   */
  public static String[] getNatureAliases ()
  {
    return natureAliases.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
  }

  /**
   * Gets the nature string for the supplied alias.
   *
   * @param _alias The nature alias.
   * @return The nature or null if not found.
   */
  public static String getNatureForAlias (String _alias)
  {
    return natureAliases.get(_alias);
  }

  /**
   * Gets the alias for a given nature id.
   *
   * @param _natureId The nature id.
   * @return The alias.
   */
  public static String getAliasForNature (String _natureId)
  {
    for(String key : natureAliases.keySet()){
      if(_natureId.equals(natureAliases.get(key))){
        return key;
      }
    }
    return null;
  }

  /**
   * Gets an array of natures aliases which are associated with the supplied
   * project.
   *
   * @param _project The project to get the aliases for.
   * @return Array of aliases.
   */
  public static String[] getProjectNatureAliases (IProject _project)
    throws Exception
  {
    ArrayList<String> aliases = new ArrayList<String>();
    for(String key : natureAliases.keySet()){
      if(_project.hasNature(natureAliases.get(key))){
        aliases.add(key);
      }
    }

    return aliases.toArray(new String[aliases.size()]);
  }
}
