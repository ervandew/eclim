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
package org.eclim.command.admin;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

import org.eclim.preference.Option;
import org.eclim.preference.OptionInstance;

/**
 * Output filter for global settings.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SettingsFilter
  implements OutputFilter<List<Option>>
{
  public static final SettingsFilter instance = new SettingsFilter();

  private static final String COMMENT = "# ";

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<Option> _result)
  {
    if(_result.size() > 0){
      return printOptions(_result);
    }
    return StringUtils.EMPTY;
  }

  /**
   * Print supplied list of options.
   *
   * @param _options The option list.
   * @return The result.
   */
  public String printOptions (List<Option> _options)
  {
    StringBuffer buffer = new StringBuffer();

    // sort the list
    Collections.sort(_options);
    String lastPath = ((Option)_options.get(0)).getPath();
    buffer.append(comment(lastPath, StringUtils.EMPTY)).append(" {");
    for(Option option : _options){
      OptionInstance instance = (OptionInstance)option;
      if(!option.getPath().equals(lastPath)){
        lastPath = option.getPath();
        buffer.append("\n# }\n\n")
          .append(comment(lastPath, StringUtils.EMPTY))
          .append(" {");
      }

      buffer.append('\n')
        .append(comment(option.getDescription(), "\t"))
        .append("\n\t");
      buffer.append(option.getName()).append('=').append(instance.getValue());
    }
    buffer.append("\n# }");
    return buffer.toString();
  }

  /**
   * Generate a comment string using the supplied text and indentation.
   *
   * @param text The text.
   * @param indent The indentation.
   * @return The comment.
   */
  private String comment (String text, String indent)
  {
    return new StringBuffer()
      .append(indent)
      .append(COMMENT)
      .append(text.replaceAll("\n", "\n" + indent + COMMENT))
      .toString();
  }
}
