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
package org.eclim.plugin.jdt.command.impl;

/**
 * Represents a super class/interface method that can be
 * overridden/implemented.
 *
 * @author Eric Van Dewoestine
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
  public String getSignature()
  {
    return this.signature;
  }

  /**
   * Set signature.
   *
   * @param signature the value to set.
   */
  public void setSignature(String signature)
  {
    this.signature = signature;
  }

  /**
   * Get implemented.
   *
   * @return implemented as boolean.
   */
  public boolean isImplemented()
  {
    return this.implemented;
  }

  /**
   * Set implemented.
   *
   * @param implemented the value to set.
   */
  public void setImplemented(boolean implemented)
  {
    this.implemented = implemented;
  }
}
