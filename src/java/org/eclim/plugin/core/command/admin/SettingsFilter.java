/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.admin;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.command.OutputFilter;

import org.eclim.plugin.core.preference.Option;
import org.eclim.plugin.core.preference.OptionInstance;

import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Output filter for global settings.
 *
 * @author Eric Van Dewoestine
 */
public class SettingsFilter
  implements OutputFilter<Option[]>
{
  public static final SettingsFilter instance = new SettingsFilter();

  private static final String COMMENT = "# ";

  /**
   * {@inheritDoc}
   */
  public String filter(CommandLine commandLine, Option[] results)
  {
    if(results.length > 0){
      return printOptions(commandLine, results);
    }
    return null;
  }

  /**
   * Print supplied array of options.
   *
   * @param options The option array.
   * @return The result.
   */
  public String printOptions(CommandLine commandLine, Option[] options)
  {
    StringBuffer buffer = new StringBuffer();

    try{
      String projectName = commandLine.getValue(Options.PROJECT_OPTION);
      if (projectName != null){
        buffer.append("# Settings for project: ")
          .append(projectName).append("\n\n");
      }else{
        String workspace = ResourcesPlugin.getWorkspace().getRoot()
          .getRawLocation().toOSString();
        buffer.append("# Global settings for workspace: ")
          .append(workspace).append("\n\n");
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }

    // sort the list
    Arrays.sort(options);
    String lastPath = ((Option)options[0]).getPath();
    buffer.append(comment(lastPath, StringUtils.EMPTY)).append(" {");
    for(Option option : options){
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
  private String comment(String text, String indent)
  {
    return new StringBuffer()
      .append(indent)
      .append(COMMENT)
      .append(text.replaceAll("\n", "\n" + indent + COMMENT))
      .toString();
  }
}
