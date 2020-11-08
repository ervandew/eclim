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
package org.eclim.plugin.core.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import org.eclim.logging.Logger;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;

/**
 * Factory for registering project natures.
 *
 * @author Eric Van Dewoestine
 */
public class ProjectNatureFactory
{
  private static final Logger logger =
    Logger.getLogger(ProjectNatureFactory.class);

  public static String NONE = "none";

  private static Map<String, String[]> natureAliases =
    new HashMap<String, String[]>();

  /**
   * Registers a project nature.
   *
   * @param alias The nature alias for users.
   * @param nature The actual nature id the alias maps to.
   */
  public static void addNature(String alias, String nature)
  {
    logger.debug("add nature alias: {}={}", alias, nature);
    natureAliases.put(alias, new String[]{nature});
  }

  /**
   * Registers a nature alias to an array of project nature ids.
   *
   * @param alias The nature alias for users.
   * @param natures The array of actual nature ids the alias maps to.
   */
  public static void addNature(String alias, String[] natures)
  {
    logger.debug("add nature alias: {}={}", alias, Arrays.toString(natures));
    natureAliases.put(alias, natures);
  }

  /**
   * Gets array of registered nature aliases.
   *
   * @return Array of aliases.
   */
  public static String[] getNatureAliases()
  {
    return natureAliases.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
  }

  /**
   * Gets the map of natures aliases to ids.
   *
   * @return Map of aliases.
   */
  public static Map<String, String[]> getNatureAliasesMap()
  {
    return Collections.unmodifiableMap(natureAliases);
  }

  /**
   * Gets the nature id for the supplied alias.
   *
   * @param alias The nature alias.
   * @return The nature or null if not found.
   */
  public static String getNatureForAlias(String alias)
  {
    String[] natures = natureAliases.get(alias);
    return natures != null ? natures[natures.length - 1] : null;
  }

  /**
   * Gets the array of nature ids the supplied alias maps to.
   *
   * @param alias The nature alias.
   * @return Array of nature ids or null if not found.
   */
  public static String[] getNaturesForAlias(String alias)
  {
    return natureAliases.get(alias);
  }

  /**
   * Gets the alias for a given nature id.
   *
   * @param natureId The nature id.
   * @return The alias.
   */
  public static String getAliasForNature(String natureId)
  {
    for(String key : natureAliases.keySet()){
      if(natureId.equals(getNatureForAlias(key))){
        return key;
      }
    }
    return null;
  }

  /**
   * Gets an array of natures aliases which are associated with the supplied
   * project.
   *
   * @param project The project to get the aliases for.
   * @return Array of aliases.
   */
  public static String[] getProjectNatureAliases(IProject project)
  {
    ArrayList<String> aliases = new ArrayList<String>();
    try{
      for(String key : natureAliases.keySet()){
        if(project.hasNature(getNatureForAlias(key))){
          aliases.add(key);
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    return aliases.toArray(new String[aliases.size()]);
  }

  /**
   * Gets an array of natures which are associated with the supplied project.
   *
   * @param project The project to get the natures for.
   * @return Array of natures.
   */
  public static String[] getProjectNatures(IProject project)
  {
    ArrayList<String> natures = new ArrayList<String>();
    try{
      for(String key : natureAliases.keySet()){
        String[] ids = natureAliases.get(key);
        for (String id : ids){
          if(project.hasNature(id)){
            natures.add(id);
          }
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    return natures.toArray(new String[natures.size()]);
  }
}
