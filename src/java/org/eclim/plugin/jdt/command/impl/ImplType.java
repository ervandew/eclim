/**
 * Copyright (c) 2005 - 2008
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
