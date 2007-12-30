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
