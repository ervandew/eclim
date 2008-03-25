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
package org.eclim.plugin.jdt.command.impl;

/**
 * Represents a super class/interface containing methods that can be
 * overridden/implmented.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ImplType
{
  private String packageName;
  private String signature;
  private ImplMethod[] methods;
  private boolean exists = true;

  /**
   * Get package.
   *
   * @return package as String.
   */
  public String getPackage ()
  {
    return this.packageName;
  }

  /**
   * Set package.
   *
   * @param _package the value to set.
   */
  public void setPackage (String _package)
  {
    this.packageName = _package;
  }

  /**
   * Get signature.
   *
   * @return signature as String.
   */
  public String getSignature ()
  {
    return this.signature;
  }

  /**
   * Set signature.
   *
   * @param _signature the value to set.
   */
  public void setSignature (String _signature)
  {
    this.signature = _signature;
  }

  /**
   * Determines whether or not the type exists (if it was found in the project).
   *
   * @return true if the type exists, false otherwise.
   */
  public boolean getExists ()
  {
    return exists;
  }

  /**
   * Sets whether or not the type exists (if it was found in the project).
   *
   * @param _exists true if the type exists, false otherwise.
   */
  public void setExists (boolean _exists)
  {
    exists = _exists;
  }

  /**
   * Get methods.
   *
   * @return methods as ImplMethod[].
   */
  public ImplMethod[] getMethods ()
  {
    return this.methods;
  }

  /**
   * Set methods.
   *
   * @param _methods the value to set.
   */
  public void setMethods (ImplMethod[] _methods)
  {
    this.methods = _methods;
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals (Object _other)
  {
    if(_other instanceof ImplType){
      ImplType other = (ImplType)_other;
      return (other.getSignature().equals(getSignature()) &&
          other.getPackage().equals(getPackage()));
    }
    return false;
  }
}
