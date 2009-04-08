/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.cdt.command.src;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.annotation.Command;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.util.CollectionUtils;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.core.parser.IProblem;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.utils.TranslationUnitHelper;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.Path;

/**
 * Command to update the file on the eclipse side and optionally validate it.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "c_src_update",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL v validate NOARG"
)
public class SrcUpdateCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
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
      IProject project = ProjectUtils.getProject(projectName);
      ICProject cproject = CoreModel.getDefault().create(project);
      if(cproject.exists()){
        Path path = new Path(ProjectUtils.getFilePath(project, file));
        ITranslationUnit src =
          CoreModelUtil.findTranslationUnitForLocation(path, cproject);

        List<IProblem> problems = getProblems(src);
        ArrayList<Error> errors = new ArrayList<Error>();
        String filename = src.getResource().getRawLocation().toOSString();
        FileOffsets offsets = FileOffsets.compile(filename);
        for(IProblem problem : problems){
          int[] lineColumn =
            offsets.offsetToLineColumn(problem.getSourceStart());

          errors.add(new Error(
              problem.getMessage(),
              filename,
              lineColumn[0],
              lineColumn[1],
              problem.isWarning()));
        }

        return ErrorFilter.instance.filter(commandLine, errors);
      }
    }
    return StringUtils.EMPTY;
  }

  private List<IProblem> getProblems(ITranslationUnit tu)
    throws Exception
  {
    //IASTTranslationUnit ast = tu.getAST();
    String filename = tu.getResource().getRawLocation().toOSString();
    IASTTranslationUnit ast =
      TranslationUnitHelper.loadTranslationUnit(filename, true);
    ArrayList<IProblem> problems = new ArrayList<IProblem>();
    CollectionUtils.addAll(problems, ast.getPreprocessorProblems());
    CollectionUtils.addAll(problems, CPPVisitor.getProblems(ast));
    return problems;
  }
}
