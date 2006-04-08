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
package org.eclim.plugin.ant.command.validate;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.ant.util.AntUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.ant.internal.ui.model.IAntModel;
import org.eclipse.ant.internal.ui.model.IProblem;
import org.eclipse.ant.internal.ui.model.IProblemRequestor;

/**
 * Command to handle ant file validation requests.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ValidateCommand
  extends org.eclim.command.xml.validate.ValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);

      List errors = super.validate(file);

      ProblemRequestor requestor = new ProblemRequestor();
      IAntModel model = AntUtils.getAntModel(file, requestor);
      model.reconcile();

      List problems = requestor.getProblems();
      FileOffsets offsets = FileOffsets.compile(file);
      for (Iterator ii = problems.iterator(); ii.hasNext();){
        IProblem problem = (IProblem)ii.next();
        int[] lineColumn = offsets.offsetToLineColumn(problem.getOffset());
        Error error = new Error(
          problem.getUnmodifiedMessage(), file,
          lineColumn[0], lineColumn[1],
          problem.isWarning());
        if(!errors.contains(error)){
          errors.add(error);
        }
      }
      return filter(_commandLine, errors.toArray(new Error[errors.size()]));
    }catch(Exception e){
      return e;
    }
  }

  /**
   * IProblemRequestor implementation to gather validation errors from the ant
   * model.
   */
  private class ProblemRequestor
    implements IProblemRequestor
  {
    private List problems = new ArrayList();

    public List getProblems ()
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
