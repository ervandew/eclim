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
package org.eclim.plugin.jdt.command.regex;

import java.util.List;

import org.eclim.command.OutputFilter;

/**
 * Filters regex result into a string.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class RegexFilter
  implements OutputFilter
{
  /**
   * {@inheritDoc}
   */
  public String filter (Object _result)
  {
    StringBuffer buffer = new StringBuffer();
    List results = (List)_result;
    if(results != null){
      for (int ii = 0; ii < results.size(); ii++){
        if(buffer.length() > 0){
          buffer.append('\n');
        }
        MatcherResult result = (MatcherResult)results.get(ii);
        buffer.append(matchToString(result));
        for (int jj = 0; jj < result.getGroupMatches().size(); jj++){
          MatcherResult group = (MatcherResult)result.getGroupMatches().get(jj);
          buffer.append('|').append(matchToString(group));
        }
      }
    }

    return buffer.toString();
  }

  /**
   * Converts the supplied match result to a string.
   *
   * @param _result The match result.
   * @return The string form of the match result.
   */
  private String matchToString (MatcherResult _result)
  {
    return new StringBuffer()
      .append(_result.getStartLine()).append(':')
      .append(_result.getStartColumn()).append('-')
      .append(_result.getEndLine()).append(':')
      .append(_result.getEndColumn())
      .toString();
  }
}
