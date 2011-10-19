/**
 * Copyright (C) 2011  Eric Van Dewoestine
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
package org.eclim.plugin.sdt.command.src;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.sdt.util.ScalaUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.compiler.IProblem;

import scala.tools.eclipse.javaelements.ScalaSourceFile;

/**
 * Command that updates the requested java src file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "scala_src_update",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL v validate NOARG," +
    "OPTIONAL b build NOARG"
)
public class SrcUpdateCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);

    ScalaSourceFile src = ScalaUtils.getSourceFile(projectName, file);

    if(commandLine.hasOption(Options.VALIDATE_OPTION)){
      // forcing a build prevents race condition after updating the changed
      // file before the problems are available.
      project.build(
          IncrementalProjectBuilder.INCREMENTAL_BUILD,
          new NullProgressMonitor());

      IProblem[] problems = src.getProblems();

      ArrayList<Error> errors = new ArrayList<Error>();
      if (problems != null){
        String path = ProjectUtils.getFilePath(project, file);
        FileOffsets offsets = FileOffsets.compile(path);
        for(int ii = 0; ii < problems.length; ii++){
          int[] lineColumn =
            offsets.offsetToLineColumn(problems[ii].getSourceStart());

          // one day vim might support ability to mark the offending text.
          /*int[] endLineColumn =
            offsets.offsetToLineColumn(problems[ii].getSourceEnd());*/

          errors.add(new Error(
              problems[ii].getMessage(),
              path,
              lineColumn[0],
              lineColumn[1],
              problems[ii].isWarning()));
        }
      }
      return errors;
    }
    return StringUtils.EMPTY;
  }
}
