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
package org.eclim.plugin.dltk.command.buildpath;

/**
 * Represents a dltk build path variable.
 *
 * @author Eric Van Dewoestine
 */
public class BuildpathVariable
  implements Comparable<BuildpathVariable>
{
  private String name;
  private String path;

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

  @Override
  public int compareTo(BuildpathVariable obj)
  {
    if (obj == this){
      return 0;
    }

    return this.getName().compareTo(obj.getName());
  }
}
