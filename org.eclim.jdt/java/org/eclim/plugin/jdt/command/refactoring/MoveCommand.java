/**
 * Copyright (C) 2012 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.refactoring;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.plugin.core.command.refactoring.AbstractRefactorCommand;
import org.eclim.plugin.core.command.refactoring.Refactor;
import org.eclim.plugin.core.command.refactoring.RefactorException;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgDestination;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgDestinationFactory;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgPolicyFactory;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.internal.ui.refactoring.reorg.CreateTargetQueries;
import org.eclipse.jdt.internal.ui.refactoring.reorg.ReorgQueries;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;

import org.eclipse.swt.widgets.Shell;

/**
 * Command to move a compilation unit.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_refactor_move",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED n name ARG," +
    "OPTIONAL v preview NOARG," +
    "OPTIONAL d diff ARG"
)
public class MoveCommand
  extends AbstractRefactorCommand
{
  @Override
  public Refactor createRefactoring(CommandLine commandLine)
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String packName = commandLine.getValue(Options.NAME_OPTION);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IPackageFragmentRoot root = JavaModelUtil.getPackageFragmentRoot(src);
    IPackageFragment pack = root.getPackageFragment(packName);
    ICompilationUnit dest = pack.getCompilationUnit(src.getElementName());
    if (dest.exists()){
      throw new RefactorException(Services.getMessage(
            "move.element.exists",
            pack.getElementName(),
            src.getElementName()));
    }

    try{
      if(!pack.exists()){
        pack = root.createPackageFragment(packName, true, null);
      }

      IMovePolicy policy = ReorgPolicyFactory.createMovePolicy(
          new IResource[0],
          new IJavaElement[]{src});

      JavaMoveProcessor processor = new JavaMoveProcessor(policy);
      IReorgDestination destination =
        ReorgDestinationFactory.createDestination(pack);
      RefactoringStatus status = processor.setDestination(destination);
      if (status.hasError()){
        throw new RefactorException(status);
      }

      Shell shell = EclimPlugin.getShell();
      processor.setCreateTargetQueries(new CreateTargetQueries(shell));
      processor.setReorgQueries(new ReorgQueries(shell));

      Refactoring refactoring = new MoveRefactoring(processor);

      // create a more descriptive name than the default.
      String desc = refactoring.getName() +
        " (" + src.getElementName() + " -> " + pack.getElementName() + ')';

      return new Refactor(desc, refactoring);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }
}
