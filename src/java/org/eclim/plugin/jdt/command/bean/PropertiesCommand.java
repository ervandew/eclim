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
package org.eclim.plugin.jdt.command.bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.TemplateUtils;

import org.eclim.plugin.jdt.PluginResources;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.MethodUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Command used to generate java bean property methods (getters / setters).
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_bean_properties",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "REQUIRED r properties ARG," +
    "REQUIRED t type ARG," +
    "OPTIONAL i indexed NOARG"
)
public class PropertiesCommand
  extends AbstractCommand
{
  private static final String GETTER_TEMPLATE = "getter.gst";
  private static final String SETTER_TEMPLATE = "setter.gst";

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
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String methods = commandLine.getValue(Options.TYPE_OPTION);
    String[] properties = StringUtils.split(
        commandLine.getValue(Options.PROPERTIES_OPTION), ',');
    int offset = getOffset(commandLine);
    boolean indexed = commandLine.hasOption(Options.INDEXED_OPTION);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IType type = TypeUtils.getType(src, offset);
    List<IField> fields = Arrays.asList(type.getFields());

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
  }

  /**
   * Insert a getter for the supplied property.
   *
   * @param src The src file.
   * @param type The type to insert into.
   * @param sibling The element to insert before.
   * @param field The field.
   * @param array true if inserting the array version.
   * @return The method element.
   */
  protected IJavaElement insertGetter(
      ICompilationUnit src,
      IType type,
      IJavaElement sibling,
      IField field,
      boolean array)
    throws Exception
  {
    String propertyType =
      Signature.getSignatureSimpleName(field.getTypeSignature());
    String methodName = StringUtils.capitalize(field.getElementName());
    boolean isBoolean = propertyType.equals("boolean");
    if(isBoolean){
      methodName = "is" + methodName;
    }else{
      methodName = "get" + methodName;
    }

    String[] args = null;
    if(array){
      propertyType = Signature.getSignatureSimpleName(
          Signature.getElementType(field.getTypeSignature()));
      args = INT_ARG;
    }
    IMethod method = type.getMethod(methodName, args);
    if(!method.exists()){
      HashMap<String, Object> values = new HashMap<String, Object>();
      values.put("propertyType", propertyType);
      values.put("name", methodName);
      values.put("property", field.getElementName());
      values.put("array", array ? Boolean.TRUE : Boolean.FALSE);
      values.put("isBoolean", isBoolean ? Boolean.TRUE : Boolean.FALSE);

      insertMethod(src, type, method, sibling, GETTER_TEMPLATE, values);
    }
    if(method.exists()){
      TypeUtils.getPosition(type, method);
    }

    return method;
  }

  /**
   * Insert a setter for the supplied property.
   *
   * @param src The src file.
   * @param type The type to insert into.
   * @param sibling The element to insert before.
   * @param field The property.
   * @param array true if inserting the array version.
   * @return The method element.
   */
  protected IJavaElement insertSetter(
      ICompilationUnit src,
      IType type,
      IJavaElement sibling,
      IField field,
      boolean array)
    throws Exception
  {
    String methodName = "set" + StringUtils.capitalize(field.getElementName());
    String propertyType =
      Signature.getSignatureSimpleName(field.getTypeSignature());
    String[] args = new String[]{field.getTypeSignature()};
    if(array){
      propertyType = Signature.getSignatureSimpleName(
          Signature.getElementType(field.getTypeSignature()));
      args = new String[]{
        INT_SIG, Signature.getElementType(field.getTypeSignature())};
    }
    IMethod method = type.getMethod(methodName, args);
    if(!method.exists()){
      HashMap<String, Object> values = new HashMap<String, Object>();
      values.put("name", methodName);
      values.put("property", field.getElementName());
      values.put("propertyType", propertyType);
      values.put("array", array ? Boolean.TRUE : Boolean.FALSE);
      values.put("isBoolean", propertyType.equals("boolean") ?
          Boolean.TRUE : Boolean.FALSE);

      insertMethod(src, type, method, sibling, SETTER_TEMPLATE, values);
    }
    if(method.exists()){
      TypeUtils.getPosition(type, method);
    }

    return method;
  }

  /**
   * Inserts a method using the supplied values.
   *
   * @param src The src file.
   * @param type The type to insert into.
   * @param method The method to be created.
   * @param sibling The element to insert before.
   * @param template The template to use.
   * @param values The values.
   */
  protected void insertMethod(
      ICompilationUnit src,
      IType type,
      IMethod method,
      IJavaElement sibling,
      String template,
      Map<String, Object> values)
    throws Exception
  {
    JavaUtils.loadPreferencesForTemplate(
        type.getJavaProject().getProject(), getPreferences(), values);

    IType superType = TypeUtils.getSuperTypeContainingMethod(type, method);
    if(superType != null){
      values.put("superType",
        JavaUtils.getCompilationUnitRelativeTypeName(src, superType));
      values.put("overrides",
          superType.isClass() ? Boolean.TRUE : Boolean.FALSE);
      values.put("implementof",
          superType.isClass() ? Boolean.FALSE : Boolean.TRUE);
      values.put("methodSignature",
          MethodUtils.getMinimalMethodSignature(method));
    }else{
      values.put("superType", null);
      values.put("overrides", null);
      values.put("implementof", null);
      values.put("methodSignature", null);
    }
    values.put("isinterface", type.isInterface() ? Boolean.TRUE : Boolean.FALSE);

    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    String result = TemplateUtils.evaluate(resources, template, values);
    type.createMethod(result, sibling, false, null);
  }

  /**
   * Determines the sibling to insert relative to for the next property.
   *
   * @param type The parent type.
   * @param fields List of all the fields.
   * @param field The resolved field.
   * @param lastSibling The last sibling.
   * @param methodType The type of the method to be inserted.
   *
   * @return The relative sibling to use.
   */
  protected IJavaElement getSibling(
      IType type,
      List<IField> fields,
      IField field,
      IJavaElement lastSibling,
      int methodType)
    throws Exception
  {
    // first run through
    if(lastSibling == null || !lastSibling.exists()){
      // first try other methods for the same field.
      for(int ii = TYPE_GET; ii <= TYPE_SET_INDEX; ii++){
        if(ii != methodType){
          IMethod method = getBeanMethod(type, field, ii);
          if(method != null){
            if(ii < methodType){
              method = MethodUtils.getMethodAfter(type, method);
            }
            if(method != null){
              return method;
            }else{
              return getFirstInnerType(type);
            }
          }
        }
      }

      int index = fields.indexOf(field);

      // insert before the next property's bean methods, if there are other
      // properties.
      if(fields.size() > 1 && (index + 1) < fields.size()){
        IMethod method = null;
        for(int ii = index + 1; method == null && ii < fields.size(); ii++){
          IField property = (IField)fields.get(ii);
          method = getBeanMethod(type, property, false);
        }
        if(method != null){
          return method;
        }
      }

      // insert after previous property's bean methods, if there are other
      // properties.
      if(fields.size() > 1 && index > 0){
        IMethod method = null;
        for(int ii = index - 1; method == null && ii >= 0; ii--){
          IField property = (IField)fields.get(ii);
          method = getBeanMethod(type, property, true);
        }
        if(method != null){
          method = MethodUtils.getMethodAfter(type, method);
          if(method != null){
            return method;
          }
        }
      }

      return getFirstInnerType(type);
    }
    if(lastSibling != null && lastSibling.exists()){
      return lastSibling;
    }
    return null;
  }

  /**
   * Attempts to get the method of the supplied type for the specified field.
   *
   * @param type The parent type.
   * @param field The field to retrieve the method for.
   * @param methodType The method type.
   * first.
   * @return The method or null if not round.
   */
  protected IMethod getBeanMethod(IType type, IField field, int methodType)
    throws Exception
  {
    String propertyName = StringUtils.capitalize(field.getElementName());
    String name = Signature.getSignatureSimpleName(field.getTypeSignature());
    boolean isBoolean = Signature.getSignatureSimpleName(
        field.getTypeSignature()).equals("boolean");

    String signature = null;
    switch(methodType){
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
        signature =  "set" + propertyName + '(' + name + ')';
      case TYPE_SET_INDEX:
        if(!isBoolean){
          signature = "set" + propertyName + "(int, " + name + ')';
        }
    }

    if(signature != null){
      IMethod[] methods = type.getMethods();
      for(int ii = 0; ii < methods.length; ii++){
        if(MethodUtils.getMinimalMethodSignature(methods[ii]).equals(signature)){
          return methods[ii];
        }
      }
    }
    return null;

    // Weird Eclipse bug: too many calls to IType.getMethod() and createMethod()
    // calls will fail later.
    /*switch(methodType){
      case TYPE_GET:
        if(isBoolean){
          return type.getMethod("is" + propertyName, null);
        }else{
          return type.getMethod("get" + propertyName, null);
        }
      case TYPE_GET_INDEX:
        if(!isBoolean){
          return type.getMethod("get" + propertyName, INT_ARG);
        }
      case TYPE_SET:
        return type.getMethod("set" + propertyName,
            new String[]{field.getTypeSignature()});
      case TYPE_SET_INDEX:
        if(!isBoolean){
          return type.getMethod("set" + propertyName, new String[]{
            INT_SIG, Signature.getElementType(field.getTypeSignature())});
        }
      default:
        return null;
    }*/
  }

  /**
   * Gets either the first or last occurring bean method for the supplied field.
   *
   * @param field The field to retrieve the method for.
   * @param last true to return the last declared bean method, false for the
   * first.
   * @return The method or null if not round.
   */
  protected IMethod getBeanMethod(IType type, IField field, boolean last)
    throws Exception
  {
    boolean isBoolean = Signature.getSignatureSimpleName(
        field.getTypeSignature()).equals("boolean");

    IMethod result = null;

    String nextProperty = StringUtils.capitalize(field.getElementName());
    // regular getter
    IMethod method = null;
    if(isBoolean){
      method = type.getMethod("is" + nextProperty, null);
    }else{
      method = type.getMethod("get" + nextProperty, null);
    }
    if(method.exists() && !last){
      return method;
    }else if(method.exists()){
      result = method;
    }

    // index getter
    if(!isBoolean){
      method = type.getMethod("get" + nextProperty, INT_ARG);
      if(method.exists() && !last){
        return method;
      }else if(method.exists()){
        result = method;
      }
    }

    // regular setter
    method = type.getMethod("set" + nextProperty,
        new String[]{field.getTypeSignature()});
    if(method.exists() && !last){
      return method;
    }else if(method.exists()){
      result = method;
    }

    // index setter
    if(!isBoolean){
      method = type.getMethod("set" + nextProperty, new String[]{
        INT_SIG, Signature.getElementType(field.getTypeSignature())});
      if(method.exists()){
        result = method;
      }
    }

    return result;
  }

  /**
   * Gets the first non-enum inner type.
   *
   * @param type The parent type.
   * @return The inner type.
   */
  protected IType getFirstInnerType(IType type)
    throws Exception
  {
    // insert before inner classes.
    IType[] types = type.getTypes();
    // find the first non-enum type.
    for (int ii = 0; ii < types.length; ii++){
      if(!types[ii].isEnum()){
        return types[ii];
      }
    }
    return null;
  }
}
