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
package org.eclim.util;

import java.util.Collection;

/**
 * Utility methods for working with collections.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CollectionUtils
{
  private CollectionUtils ()
  {
  }

  /**
   * Add all elements from the supplied array to the specified collection.
   *
   * @param collection The collection to add to.
   * @param array The elements to add.
   */
  public static void addAll (Collection collection, Object[] array)
  {
    for (Object obj : array){
      collection.add(obj);
    }
  }
}
