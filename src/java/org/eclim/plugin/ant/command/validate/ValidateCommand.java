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
package org.eclim.plugin.ant.command.validate;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.plugin.ant.util.AntUtils;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileOffsets;
import org.eclim.util.file.FileUtils;

import org.eclipse.ant.internal.ui.model.IAntModel;
import org.eclipse.ant.internal.ui.model.IProblem;
import org.eclipse.ant.internal.ui.model.IProblemRequestor;

/**
 * Command to handle ant file validation requests.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ValidateCommand
  extends org.eclim.command.xml.validate.ValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String project = _commandLine.getValue(Options.PROJECT_OPTION);
    String file = _commandLine.getValue(Options.FILE_OPTION);

    List<Error> errors = super.validate(project, file, false, null);

    ProblemRequestor requestor = new ProblemRequestor();
    IAntModel model = AntUtils.getAntModel(project, file, requestor);
    model.reconcile();

    String filepath = FileUtils.concat(ProjectUtils.getPath(project), file);

    List<IProblem> problems = requestor.getProblems();
    FileOffsets offsets = FileOffsets.compile(filepath);
    for (IProblem problem : problems){
      int[] lineColumn = offsets.offsetToLineColumn(problem.getOffset());
      Error error = new Error(
        problem.getUnmodifiedMessage(), filepath,
        lineColumn[0], lineColumn[1],
        problem.isWarning());
      if(!errors.contains(error)){
        errors.add(error);
      }
    }
    return ErrorFilter.instance.filter(_commandLine, errors);
  }

  /**
   * IProblemRequestor implementation to gather validation errors from the ant
   * model.
   */
  private class ProblemRequestor
    implements IProblemRequestor
  {
    private ArrayList<IProblem> problems = new ArrayList<IProblem>();

    public List<IProblem> getProblems ()
    {
      return problems;
    }

    public void acceptProblem (IProblem problem)
    {
      problems.add(problem);
    }

    public void beginReporting()
    {
    }

    public void endReporting()
    {
    }
  }
}
