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
package org.eclim.command.taglist;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Filter for tag list results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class TaglistFilter
  implements OutputFilter<TagResult[]>
{
  public static final TaglistFilter instance = new TaglistFilter();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, TagResult[] _result)
  {
    StringBuffer buffer = new StringBuffer();

    for (TagResult result : _result){
      if(buffer.length() > 0){
        buffer.append('\n');
      }
      buffer.append(result.getName())
        .append('\t')
        .append(result.getFile())
        .append("\t/^").append(result.getPattern()).append("$/;\"")
        .append('\t')
        .append(result.getKind())
        .append("\tline:").append(result.getLine());
    }

    return buffer.toString();
  }
}
