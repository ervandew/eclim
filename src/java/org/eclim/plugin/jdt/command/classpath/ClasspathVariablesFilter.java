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
package org.eclim.plugin.jdt.command.classpath;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Filter for ClasspathVariablesCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ClasspathVariablesFilter
  implements OutputFilter<List<ClasspathVariable>>
{
  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<ClasspathVariable> _result)
  {
    StringBuffer buffer = new StringBuffer();

    Collections.sort(_result);

    int length = 0;
    for(ClasspathVariable variable : _result){
      length = variable.getName().length() > length ?
        variable.getName().length() : length;
    }

    for(ClasspathVariable variable : _result){
      if(buffer.length() > 0){
        buffer.append('\n');
      }
      buffer.append(StringUtils.rightPad(variable.getName(), length))
        .append(" - ")
        .append(variable.getPath());
    }
    return buffer.toString();
  }
}
