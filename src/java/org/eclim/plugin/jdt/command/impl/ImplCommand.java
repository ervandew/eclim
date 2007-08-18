/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.plugin.jdt.command.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ImplCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(ImplCommand.class);

  private static final String TEMPLATE = "method.vm";

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String project = _commandLine.getValue(Options.PROJECT_OPTION);
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String superTypeName = _commandLine.getValue(Options.SUPERTYPE_OPTION);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    IType type = null;
    if(superTypeName != null){
      type = src.getJavaProject().findType(
          _commandLine.getValue(Options.TYPE_OPTION).replace('$', '.'));
      IType superType = type.getJavaProject().findType(superTypeName);

      String methodsOption = _commandLine.getValue(Options.METHOD_OPTION);

      String[] methods = null;
      if(methodsOption != null){
        methods = StringUtils.split(methodsOption, "|");
      }else{
        methods = getUnimplementedMethods(type, superType);
      }

      for(int ii = 0; ii < methods.length; ii++){
        executeInsertMethod(_commandLine, src, type, superType, methods[ii]);
      }
    }

    if(type == null){
      int offset = _commandLine.getIntValue(Options.OFFSET_OPTION);
      type = TypeUtils.getType(src, offset);
    }
    ImplResult result = executeGetMethods(_commandLine, type);
    return ImplFilter.instance.filter(_commandLine, result);
  }

  /**
   * Gets all the methods of the supertypes for the supplied type.
   *
   * @param _commandLine The original command line.
   * @param _type The type.
   * @return ImplResult
   */
  protected ImplResult executeGetMethods (CommandLine _commandLine, IType _type)
    throws Exception
  {
    ArrayList<ImplType> results = new ArrayList<ImplType>();
    if(isValidType(_type)){
      Map<String,IMethod> implementedMethods = getImplementedMethods(_type);

      IType[] types = getSuperTypes(_commandLine, _type);
      for(int ii = 0; ii < types.length; ii++){
        ImplType implType = new ImplType();
        implType.setPackage(types[ii].getPackageFragment().getElementName());
        implType.setExists(types[ii].exists());
        if(types[ii].exists()){
          implType.setSignature(TypeUtils.getTypeSignature(types[ii]));
          implType.setMethods(getMethods(_type, implementedMethods, types[ii]));
        }else{
          implType.setSignature(types[ii].getElementName());
        }

        results.add(implType);
      }
    }else{
      throw new IllegalArgumentException(
          Services.getMessage("type.not.a.class",
            _type.getFullyQualifiedName()));
    }
    return new ImplResult(_type.getFullyQualifiedName(), results);
  }

  /**
   * Inserts a stub for the supplied method or for all methods in the supplied
   * super type if no method supplied.
   *
   * @param _commandLine The original command line.
   * @param _src The compilation unit.
   * @param _type The type to insert the method(s) into.
   * @param _superType The super type to insert methods from.
   * @param _methodName The super type to insert methods from.
   *
   * @return The Position where the method(s) where inserted.
   */
  protected Object executeInsertMethod (
      CommandLine _commandLine,
      ICompilationUnit _src,
      IType _type,
      IType _superType,
      String _methodName)
    throws Exception
  {
    IMethod[] methods = _superType.getMethods();
    Map<String,IMethod> implementedMethods = getImplementedMethods(_type);

    IMethod method = null;
    for(int ii = 0; ii < methods.length; ii++){
      if(MethodUtils.getMinimalMethodSignature(methods[ii]).equals(_methodName)){
        method = methods[ii];
        break;
      }
    }

    if(method == null){
      logger.warn(Services.getMessage("method.not.found",
          _superType.getFullyQualifiedName(), _methodName));
      return null;
    }
    if(getImplemented(_type, implementedMethods, method) != null){
      logger.warn(Services.getMessage("method.already.implemented",
            _type.getFullyQualifiedName(),
            _superType.getFullyQualifiedName(),
            _methodName
      ));
      return null;
    }

    IJavaElement sibling =
      getSibling(_type, implementedMethods, methods, method);
    insertMethod(_commandLine, _src, _type, _superType, method, sibling);

    return null;
  }

  /**
   * Gets the names of the unimplemented methods from the super type.
   *
   * @param _type The type to add the methods to.
   * @param _superType The super type to add methods from.
   * @return Array of minimal method signatures.
   */
  protected String[] getUnimplementedMethods (IType _type, IType _superType)
    throws Exception
  {
    ArrayList<String> names = new ArrayList<String>();

    IMethod[] methods = _superType.getMethods();
    Map<String,IMethod> implementedMethods = getImplementedMethods(_type);

    for(int ii = 0; ii < methods.length; ii++){
      int flags = methods[ii].getFlags();
      IMethod implemented =
        getImplemented(_type, implementedMethods, methods[ii]);
      if (!Flags.isStatic(flags) &&
          !Flags.isFinal(flags) &&
          !Flags.isPrivate(flags) &&
          !methods[ii].isConstructor() &&
          implemented == null)
      {
        names.add(MethodUtils.getMinimalMethodSignature(methods[ii]));
      }
    }

    return (String[])names.toArray(new String[names.size()]);
  }

  /**
   * Gets all the super types for the supplied type.
   *
   * @param _commandLine The original command line.
   * @param _type The type.
   * @return The super types.
   */
  protected IType[] getSuperTypes (CommandLine _commandLine, IType _type)
    throws Exception
  {
    return TypeUtils.getSuperTypes(_type, true);
  }

  /**
   * Gets a map of minimal method signatures and methods implemented by the
   * supplied type.
   *
   * @param _type The type.
   * @return Map of minimal method signatures and the corresponding methods.
   */
  protected Map<String,IMethod> getImplementedMethods (IType _type)
    throws Exception
  {
    HashMap<String,IMethod> implementedMethods = new HashMap<String,IMethod>();
    IMethod[] methods = _type.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      int flags = methods[ii].getFlags();
      if (!Flags.isStatic(flags) &&
          !Flags.isFinal(flags) &&
          !Flags.isPrivate(flags))
      {
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
   * @param _commandLine The original command line.
   * @param _src The compilation unit.
   * @param _type The type to insert the method into.
   * @param _superType The super type the method is defined in.
   * @param _method The method to insert.
   * @param _sibling The element to insert the new method before, or null to
   *  append to the end.
   * @return The position the method was inserted at.
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
    HashMap<String,Object> values = new HashMap<String,Object>();
    JavaUtils.loadPreferencesForTemplate(
        _type.getJavaProject().getProject(), getPreferences(), values);

    if(!_method.isConstructor()){
      values.put("name", _method.getElementName());
      values.put("return",
          Signature.getSignatureSimpleName(_method.getReturnType()));
    }else{
      values.put("constructor", Boolean.TRUE);
      values.put("name", _type.getElementName());
      StringBuffer buffer = new StringBuffer("super(");
      String[] paramNames = _method.getParameterNames();
      for(int ii = 0; ii < paramNames.length; ii++){
        if(ii != 0){
          buffer.append(", ");
        }
        buffer.append(paramNames[ii]);
      }
      buffer.append(");");
      values.put("methodBody", buffer.toString());
    }

    if(_superType.isInterface()){
      values.put("modifier", "public");
    }else{
      values.put("modifier",
          Flags.isPublic(_method.getFlags()) ? "public" : "protected");
    }
    values.put("superType",
      JavaUtils.getCompilationUnitRelativeTypeName(_src, _superType));
    values.put("params", MethodUtils.getMethodParameters(_method, true));
    values.put("overrides",
        _superType.isClass() ? Boolean.TRUE : Boolean.FALSE);
    values.put("implements",
        _superType.isClass() ? Boolean.FALSE : Boolean.TRUE);
    values.put("methodSignature", MethodUtils.getMinimalMethodSignature(_method));
    String thrown = MethodUtils.getMethodThrows(_method);
    if(thrown != null){
      values.put("throws", thrown);
    }

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String method = TemplateUtils.evaluate(resources, TEMPLATE, values);
    Position position = TypeUtils.getPosition(_type,
        _type.createMethod(method, _sibling, false, null));

    return position;
  }

  /**
   * Gets the methods from the super type.
   *
   * @param _type The type to be modified.
   * @param _baseMethods The base methods from the base type.
   * @param _superType The super type.
   *
   * @return Array of methods.
   */
  protected ImplMethod[] getMethods (
      IType _type, Map<String,IMethod> _baseMethods, IType _superType)
    throws Exception
  {
    ArrayList<ImplMethod> results = new ArrayList<ImplMethod>();
    IMethod[] methods = _superType.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      IMethod method = methods[ii];
      if(isValidMethod(method)){
        String signature = MethodUtils.getMethodSignature(method);
        ImplMethod implMethod = new ImplMethod();
        implMethod.setSignature(signature);
        implMethod.setImplemented(
            getImplemented(_type, _baseMethods, method) != null);

        results.add(implMethod);
      }
    }

    return (ImplMethod[])results.toArray(new ImplMethod[results.size()]);
  }

  /**
   * Determine if the supplied method should be included in list of
   * overridable / implmentable methods.
   *
   * @param _method The method.
   * @return true is should be included, false otherwise.
   */
  protected boolean isValidMethod (IMethod _method)
    throws Exception
  {
    int flags = _method.getFlags();
    return (!Flags.isStatic(flags) &&
        !Flags.isFinal(flags) &&
        !Flags.isPrivate(flags));
  }

  /**
   * Determines if the supplied type is valid for overriding / implementing
   * methods.
   *
   * @param _type The type.
   * @return true if valid, false otherwise.
   */
  protected boolean isValidType (IType _type)
    throws Exception
  {
    return _type.isClass();
  }

  /**
   * Gets the implemented version of the supplied method.
   *
   * @param _type The type to be modified.
   * @param _baseMethods The list of methods defined in the base.
   * @param _method The method to test for.
   * @return The implemented method or null if none.
   */
  protected IMethod getImplemented (
      IType _type, Map<String,IMethod> _baseMethods, IMethod _method)
    throws Exception
  {
    String signature = MethodUtils.getMinimalMethodSignature(_method);
    if(_method.isConstructor()){
      signature = signature.replaceFirst(
          _method.getDeclaringType().getElementName(),
          _type.getElementName());
    }
    return _baseMethods.get(signature);
  }

  /**
   * Gets the sibling to insert before.
   *
   * @param _type The type to insert into.
   * @param _baseMethods The currently implemented methods.
   * @param _methods The super types methods.
   * @param _method The method to be added.
   * @return The sibling, or null if none.
   */
  protected IJavaElement getSibling (
      IType _type,
      Map<String,IMethod> _baseMethods,
      IMethod[] _methods,
      IMethod _method)
    throws Exception
  {
    int index = -1;
    int implementedIndex = -1;
    IJavaElement sibling = null;

    // find the nearest implemented method
    for (int ii = 0; ii < _methods.length; ii++){
      if(_methods[ii].equals(_method)){
        index = ii;
      }else{
        IMethod implemented = getImplemented(_type, _baseMethods, _methods[ii]);
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
      sibling = MethodUtils.getMethodAfter(_type, (IMethod)sibling);
    }

    // no sibling, get first non enum type.
    if(sibling == null){
      IType[] types = _type.getTypes();
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
