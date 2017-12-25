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
package org.eclim.plugin.wst.command.src;

import java.util.ArrayList;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.wst.util.JavaScriptUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.wst.jsdt.core.IJavaScriptUnit;

import org.eclipse.wst.jsdt.core.compiler.IProblem;

/**
 * Command that updates teh requested javascript src file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "javascript_src_update",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL v validate NOARG"
)
public class JavaScriptSrcUpdateCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);

    // only refresh the file.
    if(!commandLine.hasOption(Options.VALIDATE_OPTION)){
      // getting the file will refresh it.
      ProjectUtils.getFile(projectName, file);

    // validate the src file.
    }else{
      // JavaScriptUtils refreshes the file when getting it.
      IJavaScriptUnit src = JavaScriptUtils.getJavaScriptUnit(projectName, file);

      IJavaScriptUnit workingCopy = src.getWorkingCopy(null);

      ProblemRequestor requestor = new ProblemRequestor();
      try{
        workingCopy.discardWorkingCopy();
        workingCopy.becomeWorkingCopy(requestor, null);
      }finally{
        workingCopy.discardWorkingCopy();
      }
      List<IProblem> problems = requestor.getProblems();

      ArrayList<Error> errors = new ArrayList<Error>();
      String filename = src.getResource().getLocation().toOSString();
      FileOffsets offsets = FileOffsets.compile(filename);
      for(IProblem problem : problems){
        int[] lineColumn =
          offsets.offsetToLineColumn(problem.getSourceStart());

        // one day vim might support ability to mark the offending text.
        /*int[] endLineColumn =
          offsets.offsetToLineColumn(problem.getSourceEnd());*/

        errors.add(new Error(
            problem.getMessage(),
            filename,
            lineColumn[0],
            lineColumn[1],
            problem.isWarning()));
      }

      return errors;
    }
    return null;
  }

  /**
   * Gathers problems as a src file is processed.
   */
  public static class ProblemRequestor
    implements org.eclipse.wst.jsdt.core.IProblemRequestor
  {
    private ArrayList<IProblem> problems = new ArrayList<IProblem>();

    /**
     * Gets a list of problems recorded.
     *
     * @return The list of problems.
     */
    public List<IProblem> getProblems()
    {
      return problems;
    }

    @Override
    public void acceptProblem(IProblem problem)
    {
      problems.add(problem);
    }

    @Override
    public void beginReporting(){}

    @Override
    public void endReporting(){}

    @Override
    public boolean isActive()
    {
      return true;
    }
  }
}
