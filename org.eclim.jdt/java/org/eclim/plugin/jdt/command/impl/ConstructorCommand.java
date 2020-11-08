/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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

import java.util.Arrays;
import java.util.Comparator;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import org.eclipse.jdt.core.formatter.CodeFormatter;

import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;

import org.eclipse.jdt.internal.corext.codemanipulation.AddCustomConstructorOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2Core;

import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;

import org.eclipse.jdt.internal.ui.actions.ActionMessages;

import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

import org.eclipse.text.edits.TextEdit;

import com.google.gson.Gson;

/**
 * Command used to generate class constructors.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_constructor",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL r properties ARG," +
    "OPTIONAL s super NOARG"
)
public class ConstructorCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String propertiesOption = commandLine.getValue(Options.PROPERTIES_OPTION);
    int offset = getOffset(commandLine);

    String[] properties = {};
    if(propertiesOption != null){
      properties = new Gson().fromJson(propertiesOption, String[].class);
    }

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IType type = TypeUtils.getType(src, offset);

    CompilationUnit cu = SharedASTProviderCore
      .getAST(src, SharedASTProviderCore.WAIT_YES, null);
    ITypeBinding typeBinding = ASTNodes.getTypeBinding(cu, type);
    if (typeBinding.isAnonymous()) {
      return ActionMessages
        .GenerateConstructorUsingFieldsAction_error_anonymous_class;
    }

    IVariableBinding[] variables = new IVariableBinding[properties.length];
    for(int ii = 0; ii < properties.length; ii++){
      IField field = type.getField(properties[ii]);
      if(!field.exists()){
        return Services.getMessage("field.not.found",
            properties[ii], type.getElementName());
      }
      variables[ii] = ASTNodeSearchUtil
        .getFieldDeclarationFragmentNode(field, cu).resolveBinding();
    }

    if(findExistingConstructor(type, properties).exists()){
      return Services.getMessage("constructor.already.exists",
          type.getElementName() + '(' + buildParams(type, properties) + ')');
    }

    IMethodBinding constructor =
      findParentConstructor(cu, typeBinding, variables);
    if (constructor == null){
      return ActionMessages
        .GenerateConstructorUsingFieldsAction_error_nothing_found;
    }

    insertConstructor(src, cu, typeBinding, variables, constructor, commandLine);

    return null;
  }

  private void insertConstructor(
      ICompilationUnit src,
      CompilationUnit cu,
      ITypeBinding typeBinding,
      IVariableBinding[] variables,
      IMethodBinding constructor,
      CommandLine commandLine)
    throws Exception
  {
    CodeGenerationSettings settings = JavaPreferencesSettings
      .getCodeGenerationSettings(src.getJavaProject());
    settings.createComments = true;
    boolean isDefault = constructor.getDeclaringClass()
      .getQualifiedName().equals("java.lang.Object") ||
      constructor.isDefaultConstructor();

    AddCustomConstructorOperation op = new AddCustomConstructorOperation(
        cu, typeBinding,
        variables, constructor,
        getSibling((IType)typeBinding.getJavaElement()), settings, true, true);
    op.setOmitSuper(!commandLine.hasOption(Options.SUPERTYPE_OPTION) || isDefault);
    if (!typeBinding.isEnum()){
      op.setVisibility(Modifier.PUBLIC);
    }
    op.run(null);

    TextEdit edit = op.getResultingEdit();
    if (edit != null){
      edit = edit.getChildren()[0];
      JavaUtils.format(
          src, CodeFormatter.K_COMPILATION_UNIT,
          edit.getOffset(), edit.getLength());
    }
  }

  private IMethod findExistingConstructor(IType type, String[] properties)
    throws Exception
  {
    if(properties.length == 0){
      return type.getMethod(type.getElementName(), null);
    }

    String[] fieldSigs = new String[properties.length];
    for (int ii = 0; ii < properties.length; ii++){
      fieldSigs[ii] = type.getField(properties[ii]).getTypeSignature();
    }
    return type.getMethod(type.getElementName(), fieldSigs);
  }

  private IMethodBinding findParentConstructor(
      CompilationUnit cu, ITypeBinding typeBinding, IVariableBinding[] variables)
  {
    IMethodBinding constructor = null;
    if (typeBinding.isEnum() || variables.length != 0) {
      ITypeBinding binding = cu.getAST().resolveWellKnownType("java.lang.Object");
      constructor = Bindings.findMethodInType(
          binding, "Object", new ITypeBinding[0]);
    } else {
      IMethodBinding[] bindings = StubUtility2Core
        .getVisibleConstructors(typeBinding, false, true);
      Arrays.sort(bindings, new Comparator<IMethodBinding>(){
        public int compare(IMethodBinding b1, IMethodBinding b2){
          return b1.getParameterTypes().length - b2.getParameterTypes().length;
        }
      });
      constructor = bindings.length > 0 ? bindings[0] : null;
    }
    return constructor;
  }

  private IJavaElement getSibling(IType type)
    throws Exception
  {
    IJavaElement sibling = null;
    IMethod[] methods = type.getMethods();
    for (int ii = 0; ii < methods.length; ii++){
      if(methods[ii].isConstructor()){
        sibling = ii < methods.length - 1 ? methods[ii + 1] : null;
      }
    }
    // insert before any other methods or inner classes if any.
    if(sibling == null){
      if(methods.length > 0){
        sibling = methods[0];
      }else{
        IType[] types = type.getTypes();
        sibling = types != null && types.length > 0 ? types[0] : null;
      }
    }
    return sibling;
  }

  private String buildParams(IType type, String[] fields)
    throws Exception
  {
    StringBuffer params = new StringBuffer();
    for (int ii = 0; ii < fields.length; ii++){
      if(ii != 0){
        params.append(", ");
      }
      IField field = type.getField(fields[ii]);
      params.append(Signature.getSignatureSimpleName(field.getTypeSignature()))
        .append(' ').append(field.getElementName());
    }
    return params.toString();
  }
}
