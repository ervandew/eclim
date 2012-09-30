/**
 * Copyright (C) 2012  Eric Van Dewoestine
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

import org.eclim.plugin.core.command.refactoring.AbstractRefactorCommand;
import org.eclim.plugin.core.command.refactoring.Refactor;
import org.eclim.plugin.core.command.refactoring.RefactorException;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import org.eclipse.jdt.internal.corext.refactoring.changes.CreatePackageChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.MoveCompilationUnitChange;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Messages;

import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;

import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;

import org.eclipse.jdt.ui.JavaElementLabels;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

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
    "REQUIRED d dest ARG"
)
public class MoveCommand
  extends AbstractRefactorCommand
{
  @Override
  public Refactor createRefactoring(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String packName = commandLine.getValue(Options.DEST_OPTION);

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

    String label;
    if (pack.isDefaultPackage()) {
      label = Messages.format(
          CorrectionMessages
            .ReorgCorrectionsSubProcessor_movecu_default_description,
          BasicElementLabels.getFileName(src));
    } else {
      String packageLabel = JavaElementLabels
        .getElementLabel(pack, JavaElementLabels.ALL_DEFAULT);
      label = Messages.format(
          CorrectionMessages
            .ReorgCorrectionsSubProcessor_movecu_description,
          new Object[] { BasicElementLabels.getFileName(src), packageLabel });
    }

    CompositeChange composite = new CompositeChange(label);
    composite.add(new CreatePackageChange(pack));
    composite.add(new MoveCompilationUnitChange(src, pack));

    return new Refactor(new MoveRefactoring(composite));
  }

  private class MoveRefactoring
    extends Refactoring
  {
    private Change change;
    private RefactoringStatus status;

    public MoveRefactoring(Change change)
    {
      this.change = change;
      this.status = new RefactoringStatus();
    }

    @Override
    public String getName()
    {
      return change.getName();
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
      throws CoreException,
             OperationCanceledException
    {
      return status;
    }

    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
      throws CoreException,
             OperationCanceledException
    {
      return status;
    }

    public Change createChange(IProgressMonitor pm)
      throws CoreException,
             OperationCanceledException
    {
      return change;
    }
  }
}
