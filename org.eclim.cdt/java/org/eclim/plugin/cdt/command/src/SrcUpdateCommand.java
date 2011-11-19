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
package org.eclim.plugin.cdt.command.src;

import java.util.ArrayList;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.cdt.util.CUtils;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.CollectionUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.core.parser.IProblem;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;

import org.eclipse.core.runtime.NullProgressMonitor;

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
    "OPTIONAL v validate NOARG," +
    "OPTIONAL b build NOARG"
)
public class SrcUpdateCommand
  extends AbstractCommand
{
  // Taken from org.eclipse.cdt.internal.ui.refactoring.utils.TranslationUnitHelper
  private static final int AST_STYLE =
    ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT |
    ITranslationUnit.AST_SKIP_INDEXED_HEADERS;

  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    ICProject cproject = CUtils.getCProject(project);
    if(cproject.exists()){
      ITranslationUnit src = CUtils.getTranslationUnit(cproject, file);

      // refresh the index
      CCorePlugin.getIndexManager().update(
          new ICElement[]{src}, IIndexManager.UPDATE_ALL);

      if(commandLine.hasOption(Options.VALIDATE_OPTION)){
        List<IProblem> problems = getProblems(src);
        ArrayList<Error> errors = new ArrayList<Error>();
        String filename = src.getResource()
          .getRawLocation().toOSString().replace('\\', '/');
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

        if(commandLine.hasOption(Options.BUILD_OPTION)){
          project.build(
              IncrementalProjectBuilder.INCREMENTAL_BUILD,
              new NullProgressMonitor());
        }
        return errors;
      }
    }
    return null;
  }

  private List<IProblem> getProblems(ITranslationUnit tu)
    throws Exception
  {
    IIndex index = null;
    try {
      ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
      index = CCorePlugin.getIndexManager().getIndex(projects);
      index.acquireReadLock();

      IASTTranslationUnit ast = tu.getAST(index, AST_STYLE);
      ArrayList<IProblem> problems = new ArrayList<IProblem>();
      CollectionUtils.addAll(problems, ast.getPreprocessorProblems());
      CollectionUtils.addAll(problems, CPPVisitor.getProblems(ast));
      return problems;
    } finally {
      if (index != null){
        index.releaseReadLock();
      }
    }
  }
}
