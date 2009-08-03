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

import org.eclim.plugin.core.util.ProjectUtils;
import org.eclim.plugin.core.util.TemplateUtils;

import org.eclim.plugin.jdt.PluginResources;

import org.eclim.plugin.jdt.command.impl.ImplCommand;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.MethodUtils;
import org.eclim.plugin.jdt.util.TypeInfo;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.CollectionUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IProject;

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
  private static final String JUNIT_TEMPLATE = "junit<version>_method.gst";

  /**
   * {@inheritDoc}
   */
  @Override
  protected TypeInfo[] getSuperTypes(CommandLine commandLine, IType type)
    throws Exception
  {
    ArrayList<TypeInfo> types = new ArrayList<TypeInfo>();

    String baseType = commandLine.getValue(Options.BASETYPE_OPTION);
    if(baseType != null){
      IType base = type.getJavaProject().findType(baseType);
      types.add(new TypeInfo(base, null, null));

      TypeInfo[] baseTypes = super.getSuperTypes(commandLine, base);
      CollectionUtils.addAll(types, baseTypes);
    }

    TypeInfo[] testTypes = super.getSuperTypes(commandLine, type);
    for(int ii = 0; ii <testTypes.length; ii ++){
      if(!types.contains(testTypes[ii])){
        types.add(testTypes[ii]);
      }
    }

    return (TypeInfo[])types.toArray(new TypeInfo[types.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
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
  protected IMethod getImplemented(
      IType type,
      Map<String, IMethod>
      baseMethods,
      TypeInfo superTypeInfo,
      IMethod method)
    throws Exception
  {
    IMethod base = (IMethod)baseMethods.get(
        MethodUtils.getMinimalMethodSignature(method, superTypeInfo));
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
    String version = ProjectUtils.getSetting(
        method.getJavaProject().getProject(), "org.eclim.java.junit.version");

    String name = method.getElementName();
    if (version.equals("3")){
      name = "test" + StringUtils.capitalize(name);
    }

    StringBuffer buffer = new StringBuffer();
    buffer.append(name).append("()");
    return buffer.toString();
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
    IType superType = superTypeInfo.getType();
    String baseType = commandLine.getValue(Options.BASETYPE_OPTION);
    if(baseType != null){
      IType base = type.getJavaProject().findType(baseType);
      if(base.equals(superType)){
        return insertTestMethod(type, superTypeInfo, method, sibling);
      }else{
        TypeInfo[] superTypesInfo = TypeUtils.getSuperTypes(base);
        for (TypeInfo info : superTypesInfo){
          if(info.getType().equals(superType)){
            return insertTestMethod(type, superTypeInfo, method, sibling);
          }
        }
      }
    }

    return super.insertMethod(
        commandLine, src, type, superTypeInfo, method, sibling);
  }

  /**
   * Inserts a test method stub for the supplied method.
   *
   * @param type The type to insert the method into.
   * @param superTypeInfo The super type the method is defined in.
   * @param method The method to insert.
   * @param sibling The element to insert the new method before, or null to
   *  append to the end.
   * @return The position the method was inserted at.
   */
  protected Position insertTestMethod(
      IType type, TypeInfo superTypeInfo, IMethod method, IJavaElement sibling)
    throws Exception
  {
    IProject project = type.getJavaProject().getProject();
    HashMap<String, Object> values = new HashMap<String, Object>();
    JavaUtils.loadPreferencesForTemplate(project, getPreferences(), values);

    String version =
      ProjectUtils.getSetting(project, "org.eclim.java.junit.version");

    String name = method.getElementName();
    if (version.equals("3")){
      name = "test" + StringUtils.capitalize(name);
    }
    values.put("name", name);
    values.put("methodName", method.getElementName());
    values.put("superType", superTypeInfo.getType().getFullyQualifiedName());
    values.put("methodSignatures", getMethodSignatures(superTypeInfo, method));
    values.put("methodBody", null);

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String template = JUNIT_TEMPLATE.replace("<version>", version);
    String result = TemplateUtils.evaluate(resources, template, values);
    Position position = TypeUtils.getPosition(type,
        type.createMethod(result, sibling, false, null));

    return position;
  }

  /**
   * Constructs an array of method signatures.
   *
   * @param typeInfo The type to grab the methods from.
   * @param method The method or one of the overloaded methods to construct an
   * array from.
   * @return Array of method signatures.
   */
  protected String[] getMethodSignatures(TypeInfo typeInfo, IMethod method)
    throws Exception
  {
    ArrayList<String> signatures = new ArrayList<String>();
    IMethod[] methods = typeInfo.getType().getMethods();
    for (int ii = 0; ii < methods.length; ii++){
      if(methods[ii].getElementName().equals(method.getElementName())){
        String sig = MethodUtils.getMinimalMethodSignature(methods[ii], typeInfo);
        signatures.add(sig);
      }
    }
    return (String[])signatures.toArray(new String[signatures.size()]);
  }
}
