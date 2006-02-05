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
package org.eclim.plugin.jdt.command.impl;

import java.io.IOException;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.JavaUtils;
import org.eclim.plugin.jdt.TypeUtils;

import org.eclim.util.VelocityFormat;

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
  private static final String TEMPLATE = "templates/java/method.vm";

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String superType = _commandLine.getValue(Options.SUPERTYPE_OPTION);

      ICompilationUnit src = JavaUtils.getCompilationUnit(file);

      IType type = null;
      if(superType != null){
        type = src.getJavaProject().findType(
            _commandLine.getValue(Options.TYPE_OPTION).replace('$', '.'));
        String methodsOption = _commandLine.getValue(Options.METHOD_OPTION);
        if(methodsOption != null){
          String[] methods = StringUtils.split(methodsOption, "|");
          for(int ii = 0; ii < methods.length; ii++){
            executeInsertMethod(_commandLine, type, superType, methods[ii]);
          }
        }else{
          executeInsertMethod(_commandLine, type, superType, null);
        }
      }

      if(type == null){
        int offset = _commandLine.getIntValue(Options.OFFSET_OPTION);
        type = TypeUtils.getType(src, offset);
      }
      return filter(_commandLine, executeGetMethods(_commandLine, type));
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Gets all the methods of the supertypes for the supplied type.
   *
   * @param _commandLine The original command line.
   * @param _type The type.
   * @return List of ImplMethod.
   */
  protected Object executeGetMethods (CommandLine _commandLine, IType _type)
    throws Exception
  {
    List results = new ArrayList();
    if(isValidType(_type)){
      List implementedMethods = getImplementedMethods(_type);

      IType[] types = getSuperTypes(_commandLine, _type);
      for(int ii = 0; ii < types.length; ii++){
        ImplType implType = new ImplType();
        implType.setPackage(types[ii].getPackageFragment().getElementName());
        implType.setExists(types[ii].exists());
        if(types[ii].exists()){
          implType.setSignature(getTypeSignature(types[ii]));
          implType.setMethods(getMethods(implementedMethods, types[ii]));
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
   * @param _type The type to insert the method(s) into.
   * @param _superTypeName The super type to insert methods from.
   * @param _methodName The super type to insert methods from.
   *
   * @return The Position where the method(s) where inserted.
   */
  protected Object executeInsertMethod (
      CommandLine _commandLine,
      IType _type,
      String _superTypeName,
      String _methodName)
    throws Exception
  {
    IType superType = _type.getJavaProject().findType(_superTypeName);
    IMethod[] methods = superType.getMethods();
    List implementedMethods = getImplementedMethods(_type);

    IJavaElement sibling = getSibling(_type);

    // insert only one method
    if(_methodName != null){
      IMethod method = null;
      for(int ii = 0; ii < methods.length; ii++){
        if(TypeUtils.getMinimalMethodSignature(methods[ii]).equals(_methodName)){
          method = methods[ii];
          break;
        }
      }

      if(method == null){
        throw new IllegalArgumentException(
            Services.getMessage("method.not.found",
              new Object[]{_superTypeName, _methodName}));
      }
      if(isImplemented(implementedMethods, method)){
        throw new IllegalArgumentException(
            Services.getMessage("method.already.implemented",
              new Object[]{
                _type.getFullyQualifiedName(), _superTypeName, _methodName
              }));
      }

      return insertMethod(_commandLine, _type, superType, method, sibling);
    }

    // insert all methods not already implemented.
    int offset = 0;
    int length = 0;
    for(int ii = 0; ii < methods.length; ii++){
      int flags = methods[ii].getFlags();
      if (!Flags.isStatic(flags) &&
          !Flags.isFinal(flags) &&
          !Flags.isPrivate(flags) &&
          !methods[ii].isConstructor() &&
          !isImplemented(implementedMethods, methods[ii]))
      {
        Position position =
          insertMethod(_commandLine, _type, superType, methods[ii], sibling);
        offset = offset == 0 ? position.getOffset() : offset;
        // account for blank line and leading tab not included in position
        // length.
        if(length != 0){
          length += 3;
        }
        length = length + position.getLength();
      }
    }
    return new Position(
        _type.getResource().getLocation().toOSString(), offset, length);
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
   * Gets a list of minimal method signatures for methods implemented by the
   * supplied type.
   *
   * @param _type The type.
   * @return List of minial method signatures.
   */
  protected List getImplementedMethods (IType _type)
    throws Exception
  {
    List implementedMethods = new ArrayList();
    IMethod[] methods = _type.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      int flags = methods[ii].getFlags();
      if (!Flags.isStatic(flags) &&
          !Flags.isFinal(flags) &&
          !Flags.isPrivate(flags) &&
          !methods[ii].isConstructor())
      {
        implementedMethods.add(
            TypeUtils.getMinimalMethodSignature(methods[ii]));
      }
    }
    return implementedMethods;
  }

  /**
   * Inserts the supplied method into the specified source type.
   *
   * @param _commandLine The original command line.
   * @param _type The type to insert the method into.
   * @param _superType The super type the method is defined in.
   * @param _method The method to insert.
   * @param _sibling The element to insert the new method before, or null to
   *  append to the end.
   * @return The position the method was inserted at.
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
    values.put("return",
        Signature.getSignatureSimpleName(_method.getReturnType()));
    values.put("params", getMethodParameters(_method));
    values.put("superType", _superType.getFullyQualifiedName());
    values.put("overrides",
        _superType.isClass() ? Boolean.TRUE : Boolean.FALSE);
    values.put("implements",
        _superType.isClass() ? Boolean.FALSE : Boolean.TRUE);
    values.put("methodSignature", TypeUtils.getMinimalMethodSignature(_method));
    String thrown = getMethodThrows(_method);
    if(thrown != null){
      values.put("throws", thrown);
    }

    StringWriter writer = new StringWriter();
    VelocityFormat.evaluate(
        values, VelocityFormat.getTemplate(TEMPLATE), writer);
    Position position = TypeUtils.getPosition(_type,
        _type.createMethod(writer.toString(), _sibling, false, null));

    return position;
  }

  /**
   * Gets the methods from the super type.
   *
   * @param _baseMethods The base methods from the base type.
   * @param _superType The super type.
   *
   * @return Array of methods.
   */
  protected ImplMethod[] getMethods (List _baseMethods, IType _superType)
    throws Exception
  {
    List results = new ArrayList();
    IMethod[] methods = _superType.getMethods();
    for(int ii = 0; ii < methods.length; ii++){
      IMethod method = methods[ii];
      if(isValidMethod(method)){
        String signature = getMethodSignature(method);
        ImplMethod implMethod = new ImplMethod();
        implMethod.setSignature(signature);
        implMethod.setImplemented(isImplemented(_baseMethods, method));

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
        !Flags.isPrivate(flags) &&
        !_method.isConstructor());
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
   * Determines if the supplied method is implemented.
   *
   * @param _baseMethods The list of methods defined in the base.
   * @param _method The method to test for.
   * @return true if implemented, false otherwise.
   */
  protected boolean isImplemented (List _baseMethods, IMethod _method)
    throws Exception
  {
    return _baseMethods.contains(TypeUtils.getMinimalMethodSignature(_method));
  }

  /**
   * Gets the signature for the supplied type.
   *
   * @param _type The type.
   * @return The signature.
   */
  protected String getTypeSignature (IType _type)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();
    int flags = _type.getFlags();
    if(Flags.isPublic(flags)){
      buffer.append("public ");
    }

    buffer.append(_type.isClass() ? "class " : "interface ");
    buffer.append(_type.getElementName());
    return buffer.toString();
  }

  /**
   * Gets a String representation of the supplied method's signature.
   *
   * @param _method The method.
   * @return The signature.
   */
  protected String getMethodSignature (IMethod _method)
    throws Exception
  {
    int flags = _method.getFlags();
    StringBuffer buffer = new StringBuffer();
    buffer.append(Flags.isPublic(flags) ? "public " : "protected ")
      .append(Flags.isAbstract(flags) ? "abstract " : "")
      .append(Signature.getSignatureSimpleName(_method.getReturnType()))
      .append(' ')
      .append(_method.getElementName())
      .append(" (")
      .append(getMethodParameters(_method))
      .append(')');

    String[] exceptions = _method.getExceptionTypes();
    if(exceptions.length > 0){
      buffer.append("\n\tthrows ").append(getMethodThrows(_method));
    }
    return buffer.toString();
  }

  /**
   * Gets the supplied method's parameter types and names in a comma separated
   * string.
   *
   * @param _method The method.
   * @return The parameters.
   */
  protected String getMethodParameters (IMethod _method)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();
    String[] paramTypes = _method.getParameterTypes();
    String[] paramNames = _method.getParameterNames();
    for(int jj = 0; jj < paramTypes.length; jj++){
      if(jj != 0){
        buffer.append(", ");
      }
      buffer.append(Signature.getSignatureSimpleName(paramTypes[jj]));
      buffer.append(' ').append(paramNames[jj]);
    }
    return buffer.toString();
  }

  /**
   * Gets the list of thrown exceptions as a comma separated string.
   *
   * @param _method The method.
   * @return The thrown exceptions or null if none.
   */
  protected String getMethodThrows (IMethod _method)
    throws Exception
  {
    String[] exceptions = _method.getExceptionTypes();
    if(exceptions.length > 0){
      StringBuffer buffer = new StringBuffer();
      for(int ii = 0; ii < exceptions.length; ii++){
        if(ii != 0){
          buffer.append(", ");
        }
        buffer.append(Signature.getSignatureSimpleName(exceptions[ii]));
      }
      return buffer.toString();
    }
    return null;
  }

  /**
   * Gets the sibling to insert before.
   *
   * @param _type The type to insert into.
   * @return The sibling, or null if none.
   */
  protected IJavaElement getSibling (IType _type)
    throws Exception
  {
    IType[] types = _type.getTypes();
    if(types.length > 0){
      return types[0];
    }
    return null;
  }
}
