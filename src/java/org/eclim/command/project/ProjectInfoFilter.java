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
package org.eclim.command.project;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclim.command.OutputFilter;

/**
 * Output filter for project info.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectInfoFilter
  implements OutputFilter
{
  /**
   * {@inheritDoc}
   */
  public String filter (Object _result)
  {
    StringBuffer buffer = new StringBuffer();

    // list of all projects.
    if(_result instanceof List){
      for(Iterator ii = ((List)_result).iterator(); ii.hasNext();){
        if(buffer.length() > 0){
          buffer.append('\n');
        }
        buffer.append(ii.next());
      }
      return buffer.toString();

    // map of a project's settings.
    }else if (_result instanceof Map){
      Map result = (Map)_result;
      for(Iterator ii = result.keySet().iterator(); ii.hasNext();){
        Object key = ii.next();
        if(buffer.length() > 0){
          buffer.append('\n');
        }
        buffer.append(key).append('=').append(result.get(key));
      }
      return buffer.toString();
    }

    return _result.toString();
  }
}
