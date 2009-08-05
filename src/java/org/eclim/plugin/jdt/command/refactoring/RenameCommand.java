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
package org.eclim.plugin.jdt.command.refactoring;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;

import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.MethodChecks;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameEnumConstProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameLocalVariableProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeParameterProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor;

import org.eclipse.jdt.internal.corext.refactoring.tagging.IReferenceUpdating;
import org.eclipse.jdt.internal.corext.refactoring.tagging.ITextUpdating;

import org.eclipse.jdt.internal.corext.util.JdtFlags;

import org.eclipse.jdt.ui.refactoring.RenameSupport;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

/**
 * Command to rename a java element.
 *
 * Functionality gleaned from RenameSupport and RefactoringExecutionHelper.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_refactor_rename",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED n name ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED l length ARG," +
    "REQUIRED e encoding ARG"
)
public class RenameCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String name = commandLine.getValue(Options.NAME_OPTION);
    int offset = getOffset(commandLine);
    int length = commandLine.getIntValue(Options.LENGTH_OPTION);
    //int flags = RenameSupport.NONE;
    int flags = RenameSupport.UPDATE_REFERENCES;

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IJavaElement[] elements = src.codeSelect(offset, length);
    if(elements == null || elements.length == 0){
      return StringUtils.EMPTY;
    }

    IJavaElement element = elements[0];
    JavaRenameProcessor processor;
    if (element instanceof IType){
      processor = new RenameTypeProcessor((IType)element);
    }else if (element instanceof IMethod){
      IMethod method = (IMethod)element;
      if (MethodChecks.isVirtual(method)) {
        processor = new RenameVirtualMethodProcessor(method);
      } else {
        processor = new RenameNonVirtualMethodProcessor(method);
      }
    }else if (element instanceof IField){
      IField field = (IField)element;
      if (JdtFlags.isEnum(field)){
        processor = new RenameEnumConstProcessor(field);
      }else {
        RenameFieldProcessor renameField = new RenameFieldProcessor(field);
        renameField.setRenameGetter(
            (flags & RenameSupport.UPDATE_GETTER_METHOD) != 0);
        renameField.setRenameSetter(
            (flags & RenameSupport.UPDATE_SETTER_METHOD) != 0);
        processor = renameField;
      }
    }else if (element instanceof ITypeParameter){
      processor = new RenameTypeParameterProcessor((ITypeParameter)element);
    }else if (element instanceof ILocalVariable){
      processor = new RenameLocalVariableProcessor((ILocalVariable)element);
    }else{
      throw new RuntimeException("Rename of element current unsupported: " + element);
    }
    initialize(processor, name, flags);

    RenameRefactoring refactoring = new RenameRefactoring(processor);

    NullProgressMonitor monitor = new NullProgressMonitor();
    RefactoringStatus status = refactoring.checkAllConditions(
        new SubProgressMonitor(monitor, 4));
    int stopSeverity = RefactoringCore.getConditionCheckingFailedSeverity();
    if (status.getSeverity() >= stopSeverity) {
      throw new RuntimeException(status.toString());
    }

    Change change = refactoring.createChange(new SubProgressMonitor(monitor, 2));
    change.initializeValidationData(new SubProgressMonitor(monitor, 1));

    PerformChangeOperation changeOperation = new PerformChangeOperation(change);
    changeOperation.setUndoManager(
        RefactoringCore.getUndoManager(), refactoring.getName());
    changeOperation.run(new SubProgressMonitor(monitor, 4));
    return "Rename completed.";
  }

  private static void initialize(
      JavaRenameProcessor processor, String newName, int flags)
  {
    processor.setNewElementName(newName);
    if (processor instanceof IReferenceUpdating) {
      IReferenceUpdating reference = (IReferenceUpdating)processor;
      reference.setUpdateReferences((flags & RenameSupport.UPDATE_REFERENCES) != 0);
    }

    if (processor instanceof ITextUpdating) {
      ITextUpdating text = (ITextUpdating)processor;
      int TEXT_UPDATES =
        RenameSupport.UPDATE_TEXTUAL_MATCHES |
        RenameSupport.UPDATE_REGULAR_COMMENTS |
        RenameSupport.UPDATE_STRING_LITERALS;
      text.setUpdateTextualMatches((flags & TEXT_UPDATES) != 0);
    }
  }
}
