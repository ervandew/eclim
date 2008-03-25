/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.plugin.jdt.command.regex;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Filters regex result into a string.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class RegexFilter
  implements OutputFilter<List<MatcherResult>>
{
  public static final RegexFilter instance = new RegexFilter();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<MatcherResult> _result)
  {
    StringBuffer buffer = new StringBuffer();
    if(_result != null){
      for (MatcherResult result : _result){
        if(buffer.length() > 0){
          buffer.append('\n');
        }
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
