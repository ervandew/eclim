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
package org.eclim.plugin.jdt.command.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.command.impl.ImplCommand;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import org.eclipse.jdt.internal.corext.codemanipulation.AddDelegateMethodsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.AddDelegateMethodsOperation.DelegateEntry;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2Core;

import org.eclipse.jdt.internal.corext.dom.ASTNodes;

import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;

import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;

import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

/**
 * Command to handle creation of delegate methods.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_delegate",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL v variable ARG," +
    "OPTIONAL s superType ARG," +
    "OPTIONAL m methods ARG"
)
public class DelegateCommand
  extends ImplCommand
{
  private ThreadLocal<IField> field = new ThreadLocal<IField>();

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    IJavaElement element = null;
    if (commandLine.hasOption(Options.VARIABLE_OPTION)){
      String var = commandLine.getValue(Options.VARIABLE_OPTION);
      String[] parts = StringUtils.split(var, ".");
      String varName = parts[parts.length - 1];
      IType type = getType(src, commandLine);
      element = type.getField(varName);
    }else{
      element = src.getElementAt(getOffset(commandLine));
    }

    if(element == null || element.getElementType() != IJavaElement.FIELD){
      return Services.getMessage("not.a.field");
    }

    IField field = (IField)element;
    String signature = field.getTypeSignature();
    IType delegateType = TypeUtils.findUnqualifiedType(
        src, Signature.getSignatureSimpleName(signature));

    if(delegateType == null){
      return Services.getMessage("type.not.found",
          src.getJavaProject().getElementName(),
          Signature.getSignatureSimpleName(signature)) + "  " +
        Services.getMessage("check.import");
    }

    this.field.set(field);
    return super.execute(commandLine);
  }

  @Override
  protected IType getType(ICompilationUnit src, CommandLine commandLine)
  {
    if (commandLine.hasOption(Options.VARIABLE_OPTION)){
      String var = commandLine.getValue(Options.VARIABLE_OPTION);
      String[] parts = StringUtils.split(var, ".");
      String typeName = StringUtils.join(parts, '.', 0, parts.length - 1);
      try{
        return src.getJavaProject().findType(typeName.replace('$', '.'));
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
    }
    return super.getType(src, commandLine);
  }

  @Override
  protected IWorkspaceRunnable getImplOperation(
      ICompilationUnit src,
      IType type,
      Set<String> chosen,
      IJavaElement sibling,
      int pos,
      CommandLine commandLine)
  {
    RefactoringASTParser parser =
      new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL);
    CompilationUnit cu = parser.parse(type.getCompilationUnit(), true);
    ITypeBinding typeBinding = null;
    try{
      typeBinding = ASTNodes.getTypeBinding(cu, type);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    String superType = commandLine.getValue(Options.SUPERTYPE_OPTION);
    List<DelegateEntry> delegatable = getDelegatableMethods(cu, typeBinding);
    List<DelegateEntry> delegate = new ArrayList<DelegateEntry>();
    for (DelegateEntry entry : delegatable){
      ITypeBinding declBinding = entry.delegateMethod.getDeclaringClass();
      String fqn = declBinding.getQualifiedName().replaceAll("<.*?>", "");
      if (fqn.equals(superType) && isChosen(chosen, entry.delegateMethod)){
        delegate.add(entry);
      }
    }

    if (delegate.size() > 0){
      CodeGenerationSettings settings =
          JavaPreferencesSettings.getCodeGenerationSettings(src.getJavaProject());
      settings.createComments = true;
      return new AddDelegateMethodsOperation(
          cu, delegate.toArray(new DelegateEntry[delegate.size()]),
          sibling, settings, true, true);
    }
    return null;
  }

  @Override
  protected ImplResult getImplResult(ICompilationUnit src, IType type)
  {
    List<DelegateEntry> delegatable = getDelegatableMethods(src, type);
    List<IMethodBinding> methods =
      new ArrayList<IMethodBinding>(delegatable.size());
    for (DelegateEntry entry : delegatable){
      methods.add(entry.delegateMethod);
    }

    String name = type.getFullyQualifiedName() + '.' + field.get().getElementName();
    return getImplResult(name, methods);
  }

  private List<DelegateEntry> getDelegatableMethods(
      ICompilationUnit src, IType type)
  {
    RefactoringASTParser parser =
      new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL);
    CompilationUnit cu = parser.parse(type.getCompilationUnit(), true);
    ITypeBinding typeBinding = null;
    try{
      typeBinding = ASTNodes.getTypeBinding(cu, type);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return getDelegatableMethods(cu, typeBinding);
  }

  private List<DelegateEntry> getDelegatableMethods(
      CompilationUnit cu, ITypeBinding typeBinding)
  {
    IVariableBinding variable = null;
    try{
      variable = ASTNodeSearchUtil
        .getFieldDeclarationFragmentNode(field.get(), cu).resolveBinding();
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    DelegateEntry[] entries =
      StubUtility2Core.getDelegatableMethods(typeBinding);
    ArrayList<DelegateEntry> delegatable = new ArrayList<DelegateEntry>();
    for (DelegateEntry entry : entries) {
      if (entry.field.equals(variable)){
        delegatable.add(entry);
      }
    }
    return delegatable;
  }
}
