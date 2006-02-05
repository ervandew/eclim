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
package org.eclim.plugin.jdt.command.junit;

import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.JavaUtils;
import org.eclim.plugin.jdt.TypeUtils;

import org.eclim.plugin.jdt.command.impl.ImplCommand;

import org.eclim.util.VelocityFormat;

import org.eclim.util.file.Position;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * Command to handle creation of junit test stubs.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class JUnitImplCommand
  extends ImplCommand
{
  private static final String TEMPLATE = "java/method.vm";

  /**
   * {@inheritDoc}
   */
  protected IType[] getSuperTypes (CommandLine _commandLine, IType _type)
    throws Exception
  {
    List types = new ArrayList();

    String baseType = _commandLine.getValue(Options.BASETYPE_OPTION);
    if(baseType != null){
      IType base = _type.getJavaProject().findType(baseType);
      types.add(base);

      IType[] baseTypes = super.getSuperTypes(_commandLine, base);
      for(int ii = 0; ii < baseTypes.length; ii ++){
        types.add(baseTypes[ii]);
      }
    }

    IType[] testTypes = super.getSuperTypes(_commandLine, _type);
    for(int ii = 0; ii <testTypes.length; ii ++){
      if(!types.contains(testTypes[ii])){
        types.add(testTypes[ii]);
      }
    }

    return (IType[])types.toArray(new IType[types.size()]);
  }

  /**
   * {@inheritDoc}
   */
  protected boolean isValidMethod (IMethod _method)
    throws Exception
  {
    String parent = ((IType)_method.getParent()).getFullyQualifiedName();
    if("java.lang.Object".equals(parent)){
      return super.isValidMethod(_method);
    }
    int flags = _method.getFlags();
    return (!Flags.isPrivate(flags) &&
      !_method.isConstructor() &&
      !"junit.framework.Assert".equals(parent));
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
  protected boolean isImplemented (List _baseMethods, IMethod _method)
    throws Exception
  {
    return _baseMethods.contains(TypeUtils.getMinimalMethodSignature(_method)) ||
      _baseMethods.contains(getTestMethodSignature(_method));
  }

  /**
   * Gets a string representation of the test method equivalent of the supplied
   * method.
   *
   * @param _method The method.
   * @return The test method signature.
   */
  protected String getTestMethodSignature (IMethod _method)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("test")
      .append(StringUtils.capitalize(_method.getElementName()))
      .append("()");
    return buffer.toString();
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
    String baseType = _commandLine.getValue(Options.BASETYPE_OPTION);
    if(baseType != null){
      IType base = _type.getJavaProject().findType(baseType);
      if(base.equals(_superType)){
        return insertTestMethod(_type, _superType, _method, _sibling);
      }else{
        IType[] superTypes = TypeUtils.getSuperTypes(base);
        for (int ii = 0; ii < superTypes.length; ii++){
          if(superTypes[ii].equals(_superType)){
            return insertTestMethod(_type, _superType, _method, _sibling);
          }
        }
      }
    }

    return super.insertMethod(
        _commandLine, _type, _superType, _method, _sibling);
  }

  /**
   * Inserts a test method stub for the supplied method.
   *
   * @param _type The type to insert the method into.
   * @param _superType The super type the method is defined in.
   * @param _method The method to insert.
   * @param _sibling The element to insert the new method before, or null to
   *  append to the end.
   * @return The position the method was inserted at.
   */
  protected Position insertTestMethod (
      IType _type, IType _superType, IMethod _method, IJavaElement _sibling)
    throws Exception
  {
    Map values = new HashMap();
    JavaUtils.loadPreferencesForTemplate(
        _type.getJavaProject().getProject(), getPreferences(), values);

    values.put("name",
        "test" + StringUtils.capitalize(_method.getElementName()));
    values.put("modifier", "public");
    values.put("methodDoc", "java/test_doc.vm");
    values.put("methodName", _method.getElementName());
    values.put("return", "void");
    values.put("superType", _superType.getFullyQualifiedName());
    values.put("implements", Boolean.TRUE);
    values.put("methodSignature", TypeUtils.getMinimalMethodSignature(_method));
    values.put("throws", "Exception");

    StringWriter writer = new StringWriter();
    VelocityFormat.evaluate(
        values, VelocityFormat.getTemplate(TEMPLATE), writer);
    Position position = TypeUtils.getPosition(_type,
        _type.createMethod(writer.toString(), _sibling, false, null));

    return position;
  }
}
