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
package org.eclim.plugin.pdt.command.includepath;

/**
 * Represents a pdt include path variable.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class IncludePathVariable
  implements Comparable<IncludePathVariable>
{
  private String name;
  private String path;

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
   * {@inheritDoc}
   * @see Comparable#compareTo(Object)
   */
  public int compareTo (IncludePathVariable obj)
  {
    if (obj == this){
      return 0;
    }

    return this.getName().compareTo(obj.getName());
  }
}
