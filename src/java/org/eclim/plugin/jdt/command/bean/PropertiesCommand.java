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
package org.eclim.plugin.jdt.command.bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.PluginResources;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.MethodUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.TemplateUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Command used to generate java bean property methods (getters / setters).
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class PropertiesCommand
  extends AbstractCommand
{
  private static final String GETTER_TEMPLATE = "getter.vm";
  private static final String SETTER_TEMPLATE = "setter.vm";

  private static final String GETTER = "getter";
  private static final String SETTER = "setter";

  private static final int TYPE_GET = 0;
  private static final int TYPE_GET_INDEX = 1;
  private static final int TYPE_SET = 2;
  private static final int TYPE_SET_INDEX = 3;

  private static final String INT_SIG =
    Signature.createTypeSignature("int", true);
  private static final String[] INT_ARG = new String[]{INT_SIG};

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String methods = _commandLine.getValue(Options.TYPE_OPTION);
      String[] properties = StringUtils.split(
          _commandLine.getValue(Options.PROPERTIES_OPTION), ',');
      int offset = _commandLine.getIntValue(Options.OFFSET_OPTION);
      boolean indexed = _commandLine.hasOption(Options.INDEXED_OPTION);

      ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
      IType type = TypeUtils.getType(src, offset);
      List fields = Arrays.asList(type.getFields());

      IJavaElement sibling = null;
      // insert in reverse order since eclipse IType only has an insert before,
      // no insert after.
      for(int ii = properties.length - 1; ii >= 0; ii--){
        IField field = type.getField(properties[ii]);
        if(field != null){
          boolean array = false;
          if(Signature.getArrayCount(field.getTypeSignature()) > 0){
            array = true;
          }

          if(methods.indexOf(SETTER) != -1){
            // index setter
            if(array && indexed){
              sibling = getSibling(type, fields, field, sibling, TYPE_SET_INDEX);
              sibling = insertSetter(src, type, sibling, field, true);
            }
            // setter
            sibling = getSibling(type, fields, field, sibling, TYPE_SET);
            sibling = insertSetter(src, type, sibling, field, false);
          }

          if(methods.indexOf(GETTER) != -1){
            // index getter
            if(array && indexed){
              sibling = getSibling(type, fields, field, sibling, TYPE_GET_INDEX);
              sibling = insertGetter(src, type, sibling, field, true);
            }
            // getter
            sibling = getSibling(type, fields, field, sibling, TYPE_GET);
            sibling = insertGetter(src, type, sibling, field, false);
          }
        }
      }

      return StringUtils.EMPTY;
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Insert a getter for the supplied property.
   *
   * @param _src The src file.
   * @param _type The type to insert into.
   * @param _sibling The element to insert before.
   * @param _field The field.
   * @param _array true if inserting the array version.
   * @return The method element.
   */
  protected IJavaElement insertGetter (
      ICompilationUnit _src,
      IType _type,
      IJavaElement _sibling,
      IField _field,
      boolean _array)
    throws Exception
  {
    String propertyType =
      Signature.getSignatureSimpleName(_field.getTypeSignature());
    String methodName = StringUtils.capitalize(_field.getElementName());
    boolean isBoolean = propertyType.equals("boolean");
    if(isBoolean){
      methodName = "is" + methodName;
    }else{
      methodName = "get" + methodName;
    }

    String[] args = null;
    if(_array){
      propertyType = Signature.getSignatureSimpleName(
          Signature.getElementType(_field.getTypeSignature()));
      args = INT_ARG;
    }
    IMethod method = _type.getMethod(methodName, args);
    if(!method.exists()){
      HashMap<String,Object> values = new HashMap<String,Object>();
      values.put("propertyType", propertyType);
      values.put("name", methodName);
      values.put("property", _field.getElementName());
      values.put("array", _array ? Boolean.TRUE : Boolean.FALSE);
      values.put("boolean", isBoolean ? Boolean.TRUE : Boolean.FALSE);

      insertMethod(_src, _type, method, _sibling, GETTER_TEMPLATE, values);
    }
    if(method.exists()){
      TypeUtils.getPosition(_type, method);
    }

    return method;
  }

  /**
   * Insert a setter for the supplied property.
   *
   * @param _src The src file.
   * @param _type The type to insert into.
   * @param _sibling The element to insert before.
   * @param _field The property.
   * @param _array true if inserting the array version.
   * @return The method element.
   */
  protected IJavaElement insertSetter (
      ICompilationUnit _src,
      IType _type,
      IJavaElement _sibling,
      IField _field,
      boolean _array)
    throws Exception
  {
    String methodName = "set" + StringUtils.capitalize(_field.getElementName());
    String propertyType =
      Signature.getSignatureSimpleName(_field.getTypeSignature());
    String[] args = new String[]{_field.getTypeSignature()};
    if(_array){
      propertyType = Signature.getSignatureSimpleName(
          Signature.getElementType(_field.getTypeSignature()));
      args = new String[]{
        INT_SIG, Signature.getElementType(_field.getTypeSignature())};
    }
    IMethod method = _type.getMethod(methodName, args);
    if(!method.exists()){
      HashMap<String,Object> values = new HashMap<String,Object>();
      values.put("name", methodName);
      values.put("property", _field.getElementName());
      values.put("propertyType", propertyType);
      values.put("array", _array ? Boolean.TRUE : Boolean.FALSE);
      values.put("boolean", propertyType.equals("boolean") ?
          Boolean.TRUE : Boolean.FALSE);

      insertMethod(_src, _type, method, _sibling, SETTER_TEMPLATE, values);
    }
    if(method.exists()){
      TypeUtils.getPosition(_type, method);
    }

    return method;
  }

  /**
   * Inserts a method using the supplied values.
   *
   * @param _src The src file.
   * @param _type The type to insert into.
   * @param _method The method to be created.
   * @param _sibling The element to insert before.
   * @param _template The template to use.
   * @param _values The values.
   */
  protected void insertMethod (
      ICompilationUnit _src,
      IType _type,
      IMethod _method,
      IJavaElement _sibling,
      String _template,
      Map<String,Object> _values)
    throws Exception
  {
    JavaUtils.loadPreferencesForTemplate(
        _type.getJavaProject().getProject(), getPreferences(), _values);

    IType superType = TypeUtils.getSuperTypeContainingMethod(_type, _method);
    if(superType != null){
      _values.put("superType",
        JavaUtils.getCompilationUnitRelativeTypeName(_src, superType));
      _values.put("overrides",
          superType.isClass() ? Boolean.TRUE : Boolean.FALSE);
      _values.put("implements",
          superType.isClass() ? Boolean.FALSE : Boolean.TRUE);
      _values.put("methodSignature",
          MethodUtils.getMinimalMethodSignature(_method));
    }
    _values.put("interface",
        _type.isInterface() ? Boolean.TRUE : Boolean.FALSE);

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String method = TemplateUtils.evaluate(resources, _template, _values);
    _type.createMethod(method, _sibling, false, null);
  }

  /**
   * Determines the sibling to insert relative to for the next property.
   *
   * @param _type The parent type.
   * @param _fields List of all the fields.
   * @param _field The resolved field.
   * @param _lastSibling The last sibling.
   * @param _methodType The type of the method to be inserted.
   *
   * @return The relative sibling to use.
   */
  protected IJavaElement getSibling (
      IType _type,
      List _fields,
      IField _field,
      IJavaElement _lastSibling,
      int _methodType)
    throws Exception
  {
    // first run through
    if(_lastSibling == null || !_lastSibling.exists()){
      // first try other methods for the same field.
      for(int ii = TYPE_GET; ii <= TYPE_SET_INDEX; ii++){
        if(ii != _methodType){
          IMethod method = getBeanMethod(_type, _field, ii);
          if(method != null){
            if(ii < _methodType){
              method = MethodUtils.getMethodAfter(_type, method);
            }
            if(method != null){
              return method;
            }else{
              return getFirstInnerType(_type);
            }
          }
        }
      }

      int index = _fields.indexOf(_field);

      // insert before the next property's bean methods, if there are other
      // properties.
      if(_fields.size() > 1 && (index + 1) < _fields.size()){
        IMethod method = null;
        for(int ii = index + 1; method == null && ii < _fields.size(); ii++){
          IField field = (IField)_fields.get(ii);
          method = getBeanMethod(_type, field, false);
        }
        if(method != null){
          return method;
        }
      }

      // insert after previous property's bean methods, if there are other
      // properties.
      if(_fields.size() > 1 && index > 0){
        IMethod method = null;
        for(int ii = index - 1; method == null && ii >= 0; ii--){
          IField field = (IField)_fields.get(ii);
          method = getBeanMethod(_type, field, true);
        }
        if(method != null){
          method = MethodUtils.getMethodAfter(_type, method);
          if(method != null){
            return method;
          }
        }
      }

      return getFirstInnerType(_type);
    }
    if(_lastSibling != null && _lastSibling.exists()){
      return _lastSibling;
    }
    return null;
  }

  /**
   * Attempts to get the method of the supplied type for the specified field.
   *
   * @param _type The parent type.
   * @param _field The field to retrieve the method for.
   * @param _methodType The method type.
   * first.
   * @return The method or null if not round.
   */
  protected IMethod getBeanMethod (IType _type, IField _field, int _methodType)
    throws Exception
  {
    String propertyName = StringUtils.capitalize(_field.getElementName());
    String type = Signature.getSignatureSimpleName(_field.getTypeSignature());
    boolean isBoolean = Signature.getSignatureSimpleName(
        _field.getTypeSignature()).equals("boolean");

    String signature = null;
    switch(_methodType){
      case TYPE_GET:
        if(isBoolean){
          signature = "is" + propertyName + "()";
        }else{
          signature = " get" + propertyName + "()";
        }
        break;
      case TYPE_GET_INDEX:
        if(!isBoolean){
          signature = " get" + propertyName + "(int)";
        }
        break;
      case TYPE_SET:
        signature =  "set" + propertyName + '(' + type + ')';
      case TYPE_SET_INDEX:
        if(!isBoolean){
          signature = "set" + propertyName + "(int, " + type + ')';
        }
    }

    if(signature != null){
      IMethod[] methods = _type.getMethods();
      for(int ii = 0; ii < methods.length; ii++){
        if(MethodUtils.getMinimalMethodSignature(methods[ii]).equals(signature)){
          return methods[ii];
        }
      }
    }
    return null;

    // Weird Eclipse bug: too many calls to IType.getMethod() and createMethod()
    // calls will fail later.
    /*switch(_methodType){
      case TYPE_GET:
        if(isBoolean){
          return _type.getMethod("is" + propertyName, null);
        }else{
          return _type.getMethod("get" + propertyName, null);
        }
      case TYPE_GET_INDEX:
        if(!isBoolean){
          return _type.getMethod("get" + propertyName, INT_ARG);
        }
      case TYPE_SET:
        return _type.getMethod("set" + propertyName,
            new String[]{_field.getTypeSignature()});
      case TYPE_SET_INDEX:
        if(!isBoolean){
          return _type.getMethod("set" + propertyName,
              new String[]{INT_SIG, Signature.getElementType(_field.getTypeSignature())});
        }
      default:
        return null;
    }*/
  }

  /**
   * Gets either the first or last occurring bean method for the supplied field.
   *
   * @param _field The field to retrieve the method for.
   * @param _last true to return the last declared bean method, false for the
   * first.
   * @return The method or null if not round.
   */
  protected IMethod getBeanMethod (IType _type, IField _field, boolean _last)
    throws Exception
  {
    boolean isBoolean = Signature.getSignatureSimpleName(
        _field.getTypeSignature()).equals("boolean");

    IMethod result = null;

    String nextProperty = StringUtils.capitalize(_field.getElementName());
    // regular getter
    IMethod method = null;
    if(isBoolean){
      method = _type.getMethod("is" + nextProperty, null);
    }else{
      method = _type.getMethod("get" + nextProperty, null);
    }
    if(method.exists() && !_last){
      return method;
    }else if(method.exists()){
      result = method;
    }

    // index getter
    if(!isBoolean){
      method = _type.getMethod("get" + nextProperty, INT_ARG);
      if(method.exists() && !_last){
        return method;
      }else if(method.exists()){
        result = method;
      }
    }

    // regular setter
    method = _type.getMethod("set" + nextProperty,
        new String[]{_field.getTypeSignature()});
    if(method.exists() && !_last){
      return method;
    }else if(method.exists()){
      result = method;
    }

    // index setter
    if(!isBoolean){
      method = _type.getMethod("set" + nextProperty,
          new String[]{INT_SIG, Signature.getElementType(_field.getTypeSignature())});
      if(method.exists()){
        result = method;
      }
    }

    return result;
  }

  /**
   * Gets the first non-enum inner type.
   *
   * @param _type The parent type.
   * @return The inner type.
   */
  protected IType getFirstInnerType (IType _type)
    throws Exception
  {
    // insert before inner classes.
    IType[] types = _type.getTypes();
    // find the first non-enum type.
    for (int ii = 0; ii < types.length; ii++){
      if(!types[ii].isEnum()){
        return types[ii];
      }
    }
    return null;
  }
}
