/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
 * @author Eric Van Dewoestine
 */
@SuppressWarnings("unused")
public class ImplType
{
  private String packageName;
  private String signature;
  private String[] methods;

  /**
   * Constructs a new instance.
   *
   * @param packageName The packageName for this instance.
   * @param signature The signature for this instance.
   * @param methods The methods for this instance.
   */
  public ImplType(String packageName, String signature, String[] methods)
  {
    this.packageName = packageName;
    this.signature = signature;
    this.methods = methods;
  }
}
