/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Command to determines if the specified src file exists.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_src_exists",
  options =
    "REQUIRED f file ARG," +
    "OPTIONAL p project ARG"
)
public class SrcFileExistsCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(SrcFileExistsCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    try{
      String file = commandLine.getValue(Options.FILE_OPTION);
      String projectName = commandLine.getValue(Options.PROJECT_OPTION);

      if(projectName != null){
        ICompilationUnit src = JavaUtils.findCompilationUnit(projectName, file);
        return src != null && src.exists() ?
          Boolean.TRUE.toString() : Boolean.FALSE.toString();
      }
      ICompilationUnit src = JavaUtils.findCompilationUnit(file);
      return src != null && src.exists() ?
        Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }catch(IllegalArgumentException iae){
      logger.error("Error locating java source file.", iae);
      return Boolean.FALSE.toString();
    }
  }
}
