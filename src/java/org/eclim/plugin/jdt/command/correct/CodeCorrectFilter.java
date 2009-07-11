/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.correct;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Output filter for code correction results.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCorrectFilter
  implements OutputFilter<List<CodeCorrectResult>>
{
  public static final CodeCorrectFilter instance = new CodeCorrectFilter();

  /**
   * {@inheritDoc}
   */
  public String filter(CommandLine commandLine, List<CodeCorrectResult> results)
  {
    StringBuffer buffer = new StringBuffer();
    if(results != null){
      for(CodeCorrectResult result : results){
        // filter out corrections that have no preview, since they can't be
        // applied in the same fashion as those that have previews.
        String preview = result.getPreview();
        if(preview != null &&
            !preview.trim().equals("") &&
            !preview.trim().startsWith("Start the") &&
            !preview.trim().startsWith("Opens") &&
            !preview.trim().startsWith("Evaluates") &&
            !preview.trim().startsWith("<p>Move")){
          if(buffer.length() == 0){
            buffer.append(result.getProblem().getMessage());
          }
          buffer.append('\n').append(result.getIndex())
            .append('.').append(result.getProblem().getSourceStart())
            .append(":  ").append(result.getDescription());

          preview = preview
              .replaceAll("<br>", "\n")
              .replaceAll("<.+?>", "")
              .replaceAll("\\n", "\n\t");
          buffer.append("\n\t").append(preview.trim());
        }
      }
    }
    return buffer.toString();
  }
}
