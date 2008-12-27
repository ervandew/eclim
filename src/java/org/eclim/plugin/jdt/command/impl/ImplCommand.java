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
package org.eclim.plugin.jdt.command.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.jdt.PluginResources;

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
import org.eclipse.jdt.core.Signature;

/**
 * Command used to build a tree of methods that have or can be
 * implemented/overriden by the supplied file according the interfaces/parent
 * class it implements/extends.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ImplCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(ImplCommand.class);

  private static final String TEMPLATE = "method.gst";

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String superTypeName = commandLine.getValue(Options.SUPERTYPE_OPTION);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    IType type = null;
    if(superTypeName != null){
      type = src.getJavaProject().findType(
          commandLine.getValue(Options.TYPE_OPTION).replace('$', '.'));
      IType superType = type.getJavaProject().findType(superTypeName);

      String methodsOption = commandLine.getValue(Options.METHOD_OPTION);

      String[] methods = null;
      if(methodsOption != null){
        methods = StringUtils.splitByWholeSeparator(methodsOption, ",,");
      }else{
        methods = getUnimplementedMethods(type, superType);
      }

      for(int ii = 0; ii < methods.length; ii++){
        executeInsertMethod(commandLine, src, type, superType, methods[ii]);
      }
    }

    if(type == null){
      int offset = commandLine.getIntValue(Options.OFFSET_OPTION);
      if (offset != -1){
        offset = getOffset(commandLine);
      }
      type = TypeUtils.getType(src, offset);
    }
    ImplResult result = executeGetMethods(commandLine, type);
    return ImplFilter.instance.filter(commandLine, result);
  }

  /**
   * Gets all the methods of the supertypes for the supplied type.
   *
   * @param commandLine The original command line.
   * @param type The type.
   * @return ImplResult
   */
  protected ImplResult executeGetMethods(CommandLine commandLine, IType type)
    throws Exception
  {
    ArrayList<ImplType> results = new ArrayList<ImplType>();
    if(isValidType(type)){
      Map<String, IMethod> implementedMethods = getImplementedMethods(type);

      IType[] types = getSuperTypes(commandLine, type);
      for(int ii = 0; ii < types.length; ii++){
        ImplType implType = new ImplType();
        implType.setPackage(types[ii].getPackageFragment().getElementName());
        implType.setExists(types[ii].exists());
        if(types[ii].exists()){
          implType.setSignature(TypeUtils.getTypeSignature(types[ii]));
          implType.setMethods(getMethods(type, implementedMethods, types[ii]));
        }else{
          implType.setSignature(types[ii].getElementName());
        }

        results.add(implType);
      }
    }else{
      throw new IllegalArgumentException(
          Services.getMessage("type.not.a.class",
            type.getFullyQualifiedName()));
    }
    return new ImplResult(type.getFullyQualifiedName(), results);
  }

  /**
   * Inserts a stub for the supplied method or for all methods in the supplied
   * super type if no method supplied.
   *
   * @param commandLine The original command line.
   * @param src The compilation unit.
   * @param type The type to insert the method(s) into.
   * @param superType The super type to insert methods from.
   * @param methodName The super type to insert methods from.
   *
   * @return The Position where the method(s) where inserted.
   */
  protected Object executeInsertMethod(
      CommandLine commandLine,
      ICompilationUnit src,
      IType type,
      IType superType,
      String methodName)
    throws Exception
  {
    IMethod[] methods = superType.getMethods();
    Map<String, IMethod> implementedMethods = getImplementedMethods(type);

    IMethod method = null;
    for(int ii = 0; ii < methods.length; ii++){
      if(MethodUtils.getMinimalMethodSignature(methods[ii]).equals(methodName)){
        method = methods[ii];
        break;
      }
    }

    if(method == null){
      logger.warn(Services.getMessage("method.not.found",
          superType.getFullyQualifiedName(), methodName));
      return null;
    }
    if(getImplemented(type, implementedMethods, method) != null){
      logger.warn(Services.getMessage("method.already.implemented",
            type.getFullyQualifiedName(),
            superType.getFullyQualifiedName(),
            methodName
      ));
      return null;
    }

    IJavaElement sibling =
      getSibling(type, implementedMethods, methods, method);
    insertMethod(commandLine, src, type, superType, method, sibling);

    return null;
  }

  /**
   * Gets the names of the unimplemented methods from the super type.
   *
   * @param type The type to add the methods to.
   * @param superType The super type to add methods from.
   * @return Array of minimal method signatures.
   */
  protected String[] getUnimplementedMethods(IType type, IType superType)
    throws Exception
  {
    ArrayList<String> names = new ArrayList<String>();

    IMethod[] methods = superType.getMethods();
    Map<String, IMethod> implementedMethods = getImplementedMethods(type);

    for(int ii = 0; ii < methods.length; ii++){
      int flags = methods[ii].getFlags();
      IMethod implemented =
        getImplemented(type, implementedMethods, methods[ii]);
      if (!Flags.isStatic(flags) &&
          !Flags.isFinal(flags) &&
          !Flags.isPrivate(flags) &&
          !methods[ii].isConstructor() &&
          implemented == null){
        names.add(MethodUtils.getMinimalMethodSignature(methods[ii]));
      }
    }

    return (String[])names.toArray(new String[names.size()]);
  }

  /**
   * Gets all the super types for the supplied type.
   *
   * @param commandLine The original command line.
   * @param type The type.
   * @return The super types.
   */
  protected IType[] getSuperTypes(CommandLine commandLine, IType type)
    throws Exception
  {
    return TypeUtils.getSuperTypes(type, true);
  }

  /**
   * Gets a map of minimal method signatures and methods implemented by the
   * supplied type.
   *
   * @param type The type.
   * @return Map of minimal method signatures and the corresponding methods.
   */
  protected Map<String, IMethod> getImplementedMethods(IType type)
    throws Exception
  {
    HashMap<String, IMethod> implementedMethods = new HashMap<String, IMethod>();
    IMethod[] methods = type.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      int flags = methods[ii].getFlags();
      if (!Flags.isStatic(flags) &&
          !Flags.isFinal(flags) &&
          !Flags.isPrivate(flags)){
        implementedMethods.put(
            MethodUtils.getMinimalMethodSignature(methods[ii]),
            methods[ii]);
      }
    }
    return implementedMethods;
  }

  /**
   * Inserts the supplied method into the specified source type.
   *
   * @param commandLine The original command line.
   * @param src The compilation unit.
   * @param type The type to insert the method into.
   * @param superType The super type the method is defined in.
   * @param method The method to insert.
   * @param sibling The element to insert the new method before, or null to
   *  append to the end.
   * @return The position the method was inserted at.
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

    if(!method.isConstructor()){
      values.put("constructor", Boolean.FALSE);
      values.put("name", method.getElementName());
      values.put("returnType",
          Signature.getSignatureSimpleName(method.getReturnType()));
      values.put("methodBody", null);
    }else{
      values.put("constructor", Boolean.TRUE);
      values.put("name", type.getElementName());
      StringBuffer buffer = new StringBuffer("super(");
      String[] paramNames = method.getParameterNames();
      for(int ii = 0; ii < paramNames.length; ii++){
        if(ii != 0){
          buffer.append(", ");
        }
        buffer.append(paramNames[ii]);
      }
      buffer.append(");");
      values.put("methodBody", buffer.toString());
      values.put("returnType", null);
    }

    if(superType.isInterface()){
      values.put("modifier", "public");
    }else{
      values.put("modifier",
          Flags.isPublic(method.getFlags()) ? "public" : "protected");
    }
    values.put("superType",
      JavaUtils.getCompilationUnitRelativeTypeName(src, superType));
    values.put("params", MethodUtils.getMethodParameters(method, true));
    values.put("overrides",
        superType.isClass() ? Boolean.TRUE : Boolean.FALSE);
    values.put("implementof",
        superType.isClass() ? Boolean.FALSE : Boolean.TRUE);
    values.put("methodSignature", MethodUtils.getMinimalMethodSignature(method));
    String thrown = MethodUtils.getMethodThrows(method);
    values.put("throwsType", thrown != null ? thrown : null);
    values.put("delegate", Boolean.FALSE);

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String result = TemplateUtils.evaluate(resources, TEMPLATE, values);
    Position position = TypeUtils.getPosition(type,
        type.createMethod(result, sibling, false, null));

    return position;
  }

  /**
   * Gets the methods from the super type.
   *
   * @param type The type to be modified.
   * @param baseMethods The base methods from the base type.
   * @param superType The super type.
   *
   * @return Array of methods.
   */
  protected ImplMethod[] getMethods(
      IType type, Map<String, IMethod> baseMethods, IType superType)
    throws Exception
  {
    ArrayList<ImplMethod> results = new ArrayList<ImplMethod>();
    IMethod[] methods = superType.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      IMethod method = methods[ii];
      if(isValidMethod(method)){
        String signature = MethodUtils.getMethodSignature(method);
        ImplMethod implMethod = new ImplMethod();
        implMethod.setSignature(signature);
        implMethod.setImplemented(
            getImplemented(type, baseMethods, method) != null);

        results.add(implMethod);
      }
    }

    return (ImplMethod[])results.toArray(new ImplMethod[results.size()]);
  }

  /**
   * Determine if the supplied method should be included in list of
   * overridable / implmentable methods.
   *
   * @param method The method.
   * @return true is should be included, false otherwise.
   */
  protected boolean isValidMethod(IMethod method)
    throws Exception
  {
    int flags = method.getFlags();
    return (!Flags.isStatic(flags) &&
        !Flags.isFinal(flags) &&
        !Flags.isPrivate(flags));
  }

  /**
   * Determines if the supplied type is valid for overriding / implementing
   * methods.
   *
   * @param type The type.
   * @return true if valid, false otherwise.
   */
  protected boolean isValidType(IType type)
    throws Exception
  {
    return type.isClass();
  }

  /**
   * Gets the implemented version of the supplied method.
   *
   * @param type The type to be modified.
   * @param baseMethods The list of methods defined in the base.
   * @param method The method to test for.
   * @return The implemented method or null if none.
   */
  protected IMethod getImplemented(
      IType type, Map<String, IMethod> baseMethods, IMethod method)
    throws Exception
  {
    String signature = MethodUtils.getMinimalMethodSignature(method);
    if(method.isConstructor()){
      signature = signature.replaceFirst(
          method.getDeclaringType().getElementName(),
          type.getElementName());
    }
    return baseMethods.get(signature);
  }

  /**
   * Gets the sibling to insert before.
   *
   * @param type The type to insert into.
   * @param baseMethods The currently implemented methods.
   * @param methods The super types methods.
   * @param method The method to be added.
   * @return The sibling, or null if none.
   */
  protected IJavaElement getSibling(
      IType type,
      Map<String, IMethod> baseMethods,
      IMethod[] methods,
      IMethod method)
    throws Exception
  {
    int index = -1;
    int implementedIndex = -1;
    IJavaElement sibling = null;

    // find the nearest implemented method
    for (int ii = 0; ii < methods.length; ii++){
      if(methods[ii].equals(method)){
        index = ii;
      }else{
        IMethod implemented = getImplemented(type, baseMethods, methods[ii]);
        if(implemented != null){
          implementedIndex = ii;
          sibling = implemented;
          if(index != -1){
            break;
          }
        }
      }
    }

    if(implementedIndex < index && sibling != null){
      // get the method after the sibling.
      sibling = MethodUtils.getMethodAfter(type, (IMethod)sibling);
    }

    // no sibling, get first non enum type.
    if(sibling == null){
      IType[] types = type.getTypes();
      if(types.length > 0){
        // find the first non-enum type.
        for (int ii = 0; ii < types.length; ii++){
          if(!types[ii].isEnum()){
            return types[ii];
          }
        }
      }
    }
    return sibling;
  }
}
