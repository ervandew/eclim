/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.preference;

import java.util.regex.Pattern;

import org.eclim.Services;

/**
 * Represents an eclipse project option.  Eclipse options are pre-defined and
 * only their values may be changed and any new options are silently ignored.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Option
  implements Comparable<Option>
{
  private static final String GENERAL = "General";

  /*public static final int ALL = 0;
  public static final int GLOBAL = 1;
  public static final int PROJECT = 2;*/

  //private int scope;
  private String nature;
  private String path;
  private String name;
  private String regex;
  private String description;
  private Pattern pattern;

  /**
   * Gets this option's scope.
   *
   * @return The scope.
   */
  /*public int getScope ()
  {
    return scope;
  }*/

  /**
   * Sets this option's scope.
   *
   * @param _scope The scope.
   */
  /*public void setScope (int _scope)
  {
    scope = _scope;
  }*/

  /**
   * Gets the nature for this instance.
   *
   * @return The nature.
   */
  public String getNature ()
  {
    return this.nature;
  }

  /**
   * Sets the nature for this instance.
   *
   * @param nature The nature.
   */
  public void setNature (String nature)
  {
    this.nature = nature;
  }

  /**
   * Get path.
   *
   * @return path as String.
   */
  public String getPath ()
  {
    return this.path;
  }

  /**
   * Set path.
   *
   * @param _path the value to set.
   */
  public void setPath (String _path)
  {
    this.path = _path;
  }

  /**
   * Get name.
   *
   * @return name as String.
   */
  public String getName ()
  {
    return this.name;
  }

  /**
   * Set name.
   *
   * @param _name the value to set.
   */
  public void setName (String _name)
  {
    this.name = _name;
  }

  /**
   * Get regex.
   *
   * @return regex as String.
   */
  public String getRegex ()
  {
    return this.regex;
  }

  /**
   * Set regex.
   *
   * @param _regex the value to set.
   */
  public void setRegex (String _regex)
  {
    this.regex = _regex;
    if(regex != null && regex.trim().length() > 0){
      this.pattern = Pattern.compile(_regex);
    }
  }

  /**
   * Get pattern.
   *
   * @return pattern as Pattern.
   */
  public Pattern getPattern ()
  {
    return this.pattern;
  }

  /**
   * Get description.
   *
   * @return description as String.
   */
  public String getDescription ()
  {
    return this.description;
  }

  /**
   * Set description.
   *
   * @param _description the value to set.
   */
  public void setDescription (String _description)
  {
    this.description = Services.getMessage(_description);
  }

  /**
   * {@inheritDoc}
   * @see Comparable#compareTo(T)
   */
  public int compareTo (Option obj)
  {
    if(obj == this){
      return 0;
    }

    int compare = 0;
    if(this.getPath().equals(obj.getPath())){
      compare = 0;
    }

    if (this.getPath().startsWith(GENERAL) &&
        !obj.getPath().startsWith(GENERAL))
    {
      return -1;
    }

    if (obj.getPath().startsWith(GENERAL) &&
        !this.getPath().startsWith(GENERAL))
    {
      return 1;
    }

    compare = this.getPath().compareTo(obj.getPath());
    if (compare == 0){
      compare = this.getName().compareTo(obj.getName());
    }
    return compare;
  }
}
