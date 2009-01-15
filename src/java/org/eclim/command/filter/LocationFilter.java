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
package org.eclim.command.filter;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

import org.eclim.util.file.Location;

import org.eclim.util.vim.VimUtils;

/**
 * Output filter for location results.
 *
 * @author Eric Van Dewoestine
 */
public class LocationFilter
  implements OutputFilter<List<Location>>
{
  public static final LocationFilter instance = new LocationFilter();

  /**
   * {@inheritDoc}
   */
  public String filter(CommandLine commandLine, List<Location> results)
  {
    try{
      StringBuffer buffer = new StringBuffer();
      if(results != null){
        for(Location result : results){
          String lineColumn = VimUtils.translateLineColumn(result);

          if(buffer.length() > 0){
            buffer.append('\n');
          }
          buffer.append(result.getFilename())
            .append('|')
            .append(lineColumn)
            .append('|')
            .append(result.getMessage() != null ? result.getMessage() : " ");
        }
      }
      return buffer.toString();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
}
