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
package org.eclim.command.filter;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.OutputFilter;

/**
 * Filter for generating vim output for an array of Error.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ErrorFilter
  implements OutputFilter<List<Error>>
{
  public static final ErrorFilter instance = new ErrorFilter();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<Error> _result)
  {
    StringBuffer buffer = new StringBuffer();
    if (_result != null){
      for(Error error : _result){
        if(buffer.length() > 0){
          buffer.append('\n');
        }
        buffer.append(error.getFilename())
          .append('|')
          .append(error.getLineNumber())
          .append(" col ")
          .append(error.getColumnNumber())
          .append('|')
          .append(error.getMessage())
          .append('|')
          .append(error.isWarning() ? 'w' : 'e');
      }

    }
    return buffer.toString();
  }
}
