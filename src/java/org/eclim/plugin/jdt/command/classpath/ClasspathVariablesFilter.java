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
package org.eclim.plugin.jdt.command.classpath;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Filter for ClasspathVariablesCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ClasspathVariablesFilter
  implements OutputFilter<List<ClasspathVariable>>
{
  public static final ClasspathVariablesFilter instance =
    new ClasspathVariablesFilter();

  /**
   * {@inheritDoc}
   */
  public String filter(CommandLine commandLine, List<ClasspathVariable> results)
  {
    StringBuffer buffer = new StringBuffer();

    Collections.sort(results);

    int length = 0;
    for(ClasspathVariable variable : results){
      length = variable.getName().length() > length ?
        variable.getName().length() : length;
    }

    for(ClasspathVariable variable : results){
      if(buffer.length() > 0){
        buffer.append('\n');
      }
      buffer.append(StringUtils.rightPad(variable.getName(), length))
        .append(" - ")
        .append(variable.getPath());
    }
    return buffer.toString();
  }
}
