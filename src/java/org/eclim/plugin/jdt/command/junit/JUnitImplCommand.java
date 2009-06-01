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
package org.eclim.plugin.jdt.command.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * Command to handle creation of junit test stubs.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_junit_impl",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL b baseType ARG," +
    "OPTIONAL s superType ARG," +
    "OPTIONAL m methods ARG"
)
public class JUnitImplCommand
  extends ImplCommand
{
  private static final String JUNIT3_TEMPLATE = "junit3_method.gst";

  /**
   * {@inheritDoc}
   */
  protected IType[] getSuperTypes(CommandLine commandLine, IType type)
    throws Exception
  {
    ArrayList<IType> types = new ArrayList<IType>();

    String baseType = commandLine.getValue(Options.BASETYPE_OPTION);
    if(baseType != null){
      IType base = type.getJavaProject().findType(baseType);
      types.add(base);

      IType[] baseTypes = super.getSuperTypes(commandLine, base);
      for(int ii = 0; ii < baseTypes.length; ii ++){
        types.add(baseTypes[ii]);
      }
    }

    IType[] testTypes = super.getSuperTypes(commandLine, type);
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
  protected boolean isValidMethod(IMethod method)
    throws Exception
  {
    String parent = ((IType)method.getParent()).getFullyQualifiedName();
    if("java.lang.Object".equals(parent)){
      return super.isValidMethod(method);
    }
    int flags = method.getFlags();
    return (!Flags.isPrivate(flags) &&
      !method.isConstructor() &&
      !"junit.framework.Assert".equals(parent));
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
  protected IMethod getImplemented(
      IType type, Map<String, IMethod> baseMethods, IMethod method)
    throws Exception
  {
    IMethod base = (IMethod)baseMethods.get(
        MethodUtils.getMinimalMethodSignature(method));
    if(base != null){
      return base;
    }
    return (IMethod)baseMethods.get(getTestMethodSignature(method));
  }

  /**
   * Gets a string representation of the test method equivalent of the supplied
   * method.
   *
   * @param method The method.
   * @return The test method signature.
   */
  protected String getTestMethodSignature(IMethod method)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("test")
      .append(StringUtils.capitalize(method.getElementName()))
      .append("()");
    return buffer.toString();
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
    String baseType = commandLine.getValue(Options.BASETYPE_OPTION);
    if(baseType != null){
      IType base = type.getJavaProject().findType(baseType);
      if(base.equals(superType)){
        return insertTestMethod(type, superType, method, sibling);
      }else{
        IType[] superTypes = TypeUtils.getSuperTypes(base);
        for (int ii = 0; ii < superTypes.length; ii++){
          if(superTypes[ii].equals(superType)){
            return insertTestMethod(type, superType, method, sibling);
          }
        }
      }
    }

    return super.insertMethod(
        commandLine, src, type, superType, method, sibling);
  }

  /**
   * Inserts a test method stub for the supplied method.
   *
   * @param type The type to insert the method into.
   * @param superType The super type the method is defined in.
   * @param method The method to insert.
   * @param sibling The element to insert the new method before, or null to
   *  append to the end.
   * @return The position the method was inserted at.
   */
  protected Position insertTestMethod(
      IType type, IType superType, IMethod method, IJavaElement sibling)
    throws Exception
  {
    HashMap<String, Object> values = new HashMap<String, Object>();
    JavaUtils.loadPreferencesForTemplate(
        type.getJavaProject().getProject(), getPreferences(), values);

    values.put("name",
        "test" + StringUtils.capitalize(method.getElementName()));
    values.put("methodName", method.getElementName());
    values.put("superType", superType.getFullyQualifiedName());
    values.put("methodSignatures", getMethodSignatures(superType, method));
    values.put("methodBody", null);

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String result = TemplateUtils.evaluate(resources, JUNIT3_TEMPLATE, values);
    Position position = TypeUtils.getPosition(type,
        type.createMethod(result, sibling, false, null));

    return position;
  }

  /**
   * Constructs an array of method signatures.
   *
   * @param type The type to grab the methods from.
   * @param method The method or one of the overloaded methods to construct an
   * array from.
   * @return Array of method signatures.
   */
  protected String[] getMethodSignatures(IType type, IMethod method)
    throws Exception
  {
    ArrayList<String> signatures = new ArrayList<String>();
    IMethod[] methods = type.getMethods();
    for (int ii = 0; ii < methods.length; ii++){
      if(methods[ii].getElementName().equals(method.getElementName())){
        signatures.add(MethodUtils.getMinimalMethodSignature(methods[ii]));
      }
    }
    return (String[])signatures.toArray(new String[signatures.size()]);
  }
}
