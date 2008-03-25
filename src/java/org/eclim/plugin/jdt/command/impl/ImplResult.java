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

import java.util.List;

/**
 * Container for impl command result.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ImplResult
{
  private String type;
  private List<ImplType> superTypes;

  /**
   * Default constructor.
   */
  public ImplResult (String _type, List<ImplType> _superTypes)
  {
    type = _type;
    superTypes = _superTypes;
  }

  /**
   * Gets the type these results are for.
   *
   * @return The type.
   */
  public String getType ()
  {
    return type;
  }

  /**
   * Gets the super type results.
   *
   * @return The results.
   */
  public List<ImplType> getSuperTypes ()
  {
    return superTypes;
  }
}
