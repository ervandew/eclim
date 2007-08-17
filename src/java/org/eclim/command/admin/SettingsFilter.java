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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SettingsFilter
  implements OutputFilter<List<Option>>
{
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
