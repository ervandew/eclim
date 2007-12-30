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
package org.eclim.plugin.jdt.command.doc;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Filter for javadoc search results.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class DocSearchFilter
  implements OutputFilter<List<String>>
{
  public static final DocSearchFilter instance = new DocSearchFilter();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<String> _result)
  {
    StringBuffer buffer = new StringBuffer();
    if(_result != null){
      for(String doc : _result){
        if(buffer.length() > 0){
          buffer.append('\n');
        }
        buffer.append(doc);
      }
    }
    return buffer.toString();
  }
}
