/**
 * Copyright (c) 2004 - 2005
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.jdt.command.delegate;

import java.io.IOException;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Map;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.JavaUtils;
import org.eclim.plugin.jdt.TypeUtils;

import org.eclim.plugin.jdt.command.impl.ImplCommand;

import org.eclim.util.VelocityFormat;

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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class DelegateCommand
  extends ImplCommand
{
  private static final String TEMPLATE = "java/method.vm";

  private IField field;
  private IType delegateType;

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);
      ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

      int offset = _commandLine.getIntValue(Options.OFFSET_OPTION);
      if(offset != -1){
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
                new Object[]{
                  src.getJavaProject().getElementName(),
                  Signature.getSignatureSimpleName(signature)}) + "  " +
            Services.getMessage("check.import");
        }
      }

      return super.execute(_commandLine);
    }catch(Exception e){
      return e;
    }
  }

  /**
   * {@inheritDoc}
   */
  protected IType[] getSuperTypes (CommandLine _commandLine, IType _type)
    throws Exception
  {
    IType[] types = super.getSuperTypes(_commandLine, delegateType);
    IType[] results = new IType[types.length + 1];
    results[0] = delegateType;
    System.arraycopy(types, 0, results, 1, types.length);

    return results;
  }

  /**
   * {@inheritDoc}
   */
  protected boolean isValidMethod (IMethod _method)
    throws Exception
  {
    int flags = _method.getFlags();
    return (Flags.isPublic(flags) && !_method.isConstructor());
  }

  /**
   * {@inheritDoc}
   */
  protected boolean isValidType (IType _type)
    throws Exception
  {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  protected Position insertMethod (
      CommandLine _commandLine,
      IType _type,
      IType _superType,
      IMethod _method,
      IJavaElement _sibling)
    throws Exception
  {
    Map values = new HashMap();
    JavaUtils.loadPreferencesForTemplate(
        _type.getJavaProject().getProject(), getPreferences(), values);

    values.put("name", _method.getElementName());
    values.put("modifier",
        Flags.isPublic(_method.getFlags()) ? "public" : "protected");
    String returnType = Signature.getSignatureSimpleName(
        _method.getReturnType());
    values.put("return", returnType);
    values.put("params", getMethodParameters(_method));
    String thrown = getMethodThrows(_method);
    if(thrown != null){
      values.put("throws", thrown);
    }

    StringBuffer methodBody = new StringBuffer();
    if(!returnType.equals("void")){
      methodBody.append("return ");
    }
    methodBody.append(field.getElementName())
      .append('.').append(_method.getElementName()).append('(');
    String[] paramNames = _method.getParameterNames();
    for(int ii = 0; ii < paramNames.length; ii++){
      if(ii != 0){
        methodBody.append(", ");
      }
      methodBody.append(paramNames[ii]);
    }
    methodBody.append(");");
    values.put("methodBody", methodBody.toString());

    values.put("superType", _superType.getFullyQualifiedName());
    values.put("implements", Boolean.TRUE);
    values.put("delegate", Boolean.TRUE);
    values.put("methodSignature", TypeUtils.getMinimalMethodSignature(_method));

    StringWriter writer = new StringWriter();
    VelocityFormat.evaluate(
        values, VelocityFormat.getTemplate(TEMPLATE), writer);
    Position position = TypeUtils.getPosition(_type,
        _type.createMethod(writer.toString(), _sibling, false, null));

    return position;
  }
}
