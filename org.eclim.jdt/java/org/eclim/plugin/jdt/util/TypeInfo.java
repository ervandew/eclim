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
package org.eclim.plugin.jdt.util;

import java.util.Arrays;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.eclipse.jdt.core.IType;

/**
 * Class which encapsulates a type and its generics info.
 *
 * @author Eric Van Dewoestine
 */
public class TypeInfo
{
  private IType type;
  private String[] typeParameters;
  private String[] typeArguments;

  /**
   * Constructs a new instance.
   *
   * @param type The type for this instance.
   * @param typeParameters The typeParameters for this instance.
   * @param typeArguments The typeArguments for this instance.
   */
  public TypeInfo(IType type, String[] typeParameters, String[] typeArguments)
  {
    this.type = type;
    this.typeParameters = typeParameters != null ? typeParameters : new String[0];
    this.typeArguments = typeArguments != null ? typeArguments : new String[0];
  }

  /**
   * Gets the type for this instance.
   *
   * @return The type.
   */
  public IType getType()
  {
    return this.type;
  }

  /**
   * Gets the typeParameters for this instance.
   *
   * @return The typeParameters.
   */
  public String[] getTypeParameters()
  {
    return this.typeParameters;
  }

  /**
   * Gets the typeArguments for this instance.
   *
   * @return The typeArguments.
   */
  public String[] getTypeArguments()
  {
    return this.typeArguments;
  }

  /**
   * Determines if this object is equal to the supplied object.
   *
   * @param other The object to test equality with.
   * @return true if the objects are equal, false otherwise.
   */
  public boolean equals(Object other)
  {
    if (!(other instanceof TypeInfo)) {
      return false;
    }
    if (this == other) {
      return true;
    }
    TypeInfo result = (TypeInfo)other;
    boolean equal = new EqualsBuilder()
      .append(getType(), result.getType())
      .isEquals();

    return equal;
  }

  /**
   * Gets the hash code for this object.
   *
   * @return The hash code for this object.
   */
  public int hashCode()
  {
    return new HashCodeBuilder(24, 56)
      .append(type)
      .toHashCode();
  }

  public String toString()
  {
    return new StringBuffer()
      .append(type != null ? type.getElementName() : "null").append(":")
      .append(" params: ").append(Arrays.toString(typeParameters))
      .append(" args: ").append(Arrays.toString(typeArguments))
      .toString();
  }
}
