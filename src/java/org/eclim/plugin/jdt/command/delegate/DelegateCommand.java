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
package org.eclim.plugin.jdt.command.delegate;

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
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.file.Position;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
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
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL s superType ARG," +
    "OPTIONAL m methods ARG"
)
public class DelegateCommand
  extends ImplCommand
{
  private static final String TEMPLATE = "method.gst";

  private IField field;
  private IType delegateType;

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    int offset = commandLine.getIntValue(Options.OFFSET_OPTION);
    if(offset != -1){
      offset = getOffset(commandLine);
      IJavaElement element = src.getElementAt(offset);
      if(element.getElementType() != IJavaElement.FIELD){
        return Services.getMessage("not.a.field");
      }

      field = (IField)element;

      String signature = field.getTypeSignature();
      delegateType = TypeUtils.findUnqualifiedType(
          src, Signature.getSignatureSimpleName(signature));

      if(delegateType == null){
        return Services.getMessage("type.not.found",
            src.getJavaProject().getElementName(),
            Signature.getSignatureSimpleName(signature)) + "  " +
          Services.getMessage("check.import");
      }
    }

    return super.execute(commandLine);
  }

  /**
   * {@inheritDoc}
   */
  protected IType[] getSuperTypes(CommandLine commandLine, IType type)
    throws Exception
  {
    IType[] types = super.getSuperTypes(commandLine, delegateType);
    IType[] results = new IType[types.length + 1];
    results[0] = delegateType;
    System.arraycopy(types, 0, results, 1, types.length);

    return results;
  }

  /**
   * {@inheritDoc}
   */
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
  protected boolean isValidType(IType type)
    throws Exception
  {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  protected Position insertMethod(
      CommandLine commandLine,
      ICompilationUnit src,
      IType type,
      IType superType,
      IMethod method,
      IJavaElement sibling)
    throws Exception
  {
    HashMap<String, Object> values = new HashMap<String, Object>();
    JavaUtils.loadPreferencesForTemplate(
        type.getJavaProject().getProject(), getPreferences(), values);

    if(superType.isInterface()){
      values.put("modifier", "public");
    }else{
      values.put("modifier",
          Flags.isPublic(method.getFlags()) ? "public" : "protected");
    }
    values.put("name", method.getElementName());
    String returnType = Signature.getSignatureSimpleName(
        method.getReturnType());
    values.put("returnType", returnType);
    values.put("params", MethodUtils.getMethodParameters(method, true));
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
    values.put("methodSignature", MethodUtils.getMinimalMethodSignature(method));

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String result = TemplateUtils.evaluate(resources, TEMPLATE, values);
    Position position = TypeUtils.getPosition(type,
        type.createMethod(result, sibling, false, null));

    return position;
  }
}
