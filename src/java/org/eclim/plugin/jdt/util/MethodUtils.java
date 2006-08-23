/**
 * Copyright (c) 2005 - 2006
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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * Utility methods for working with IMethod elements.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class MethodUtils
{
  /**
   * Retrieves the method which follows the supplied method in the specified
   * type.
   *
   * @param _type The type.
   * @param _method The method.
   * @return The method declared after the supplied method.
   */
  public static IMethod getMethodAfter (IType _type, IMethod _method)
    throws Exception
  {
    if(_type == null || _method == null){
      return null;
    }

    // get the method after the sibling.
    IMethod[] all = _type.getMethods();
    for (int ii = 0; ii < all.length; ii++){
      if(all[ii].equals(_method) && ii < all.length - 1){
        return all[ii + 1];
      }
    }
    return null;
  }
}
