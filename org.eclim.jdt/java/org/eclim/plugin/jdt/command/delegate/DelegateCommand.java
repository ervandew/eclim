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
package org.eclim.plugin.jdt.command.delegate;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.util.TemplateUtils;

import org.eclim.plugin.jdt.PluginResources;

import org.eclim.plugin.jdt.command.impl.ImplCommand;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.MethodUtils;
import org.eclim.plugin.jdt.util.TypeInfo;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.file.Position;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.Signature;

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
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL s superType ARG," +
    "OPTIONAL m methods ARG"
)
public class DelegateCommand
  extends ImplCommand
{
  private static final String TEMPLATE = "method.gst";

  private IField field;
  private TypeInfo delegateTypeInfo;

  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    IJavaElement element = src.getElementAt(getOffset(commandLine));
    if(element.getElementType() != IJavaElement.FIELD){
      return Services.getMessage("not.a.field");
    }

    field = (IField)element;

    String signature = field.getTypeSignature();
    IType delegateType = TypeUtils.findUnqualifiedType(
        src, Signature.getSignatureSimpleName(signature));

    if(delegateType == null){
      return Services.getMessage("type.not.found",
          src.getJavaProject().getElementName(),
          Signature.getSignatureSimpleName(signature)) + "  " +
        Services.getMessage("check.import");
    }

    ITypeParameter[] params = delegateType.getTypeParameters();
    String[] typeParams = new String[params.length];
    for (int ii = 0; ii < params.length; ii++){
      typeParams[ii] = params[ii].getElementName();
    }

    String[] args = Signature.getTypeArguments(signature);
    String[] typeArgs = new String[args.length];
    for (int ii = 0; ii < args.length; ii++){
      typeArgs[ii] = Signature.getSignatureSimpleName(args[ii]);
    }

    delegateTypeInfo = new TypeInfo(delegateType, typeParams, typeArgs);

    return super.execute(commandLine);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected TypeInfo[] getSuperTypes(CommandLine commandLine, IType type)
    throws Exception
  {
    IType delegateType = delegateTypeInfo.getType();
    ArrayList<TypeInfo> types = new ArrayList<TypeInfo>();
    types.add(delegateTypeInfo);
    TypeUtils.getInterfaces(delegateType, types, false, delegateTypeInfo);
    TypeUtils.getSuperClasses(delegateType, types, false, delegateTypeInfo);

    return types.toArray(new TypeInfo[types.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isValidMethod(IMethod method)
    throws Exception
  {
    int flags = method.getFlags();
    return (!method.isConstructor() &&
        (Flags.isPublic(flags) || method.getDeclaringType().isInterface()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isValidType(IType type)
    throws Exception
  {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Position insertMethod(
      CommandLine commandLine,
      ICompilationUnit src,
      IType type,
      TypeInfo superTypeInfo,
      IMethod method,
      IJavaElement sibling)
    throws Exception
  {
    HashMap<String, Object> values = new HashMap<String, Object>();
    JavaUtils.loadPreferencesForTemplate(
        type.getJavaProject().getProject(), getPreferences(), values);

    IType superType = superTypeInfo.getType();
    if(superType.isInterface()){
      values.put("modifier", "public");
    }else{
      values.put("modifier",
          Flags.isPublic(method.getFlags()) ? "public" : "protected");
    }
    values.put("name", method.getElementName());
    String returnType = Signature.getSignatureSimpleName(
        method.getReturnType());
    values.put("returnType",
        TypeUtils.replaceTypeParams(returnType, superTypeInfo));
    values.put("params",
        MethodUtils.getMethodParameters(method, superTypeInfo, true));
    String thrown = MethodUtils.getMethodThrows(method);
    values.put("throwsType", thrown != null ? thrown : null);
    values.put("overrides", Boolean.FALSE);

    StringBuffer methodBody = new StringBuffer();
    if(!returnType.equals("void")){
      methodBody.append("return ");
    }
    methodBody.append(field.getElementName())
      .append('.').append(method.getElementName()).append('(');
    String[] paramNames = method.getParameterNames();
    for(int ii = 0; ii < paramNames.length; ii++){
      if(ii != 0){
        methodBody.append(", ");
      }
      methodBody.append(paramNames[ii]);
    }
    methodBody.append(");");
    values.put("methodBody", methodBody.toString());

    String typeName =
      JavaUtils.getCompilationUnitRelativeTypeName(src, superType);
    values.put("superType", typeName);
    values.put("implementof", Boolean.TRUE);
    values.put("delegate", Boolean.TRUE);
    values.put("methodSignature",
        MethodUtils.getMinimalMethodSignature(method, superTypeInfo));

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String result = TemplateUtils.evaluate(resources, TEMPLATE, values);
    Position position = TypeUtils.getPosition(type,
        type.createMethod(result, sibling, false, null));

    return position;
  }
}
