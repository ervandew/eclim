/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.PluginResources;

import org.eclim.plugin.jdt.command.impl.ImplCommand;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.MethodUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.TemplateUtils;

import org.eclim.util.file.Position;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * Command to handle creation of junit test stubs.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class JUnitImplCommand
  extends ImplCommand
{
  private static final String JUNIT3_TEMPLATE = "junit3_method.gst";

  /**
   * {@inheritDoc}
   */
  protected IType[] getSuperTypes (CommandLine _commandLine, IType _type)
    throws Exception
  {
    ArrayList<IType> types = new ArrayList<IType>();

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
  protected IMethod getImplemented (
      IType _type, Map<String,IMethod> _baseMethods, IMethod _method)
    throws Exception
  {
    IMethod method = (IMethod)_baseMethods.get(
        MethodUtils.getMinimalMethodSignature(_method));
    if(method != null){
      return method;
    }
    return (IMethod)_baseMethods.get(getTestMethodSignature(_method));
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
      ICompilationUnit _src,
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
        _commandLine, _src, _type, _superType, _method, _sibling);
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
    HashMap<String,Object> values = new HashMap<String,Object>();
    JavaUtils.loadPreferencesForTemplate(
        _type.getJavaProject().getProject(), getPreferences(), values);

    values.put("name",
        "test" + StringUtils.capitalize(_method.getElementName()));
    values.put("methodName", _method.getElementName());
    values.put("superType", _superType.getFullyQualifiedName());
    values.put("methodSignatures", getMethodSignatures(_superType, _method));
    values.put("methodBody", null);

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String method = TemplateUtils.evaluate(resources, JUNIT3_TEMPLATE, values);
    Position position = TypeUtils.getPosition(_type,
        _type.createMethod(method, _sibling, false, null));

    return position;
  }

  /**
   * Constructs an array of method signatures.
   *
   * @param _type The type to grab the methods from.
   * @param _method The method or one of the overloaded methods to construct an
   * array from.
   * @return Array of method signatures.
   */
  protected String[] getMethodSignatures (IType _type, IMethod _method)
    throws Exception
  {
    ArrayList<String> signatures = new ArrayList<String>();
    IMethod[] methods = _type.getMethods();
    for (int ii = 0; ii < methods.length; ii++){
      if(methods[ii].getElementName().equals(_method.getElementName())){
        signatures.add(MethodUtils.getMinimalMethodSignature(methods[ii]));
      }
    }
    return (String[])signatures.toArray(new String[signatures.size()]);
  }
}
