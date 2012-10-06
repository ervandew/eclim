/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.src;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;

/**
 * Command used to format source code in the way Source / Format menu does it.
 *
 * @author Anton Sharonov
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_format",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED h hoffset ARG," +
    "REQUIRED t toffset ARG," +
    "REQUIRED e encoding ARG"
)
public class FormatCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    int headOffset = getOffset(commandLine, "h");
    int tailOffset = getOffset(commandLine, "t");
    int kind = CodeFormatter.K_COMPILATION_UNIT |
      CodeFormatter.F_INCLUDE_COMMENTS;

    JavaUtils.format(src, kind, headOffset, tailOffset - headOffset);

    return null;
  }
}
