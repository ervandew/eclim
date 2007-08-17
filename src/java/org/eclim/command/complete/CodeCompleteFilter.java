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
package org.eclim.command.complete;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.command.OutputFilter;

/**
 * Filter for code completion results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCompleteFilter
  implements OutputFilter<List<CodeCompleteResult>>
{
  private static final String DELIMETER = "|";

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<CodeCompleteResult> _result)
  {
    String delimeter = null;
    try{
      delimeter = _commandLine.hasOption(Options.DELIMETER_OPTION) ?
        _commandLine.getValue(Options.DELIMETER_OPTION) : DELIMETER;
    }catch(Exception e){
      throw new RuntimeException(e);
    }

    StringBuffer buffer = new StringBuffer();
    if(_result != null){
      for(CodeCompleteResult result : _result){
        if(buffer.length() > 0){
          buffer.append('\n');
        }

        buffer.append(result.getCompletion()).append(delimeter);

        if(result.getShortDescription() != null){
          buffer.append(result.getShortDescription());
        }

        buffer.append(delimeter);

        if(result.getDescription() != null){
          buffer.append(result.getDescription());
        }
      }
    }
    return buffer.toString();
  }
}
