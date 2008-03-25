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
package org.eclim.plugin.jdt.command.include;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Represents an import request result.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ImportResult
{
  private String element;
  private int type;

  /**
   * Constructs a new instance.
   *
   * @param _element The resulting element.
   * @param _type The element type.
   */
  public ImportResult (String _element, int _type)
  {
    element = _element;
    type = _type;
  }

  /**
   * Get element.
   *
   * @return element as String.
   */
  public String getElement ()
  {
    return this.element;
  }

  /**
   * Get type.
   *
   * @return type as int.
   */
  public int getType ()
  {
    return this.type;
  }

  public boolean equals (Object obj)
  {
    if (obj instanceof ImportResult == false) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    ImportResult other = (ImportResult) obj;
    return new EqualsBuilder()
      .append(getElement(), other.getElement())
      .append(getType(), other.getType())
      .isEquals();
  }
}
