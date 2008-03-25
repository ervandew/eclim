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
package org.eclim.plugin.jdt.util;

import java.util.Comparator;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Comparator for sorting IJavaElement(s).
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class IJavaElementComparator
  implements Comparator
{
  /**
   * {@inheritDoc}
   */
  public int compare (Object _o1, Object _o2)
  {
    if(_o1 == null && _o2 == null){
      return 0;
    }else if(_o2 == null){
      return -1;
    }else if(_o1 == null){
      return 1;
    }

    IJavaElement p1 = JavaUtils.getPrimaryElement((IJavaElement)_o1);
    IJavaElement p2 = JavaUtils.getPrimaryElement((IJavaElement)_o2);

    return p1.getElementType() - p2.getElementType();
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals (Object _obj)
  {
    if(_obj instanceof IJavaElementComparator){
      return true;
    }
    return false;
  }
}
