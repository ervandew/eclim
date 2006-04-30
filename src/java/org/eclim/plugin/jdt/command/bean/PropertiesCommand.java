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

import java.io.IOException;

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
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.TemplateUtils;

import org.eclim.util.file.Position;

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
  private static final String GETTER_SETTER = "getter_setter";

  private static final String INT_SIG =
    Signature.createTypeSignature("int", true);
  private static final String[] INT_ARG = new String[]{INT_SIG};

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
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

      Position position = new Position(
        type.getResource().getLocation().toOSString(), 0, 0);
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
          // index setter
          if(array && indexed){
            sibling = getSibling(type, fields, field, sibling);
            sibling = insertSetter(position, type, sibling, field, methods, true);
          }
          // setter
          sibling = getSibling(type, fields, field, sibling);
          sibling = insertSetter(position, type, sibling, field, methods, false);
          // index getter
          if(array && indexed){
            sibling = getSibling(type, fields, field, sibling);
            sibling = insertGetter(position, type, sibling, field, methods, true);
          }
          // getter
          sibling = getSibling(type, fields, field, sibling);
          sibling = insertGetter(position, type, sibling, field, methods, false);
        }
      }

      return filter(_commandLine, position);
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Insert a getter for the supplied property.
   *
   * @param _position The position to update.
   * @param _type The type to insert into.
   * @param _sibling The element to insert before.
   * @param _field The field.
   * @param _context The requested context (getter/setter).
   * @param _array true if inserting the array version.
   * @return The method element.
   */
  protected IJavaElement insertGetter (
      Position _position,
      IType _type,
      IJavaElement _sibling,
      IField _field,
      String _context,
      boolean _array)
    throws Exception
  {
    String propertyType =
      Signature.getSignatureSimpleName(_field.getTypeSignature());
    String methodName = StringUtils.capitalize(_field.getElementName());
    if(propertyType.equals("boolean")){
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
    Position position = null;
    if(_context.indexOf(GETTER) != -1 && !method.exists()){
      Map values = new HashMap();
      values.put("propertyType", propertyType);
      values.put("name", methodName);
      values.put("property", _field.getElementName());
      values.put("array", _array ? Boolean.TRUE : Boolean.FALSE);

      position = insertMethod(
          _position, _type, method, _sibling, GETTER_TEMPLATE, values);
    }
    if(method.exists()){
      position = TypeUtils.getPosition(_type, method);
    }

    return method;
  }

  /**
   * Insert a setter for the supplied property.
   *
   * @param _position The position to update.
   * @param _type The type to insert into.
   * @param _sibling The element to insert before.
   * @param _field The property.
   * @param _context The requested context (getter/setter).
   * @param _array true if inserting the array version.
   * @return The method element.
   */
  protected IJavaElement insertSetter (
      Position _position,
      IType _type,
      IJavaElement _sibling,
      IField _field,
      String _context,
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
    Position position = null;
    if(_context.indexOf(SETTER) != -1 && !method.exists()){
      Map values = new HashMap();
      values.put("name", methodName);
      values.put("property", _field.getElementName());
      values.put("propertyType", propertyType);
      values.put("array", _array ? Boolean.TRUE : Boolean.FALSE);

      position = insertMethod(
          _position, _type, method, _sibling, SETTER_TEMPLATE, values);
    }
    if(method.exists()){
      position = TypeUtils.getPosition(_type, method);
    }

    return method;
  }

  /**
   * Inserts a method using the supplied values.
   *
   * @param _position The position to update.
   * @param _type The type to insert into.
   * @param _method The method to be created.
   * @param _sibling The element to insert before.
   * @param _template The template to use.
   * @param _values The values.
   * @return The position the method was insert into.
   */
  protected Position insertMethod (
      Position _position,
      IType _type,
      IMethod _method,
      IJavaElement _sibling,
      String _template,
      Map _values)
    throws Exception
  {
    JavaUtils.loadPreferencesForTemplate(
        _type.getJavaProject().getProject(), getPreferences(), _values);

    IType superType = TypeUtils.getSuperTypeContainingMethod(_type, _method);
    if(superType != null){
      _values.put("superType", superType.getFullyQualifiedName());
      _values.put("overrides",
          superType.isClass() ? Boolean.TRUE : Boolean.FALSE);
      _values.put("implements",
          superType.isClass() ? Boolean.FALSE : Boolean.TRUE);
      _values.put("methodSignature",
          TypeUtils.getMinimalMethodSignature(_method));
    }
    _values.put("interface",
        _type.isInterface() ? Boolean.TRUE : Boolean.FALSE);

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String method = TemplateUtils.evaluate(resources, _template, _values);
    Position position = TypeUtils.getPosition(_type,
        _type.createMethod(method, _sibling, false, null));

    _position.setOffset(position.getOffset());
    if(_position.getLength() != 0){
      _position.setLength(_position.getLength() + 3);
    }
    _position.setLength(_position.getLength() + position.getLength());

    return position;
  }

  /**
   * Determines the sibling to insert relative to for the next property.
   *
   * @param _type The parent type.
   * @param _fields List of all the fields.
   * @param _field The resolved field.
   * @param _lastSibling The last sibling.
   *
   * @return The relative sibling to use.
   */
  protected IJavaElement getSibling (
      IType _type, List _fields, IField _field, IJavaElement _lastSibling)
    throws Exception
  {
    // first run through
    if(_lastSibling == null || !_lastSibling.exists()){
      // insert before the next property's bean methods
      int index = _fields.indexOf(_field) + 1;
      if(index < _fields.size()){
        IField field = (IField)_fields.get(index);
        String nextProperty = StringUtils.capitalize(field.getElementName());
        // regular getter
        IMethod method = _type.getMethod("get" + nextProperty, null);
        // index getter
        method = !method.exists() ?
          _type.getMethod("get" + nextProperty, INT_ARG) : method;
        // regular setter
        method = !method.exists() ?
          _type.getMethod("set" + nextProperty,
              new String[]{field.getTypeSignature()}) : method;
        // index setter
        method = !method.exists() ?
          _type.getMethod("set" + nextProperty,
              new String[]{INT_SIG,
                Signature.getElementType(field.getTypeSignature())}) : method;
        if(method.exists()){
          return method;
        }
      }

      // insert before inner classes.
      IType[] types = _type.getTypes();
      // find the first non-enum type.
      for (int ii = 0; ii < types.length; ii++){
        if(!types[ii].isEnum()){
          return types[ii];
        }
      }
    }
    if(_lastSibling != null && _lastSibling.exists()){
      return _lastSibling;
    }
    return null;
  }
}
