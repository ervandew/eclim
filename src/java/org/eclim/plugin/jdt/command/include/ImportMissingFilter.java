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
package org.eclim.plugin.jdt.command.include;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Filter to format results from ImportMissingCommand.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ImportMissingFilter
  implements OutputFilter<List<ImportMissingResult>>
{
  public static final ImportMissingFilter instance = new ImportMissingFilter();

  /**
   * {@inheritDoc}
   * @see OutputFilter#filter(CommandLine,T)
   */
  public String filter(CommandLine commandLine, List<ImportMissingResult> results)
  {
    StringBuffer out = new StringBuffer();
    out.append('[');
    for(int ii = 0; ii < results.size(); ii++){
      ImportMissingResult result = results.get(ii);
      if(ii > 0){
        out.append(',');
      }
      out.append("{'type': '").append(result.getType()).append("',");
      out.append("'imports': [");
      List<ImportResult> imports = result.getImports();
      for(int jj = 0; jj < imports.size(); jj++){
        ImportResult ir = imports.get(jj);
        if(jj > 0){
          out.append(',');
        }
        out.append('\'').append(ir.getElement()).append('\'');
      }
      out.append("]}");
    }
    out.append(']');
    return out.toString();
  }
}
