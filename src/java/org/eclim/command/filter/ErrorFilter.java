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

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.OutputFilter;
import org.eclim.command.Options;

/**
 * Filter for generating vim output for an array of Error.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ErrorFilter
  implements OutputFilter
{
  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, Object _result)
  {
    if (_result != null &&
        _result.getClass().isArray() &&
        Error.class.isAssignableFrom(_result.getClass().getComponentType()))
    {
      StringBuffer buffer = new StringBuffer();
      Error[] errors = (Error[])_result;
      for(int ii = 0; ii < errors.length; ii++){
        if(ii > 0){
          buffer.append('\n');
        }
        buffer.append(errors[ii].getFilename())
          .append('|')
          .append(errors[ii].getLineNumber())
          .append(" col ")
          .append(errors[ii].getColumnNumber())
          .append('|')
          .append(errors[ii].getMessage())
          .append('|')
          .append(errors[ii].isWarning() ? 'w' : 'e');
      }

      return buffer.toString();
    }
    return _result.toString();
  }
}
