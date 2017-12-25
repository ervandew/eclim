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
package org.eclim.plugin.jdt.command.refactoring;

import java.lang.reflect.Field;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.refactoring.AbstractRefactorCommand;
import org.eclim.plugin.core.command.refactoring.Refactor;
import org.eclim.plugin.core.command.refactoring.RefactorException;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;

import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.MethodChecks;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameEnumConstProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameLocalVariableProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenamePackageProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeParameterProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor;

import org.eclipse.jdt.internal.corext.refactoring.tagging.IReferenceUpdating;
import org.eclipse.jdt.internal.corext.refactoring.tagging.ITextUpdating;

import org.eclipse.jdt.internal.corext.util.JdtFlags;

import org.eclipse.jdt.ui.refactoring.RenameSupport;

import org.eclipse.ltk.core.refactoring.Refactoring;

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
    "REQUIRED e encoding ARG," +
    "OPTIONAL v preview NOARG," +
    "OPTIONAL d diff ARG"
)
public class RenameCommand
  extends AbstractRefactorCommand
{
  @Override
  public Refactor createRefactoring(CommandLine commandLine)
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String name = commandLine.getValue(Options.NAME_OPTION);
    int offset = getOffset(commandLine);
    int length = commandLine.getIntValue(Options.LENGTH_OPTION);
    //int flags = RenameSupport.NONE;
    int flags = RenameSupport.UPDATE_REFERENCES;

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    try{
      IJavaElement[] elements = src.codeSelect(offset, length);
      if(elements == null || elements.length == 0){
        throw new RefactorException();
      }

      IJavaElement element = elements[0];

      // check for element outside any user project
      if (element instanceof IMember){
        ICompilationUnit cu = ((IMember)element).getCompilationUnit();
        if (cu == null){
          throw new RefactorException(Services.getMessage(
                "rename.element.unable", element.getElementName()));
        }
      }

      JavaRenameProcessor processor = getProcessor(element, name, flags);
      Refactoring refactoring = new RenameRefactoring(processor);

      // create a more descriptive name than the default.
      String desc = refactoring.getName() +
        " (" + element.getElementName() + " -> " + name + ')';

      return new Refactor(desc, refactoring);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  /**
   * Get the JavaRenameProcessor for the given element.
   *
   * @param element The IJavaElement
   * @param name The new name for the element.
   * @param flags Rename operation flags.
   * @return The JavaRenameProcessor or null if the element is unsupported.
   */
  private JavaRenameProcessor getProcessor(
      IJavaElement element, String name, int flags)
  {
    JavaRenameProcessor processor;
    try{
      if (element instanceof IPackageFragment){
        processor = new RenamePackageProcessor((IPackageFragment)element);
        // hack to force renaming of sub packages.
        Field renameSubpackages =
          RenamePackageProcessor.class.getDeclaredField("fRenameSubpackages");
        renameSubpackages.setAccessible(true);
        renameSubpackages.setBoolean(processor, true);
      }else if (element instanceof IType){
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
          flags |= RenameSupport.UPDATE_GETTER_METHOD;
          flags |= RenameSupport.UPDATE_SETTER_METHOD;
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
        return null;
      }
    }catch(NoSuchFieldException nsfe){
      throw new RuntimeException(nsfe);
    }catch(IllegalAccessException iae){
      throw new RuntimeException(iae);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    processor.setNewElementName(name);
    if (processor instanceof IReferenceUpdating) {
      IReferenceUpdating reference = (IReferenceUpdating)processor;
      reference.setUpdateReferences((flags & RenameSupport.UPDATE_REFERENCES) != 0);
    }

    if (processor instanceof ITextUpdating) {
      ITextUpdating text = (ITextUpdating)processor;
      @SuppressWarnings("deprecation")
      int TEXT_UPDATES =
        RenameSupport.UPDATE_TEXTUAL_MATCHES |
        RenameSupport.UPDATE_REGULAR_COMMENTS |
        RenameSupport.UPDATE_STRING_LITERALS;
      text.setUpdateTextualMatches((flags & TEXT_UPDATES) != 0);
    }

    return processor;
  }
}
