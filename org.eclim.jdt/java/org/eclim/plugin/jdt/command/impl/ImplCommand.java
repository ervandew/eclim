/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.MethodUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import org.eclipse.jdt.core.formatter.CodeFormatter;

import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedMethodsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;

import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;

import com.google.gson.Gson;

/**
 * Command used to build a tree of methods that have or can be
 * implemented/overriden by the supplied file according the interfaces/parent
 * class it implements/extends.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_impl",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL s superType ARG," +
    "OPTIONAL m methods ARG"
)
public class ImplCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IType type = getType(src, commandLine);

    // if a supertype option is supplied, then add methods as necessary.
    if (commandLine.hasOption(Options.SUPERTYPE_OPTION)){
      insertMethods(src, type, commandLine);
    }

    return getImplResult(src, type);
  }

  /**
   * Gets the type to be edited.
   *
   * @param src The ICompilationUnit of the source file to edit.
   * @param commandLine The command line.
   * @return The IType to be edited.
   */
  protected IType getType(ICompilationUnit src, CommandLine commandLine)
    throws Exception
  {
    // get the type to be modified by name or its offset in the file.
    return commandLine.hasOption(Options.TYPE_OPTION) ?
      src.getJavaProject().findType(
          commandLine.getValue(Options.TYPE_OPTION).replace('$', '.')) :
      TypeUtils.getType(src, getOffset(commandLine));
  }

  /**
   * Get the operation used to add the requested methods.
   *
   * @param src The ICompilationUnit of the source file to edit.
   * @param type The IType of the type in the source to edit.
   * @param chosen A set containing method signatures to add or null to add all
   *   methods from the chosen super type.
   * @param sibling The sibling to insert the methods before.
   * @param pos The position of the sibling.
   * @param commandLine The command line.
   *
   * @return An IWorkspaceRunnable
   */
  protected IWorkspaceRunnable getImplOperation(
      ICompilationUnit src,
      IType type,
      Set<String> chosen,
      IJavaElement sibling,
      int pos,
      CommandLine commandLine)
    throws Exception
  {
    RefactoringASTParser parser =
      new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL);
    CompilationUnit cu = parser.parse(type.getCompilationUnit(), true);
    ITypeBinding typeBinding = ASTNodes.getTypeBinding(cu, type);

    String superType = commandLine.getValue(Options.SUPERTYPE_OPTION);
    List<IMethodBinding> overridable = getOverridableMethods(cu, typeBinding);
    List<IMethodBinding> override = new ArrayList<IMethodBinding>();
    for (IMethodBinding binding : overridable){
      ITypeBinding declBinding = binding.getDeclaringClass();
      String fqn = declBinding.getQualifiedName().replaceAll("<.*?>", "");
      if (fqn.equals(superType) && isChosen(chosen, binding)){
        override.add(binding);
      }
    }

    if (override.size() > 0){
      return new AddUnimplementedMethodsOperation(
          cu, typeBinding,
          override.toArray(new IMethodBinding[override.size()]),
          pos, true, true, true);
    }
    return null;
  }

  /**
   * Checks if the supplied IMethodBinding is in the set of chosen methods to
   * insert.
   *
   * @param chosen The set of chosen method signatures.
   * @param methodBinding The IMethodBinding to check.
   * @return True if the method is in the chosen set, false otherwise.
   */
  protected boolean isChosen(Set<String> chosen, IMethodBinding methodBinding)
  {
    return chosen == null ||
      chosen.contains(getMethodBindingShortCallSignature(methodBinding));
  }

  /**
   * Get the ImplResult containing super types and their methods that can be
   * added to the supplied source.
   *
   * @param src The ICompilationUnit of the source file.
   * @param type The IType of the type in the source that methods would be
   *   modified.
   * @return An ImplResult
   */
  protected ImplResult getImplResult(ICompilationUnit src, IType type)
    throws Exception
  {
    List<IMethodBinding> overridable = getOverridableMethods(src, type);
    return getImplResult(type.getFullyQualifiedName(), overridable);
  }

  /**
   * Get the ImplResult containing super types and their methods that can be
   * added to the supplied source.
   *
   * @param name The name of the type in the source that methods would be
   *   added to.
   * @param methods List of IMethodBinding representing the available methods.
   * @return An ImplResult
   */
  protected ImplResult getImplResult(String name, List<IMethodBinding> methods)
  {
    ArrayList<ImplType> results = new ArrayList<ImplType>();
    ArrayList<String> overrideMethods = null;
    ITypeBinding curTypeBinding = null;
    for (IMethodBinding methodBinding : methods) {
      ITypeBinding typeBinding = methodBinding.getDeclaringClass();
      if (typeBinding != curTypeBinding){
        if (overrideMethods != null && overrideMethods.size() > 0){
          results.add(createImplType(curTypeBinding, overrideMethods));
        }
        overrideMethods = new ArrayList<String>();
      }
      curTypeBinding = typeBinding;
      overrideMethods.add(getMethodBindingSignature(methodBinding));
    }
    if (overrideMethods != null && overrideMethods.size() > 0){
      results.add(createImplType(curTypeBinding, overrideMethods));
    }

    return new ImplResult(name, results);
  }

  /**
   * Gets a list of overridable IMethodBindings.
   *
   * @param src The source file.
   * @param type The type within the source file.
   * @return List of IMethodBinding.
   */
  protected List<IMethodBinding> getOverridableMethods(
      ICompilationUnit src, IType type)
    throws Exception
  {
    RefactoringASTParser parser =
      new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL);
    CompilationUnit cu = parser.parse(type.getCompilationUnit(), true);
    ITypeBinding typeBinding = ASTNodes.getTypeBinding(cu, type);
    return getOverridableMethods(cu, typeBinding);
  }

  /**
   * Gets a list of overridable IMethodBindings.
   *
   * @param cu AST CompilationUnit.
   * @param typeBinding The binding of the type with the CompilationUnit.
   * @return List of IMethodBinding.
   */
  protected List<IMethodBinding> getOverridableMethods(
      CompilationUnit cu, ITypeBinding typeBinding)
    throws Exception
  {
    if(!typeBinding.isClass()){
      throw new IllegalArgumentException(
          Services.getMessage("type.not.a.class", typeBinding.getQualifiedName()));
    }

    IPackageBinding packageBinding = typeBinding.getPackage();
    IMethodBinding[] methods =
      StubUtility2.getOverridableMethods(cu.getAST(), typeBinding, false);
    ArrayList<IMethodBinding> overridable = new ArrayList<IMethodBinding>();
    for (IMethodBinding methodBinding : methods) {
      if (Bindings.isVisibleInHierarchy(methodBinding, packageBinding)){
        overridable.add(methodBinding);
      }
    }
    return overridable;
  }

  private ImplType createImplType(
      ITypeBinding typeBinding, List<String> overridable)
  {
    String packageName = typeBinding.getPackage().getName();
    String qualifiedBindingName = typeBinding.getQualifiedName();
    // Take the remaining part after package name (and following dot) to be the
    // binding name so as to correctly identify nested classes/interfaces
    String bindingName = qualifiedBindingName.substring(packageName.length() + 1);
    String signature =
      (typeBinding.isInterface() ? "interface " : "class ") +
      bindingName.replaceAll("#RAW", "");
    return new ImplType(
        packageName,
        signature,
        overridable.toArray(new String[overridable.size()]));
  }

  private void insertMethods(
      ICompilationUnit src, IType type, CommandLine commandLine)
    throws Exception
  {
    String methodsOption = commandLine.getValue(Options.METHOD_OPTION);
    HashSet<String> chosen = null;
    if(methodsOption != null){
      chosen = new HashSet<String>();
      String[] sigs = new Gson().fromJson(methodsOption, String[].class);
      for (String sig : sigs){
        chosen.add(sig.replace(" ", ""));
      }
    }

    int pos = -1;
    int len = src.getBuffer().getLength();
    IJavaElement sibling = getSibling(type);
    if (sibling != null){
      pos = ((ISourceReference)sibling).getSourceRange().getOffset();
    }

    IWorkspaceRunnable op = getImplOperation(
        src, type, chosen, sibling, pos, commandLine);
    if (op != null){
      String lineDelim = src.findRecommendedLineSeparator();
      IImportDeclaration[] imports = src.getImports();
      int importsEnd = -1;
      if (imports.length > 0){
        ISourceRange last = imports[imports.length - 1].getSourceRange();
        importsEnd = last.getOffset() + last.getLength() + lineDelim.length();
      }

      op.run(null);

      // an op.getResultingEdit() would be nice here, but we'll make do w/ what
      // we got and caculate our own edit offset/length combo so we can format
      // the new code.
      int offset = pos != -1 ? pos : (len - 1 - lineDelim.length());
      int newLen = src.getBuffer().getLength();
      int length = newLen - len - 1;

      // the change in length may include newly added imports, so handle that as
      // best we can
      int importLenChange = 0;
      imports = src.getImports();
      if (importsEnd != -1){
        ISourceRange last = imports[imports.length - 1].getSourceRange();
        importLenChange = last.getOffset() + last.getLength() +
          lineDelim.length() - importsEnd;
      }else if(imports.length > 0){
        ISourceRange first = imports[0].getSourceRange();
        ISourceRange last = imports[imports.length - 1].getSourceRange();
        importLenChange = last.getOffset() + last.getLength() +
          (lineDelim.length() * 2) - first.getOffset();
      }

      offset += importLenChange;
      length -= importLenChange;

      JavaUtils.format(src, CodeFormatter.K_COMPILATION_UNIT, offset, length);
    }
  }

  private String getMethodBindingSignature(IMethodBinding binding)
  {
    return binding.toString().trim()
      .replaceAll("\\bjava\\.lang\\.", "")
      .replaceAll("\\s+throws\\s+.*", "")
      .replaceFirst("\\w+\\s*\\(.*?\\)", getMethodBindingCallSignature(binding));
  }

  private String getMethodBindingCallSignature(IMethodBinding binding)
  {
    ITypeBinding[] paramTypes = binding.getParameterTypes();
    String[] params = new String[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++){
      params[i] = paramTypes[i].getQualifiedName()
        .replaceAll("\\bjava\\.lang\\.", "")
        .replaceAll("#RAW", "");
    }
    return binding.getName() + '(' + StringUtils.join(params, ',') + ')';
  }

  private String getMethodBindingShortCallSignature(IMethodBinding binding)
  {
    return getMethodBindingCallSignature(binding).replaceAll("<.*?>", "");
  }

  private IJavaElement getSibling(IType type)
    throws Exception
  {
    IJavaElement sibling = null;

    // insert after last method
    IMethod[] methods = type.getMethods();
    if (methods.length > 0){
      sibling = MethodUtils.getMethodAfter(type, methods[methods.length - 1]);
    }

    // insert before inner classes.
    if (sibling == null){
      IType[] types = type.getTypes();
      // find the first non-enum type.
      for (int ii = 0; ii < types.length; ii++){
        if(!types[ii].isEnum()){
          sibling = types[ii];
          break;
        }
      }
    }

    return sibling;
  }
}
