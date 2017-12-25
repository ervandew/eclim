/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
 * Represents an eclipse project option.  Eclipse options are pre-defined and
 * only their values may be changed and any new options are silently ignored.
 *
 * @author Eric Van Dewoestine
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
  private String description;
  private Validator validator;

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
   * @param scope The scope.
   */
  /*public void setScope (int scope)
  {
    this.scope = scope;
  }*/

  /**
   * Gets the nature for this instance.
   *
   * @return The nature.
   */
  public String getNature()
  {
    return this.nature;
  }

  /**
   * Sets the nature for this instance.
   *
   * @param nature The nature.
   */
  public void setNature(String nature)
  {
    this.nature = nature;
  }

  /**
   * Get path.
   *
   * @return path as String.
   */
  public String getPath()
  {
    return this.path;
  }

  /**
   * Set path.
   *
   * @param path the value to set.
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  /**
   * Get name.
   *
   * @return name as String.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Set name.
   *
   * @param name the value to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Get description.
   *
   * @return description as String.
   */
  public String getDescription()
  {
    return this.description;
  }

  /**
   * Set description.
   *
   * @param description the value to set.
   */
  public void setDescription(String description)
  {
    this.description = Services.getMessage(description);
  }

  /**
   * Gets the validator for this instance.
   *
   * @return The validator.
   */
  public Validator getValidator()
  {
    return this.validator;
  }

  /**
   * Sets the validator for this instance.
   *
   * @param validator The validator.
   */
  public void setValidator(Validator validator)
  {
    this.validator = validator;
  }

  @Override
  public int compareTo(Option obj)
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
