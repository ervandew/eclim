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
package org.eclim.plugin.jdt.command.correct;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Output filter for code correction results.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCorrectFilter
  implements OutputFilter<List<CodeCorrectResult>>
{
  public static final CodeCorrectFilter instance = new CodeCorrectFilter();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<CodeCorrectResult> _result)
  {
    StringBuffer buffer = new StringBuffer();
    if(_result != null){
      for(CodeCorrectResult result : _result){
        // filter out corrections that have no preview, since they can't be
        // applied in the same fashion as those that have previews.
        if(result.getPreview() != null){
          if(buffer.length() == 0){
            buffer.append(result.getProblem().getMessage());
          }
          buffer.append('\n').append(result.getIndex())
            .append('.').append(result.getProblem().getSourceStart())
            .append(":  ").append(result.getDescription());

          String preview = result.getPreview()
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
