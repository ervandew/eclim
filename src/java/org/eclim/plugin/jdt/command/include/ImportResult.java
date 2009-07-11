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
package org.eclim.plugin.jdt.command.include;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Represents an import request result.
 *
 * @author Eric Van Dewoestine
 */
public class ImportResult
  implements Comparable<ImportResult>
{
  private String element;
  private int type;

  /**
   * Constructs a new instance.
   *
   * @param element The resulting element.
   * @param type The element type.
   */
  public ImportResult (String element, int type)
  {
    this.element = element;
    this.type = type;
  }

  /**
   * Get element.
   *
   * @return element as String.
   */
  public String getElement()
  {
    return this.element;
  }

  /**
   * Get type.
   *
   * @return type as int.
   */
  public int getType()
  {
    return this.type;
  }

  public boolean equals(Object obj)
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

  /**
   * {@inheritDoc}
   * @see Comparable#compareTo(Object)
   */
  public int compareTo(ImportResult other)
  {
    return getElement().compareTo(other.getElement());
  }
}
