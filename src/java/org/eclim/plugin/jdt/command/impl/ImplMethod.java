/**
 * Copyright (c) 2004 - 2005
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
 * Represents a super class/interface method that can be
 * overridden/implemented.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ImplMethod
{
  private String signature;
  private boolean implemented;

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
   * Get implemented.
   *
   * @return implemented as boolean.
   */
  public boolean isImplemented ()
  {
    return this.implemented;
  }

  /**
   * Set implemented.
   *
   * @param _implemented the value to set.
   */
  public void setImplemented (boolean _implemented)
  {
    this.implemented = _implemented;
  }
}
