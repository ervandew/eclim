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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;

import org.eclim.command.OutputFilter;

import org.eclim.preference.OptionInstance;

/**
 * Output filter for project info.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectInfoFilter
  implements OutputFilter
{
  private static Comparator OPTION_COMPARATOR =
    new BeanComparator("name");

  /**
   * {@inheritDoc}
   */
  public String filter (Object _result)
  {
    StringBuffer buffer = new StringBuffer();

    List list = (List)_result;
    if(list.size() > 0){
      // list of project's current settings.
      if(list.get(0) instanceof OptionInstance){
        // sort the list
        Collections.sort(list, OPTION_COMPARATOR);
        for(Iterator ii = list.iterator(); ii.hasNext();){
          OptionInstance option = (OptionInstance)ii.next();
          if(buffer.length() > 0){
            buffer.append('\n');
          }
          buffer.append("# ").append(option.getDescription()).append('\n');
          buffer.append(option.getName()).append('=').append(option.getValue());
        }
        return buffer.toString();

      // list of all projects.
      }else{
        for(Iterator ii = list.iterator(); ii.hasNext();){
          if(buffer.length() > 0){
            buffer.append('\n');
          }
          buffer.append(ii.next());
        }
        return buffer.toString();
      }
    }

    return buffer.toString();
  }
}
