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

import java.util.ArrayList;
import java.util.List;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Command that updates the requested java src file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_src_update",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL v validate NOARG," +
    "OPTIONAL b build NOARG"
)
public class SrcUpdateCommand
  extends AbstractCommand
{
  @Override
  @SuppressWarnings("unchecked")
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);

    // only refresh the file.
    if(!commandLine.hasOption(Options.VALIDATE_OPTION)){
      // getting the file will refresh it.
      ProjectUtils.getFile(project, file);

    // validate the src file.
    }else{
      // JavaUtils refreshes the file when getting it.
      IJavaProject javaProject = JavaUtils.getJavaProject(project);
      ICompilationUnit src = JavaUtils.getCompilationUnit(javaProject, file);

      IProblem[] problems = JavaUtils.getProblems(src);

      ArrayList<Error> errors = new ArrayList<Error>();
      String filename = src.getResource()
        .getLocation().toOSString().replace('\\', '/');
      FileOffsets offsets = FileOffsets.compile(filename);
      for(int ii = 0; ii < problems.length; ii++){
        int[] lineColumn =
          offsets.offsetToLineColumn(problems[ii].getSourceStart());

        // one day vim might support ability to mark the offending text.
        /*int[] endLineColumn =
          offsets.offsetToLineColumn(problems[ii].getSourceEnd());*/

        errors.add(new Error(
            problems[ii].getMessage(),
            filename,
            lineColumn[0],
            lineColumn[1],
            problems[ii].isWarning()));
      }

      boolean checkstyle = "true".equals(getPreferences()
          .getValue(project, "org.eclim.java.checkstyle.onvalidate"));
      if (checkstyle){
        errors.addAll((List<Error>)
            Services.getCommand("java_checkstyle").execute(commandLine));
      }

      if(commandLine.hasOption(Options.BUILD_OPTION)){
        project.build(
            IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
      }
      return errors;
    }
    return null;
  }
}
