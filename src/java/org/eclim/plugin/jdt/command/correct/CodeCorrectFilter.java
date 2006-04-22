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
package org.eclim.plugin.jdt.command.correct;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;
import org.eclim.command.Options;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Output filter for code correction results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCorrectFilter
  implements OutputFilter
{
  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, Object _result)
  {
    StringBuffer buffer = new StringBuffer();
    if(_result != null && !(_result instanceof String)){
      CodeCorrectResult[] results = (CodeCorrectResult[])_result;
      for(int ii = 0; ii < results.length; ii++){
        // filter out corrections that have no preview, since they can't be
        // applied in the same fashion as those that have previews.
        if(results[ii].getPreview() != null){
          if(buffer.length() == 0){
            buffer.append(results[ii].getProblem().getMessage());
          }
          buffer.append('\n').append(results[ii].getIndex())
            .append('.').append(results[ii].getProblem().getSourceStart())
            .append(":  ").append(results[ii].getDescription());

          String preview = results[ii].getPreview()
              .replaceAll("<br>", "\n")
              .replaceAll("<.+>", "")
              .replaceAll("\\n\\s*", "\n\t");
          buffer.append("\n\t").append(preview.trim());
        }
      }
    }
    return buffer.toString();
  }
}
