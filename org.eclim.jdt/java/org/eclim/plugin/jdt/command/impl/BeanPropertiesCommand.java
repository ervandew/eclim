/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.ASTUtils;
import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.MethodUtils;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclim.util.file.Position;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.core.dom.Modifier;

import org.eclipse.jdt.core.formatter.CodeFormatter;

import org.eclipse.jdt.internal.corext.codemanipulation.AddGetterSetterOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;

import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

import org.eclipse.text.edits.TextEdit;

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
    "REQUIRED e encoding ARG," +
    "REQUIRED r properties ARG," +
    "REQUIRED t type ARG," +
    "OPTIONAL i indexed NOARG"
)
public class BeanPropertiesCommand
  extends AbstractCommand
{
  private static final String GETTER = "getter";
  private static final String SETTER = "setter";

  private static final int TYPE_GET = 0;
  private static final int TYPE_GET_INDEX = 1;
  private static final int TYPE_SET = 2;
  private static final int TYPE_SET_INDEX = 3;

  private static final String INT_SIG =
    Signature.createTypeSignature("int", true);
  private static final String[] INT_ARG = new String[]{INT_SIG};

  private static final IField[] NO_FIELDS = new IField[0];

  @Override
  public Object execute(CommandLine commandLine)
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

    CodeGenerationSettings settings =
        JavaPreferencesSettings.getCodeGenerationSettings(src.getJavaProject());
    settings.createComments = true;

    for(String property : properties){
      IField field = type.getField(property);
      if(field != null){
        boolean isArray = Signature.getArrayCount(field.getTypeSignature()) > 0;

        IField getter = methods.indexOf(GETTER) != -1 ? field : null;
        IField setter = methods.indexOf(SETTER) != -1 ? field : null;
        int methodType = getter != null ? TYPE_GET : TYPE_SET;
        // edge case to prevent insert setter before getter if getter already
        // exists.
        if (getter != null && setter != null &&
            GetterSetterUtil.getGetter(field) != null)
        {
          methodType = TYPE_SET;
        }
        IJavaElement sibling = getSibling(type, fields, field, methodType);
        insertMethods(src, type, sibling, settings, getter, setter);

        if (isArray && indexed){
          insertIndexMethods(src, type, fields, settings, getter, setter);
        }
      }
    }

    return null;
  }

  private void insertMethods(
      ICompilationUnit src,
      IType type,
      IJavaElement sibling,
      CodeGenerationSettings settings,
      IField getter,
      IField setter)
    throws Exception
  {
    AddGetterSetterOperation op = new AddGetterSetterOperation(
        type,
        getter != null ? new IField[]{getter} : NO_FIELDS,
        setter != null ? new IField[]{setter} : NO_FIELDS,
        NO_FIELDS,
        ASTUtils.getCompilationUnit(src), null, sibling, settings, true, true);
    op.run(null);

    TextEdit edit = op.getResultingEdit();
    if (edit != null){
      JavaUtils.format(
          src, CodeFormatter.K_COMPILATION_UNIT,
          edit.getOffset(), edit.getLength());
    }
  }

  private void insertIndexMethods(
      ICompilationUnit src,
      IType type,
      List<IField> fields,
      CodeGenerationSettings settings,
      IField getter,
      IField setter)
    throws Exception
  {
    // eclipse doesn't natively support indexed accessors, so this method runs
    // some regexes on the getter/setter stubs to generate indexed versions.
    if (getter != null){
      IMethod existing = getBeanMethod(type, getter, TYPE_GET_INDEX);
      if (existing == null) {
        IJavaElement sibling = getSibling(type, fields, getter, TYPE_GET_INDEX);
        String name = GetterSetterUtil.getGetterName(getter, null);
        String stub = GetterSetterUtil.getGetterStub(
            getter, name, settings.createComments, Modifier.PUBLIC);
        stub = stub.replaceFirst(
            "\n(\\s*\\*\\s*@return)",
            "\n * @param index the index to get\n$1");
        stub = stub.replaceFirst(
            "\\[\\]\\s*(" + name + "\\s*\\()\\)", " $1int index)");
        stub = stub.replaceFirst("(return\\s+\\w+)(.*?;)", "$1[index]$2");

        IMethod inserted = type.createMethod(stub, sibling, false, null);
        // format the inserted method according to the user's preferences
        Position position = TypeUtils.getPosition(type, inserted);
        JavaUtils.format(
            src, CodeFormatter.K_COMPILATION_UNIT,
            position.getOffset(), position.getLength());
      }
    }

    if (setter != null){
      IMethod existing = getBeanMethod(type, setter, TYPE_SET_INDEX);
      if (existing == null) {
        IJavaElement sibling = getSibling(type, fields, setter, TYPE_SET_INDEX);
        String name = GetterSetterUtil.getSetterName(setter, null);
        String stub = GetterSetterUtil.getSetterStub(
            setter, name, settings.createComments, Modifier.PUBLIC);
        stub = stub.replaceFirst(
            "\n(\\s*\\*\\s*@param)",
            "\n * @param index the index to set\n$1");
        stub = stub.replaceFirst(
            "(" + name + "\\s*\\()(.*?)\\[\\](\\s*.*?)\\)",
            "$1int index, $2$3)");
        stub = stub.replaceFirst("(\\w+)(\\s*=\\s*)", "$1[index]$2");

        IMethod inserted = type.createMethod(stub, sibling, false, null);
        // format the inserted method according to the user's preferences
        Position position = TypeUtils.getPosition(type, inserted);
        JavaUtils.format(
            src, CodeFormatter.K_COMPILATION_UNIT,
            position.getOffset(), position.getLength());
      }
    }
  }

  private IJavaElement getSibling(
      IType type,
      List<IField> fields,
      IField field,
      int methodType)
    throws Exception
  {
    IMethod siblingMethod = null;
    int siblingType = TYPE_GET;
    // first try other methods for the same field.
    for(int ii = TYPE_GET; ii <= TYPE_SET_INDEX; ii++){
      IMethod method = getBeanMethod(type, field, ii);
      if(method != null){
        siblingMethod = method;
        siblingType = ii;
      }
    }
    if (siblingMethod != null){
      if(siblingType < methodType){
        siblingMethod = MethodUtils.getMethodAfter(type, siblingMethod);
      }
      if(siblingMethod != null){
        return siblingMethod;
      }
      return getFirstInnerType(type);
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

  private IType getFirstInnerType(IType type)
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

  private IMethod getBeanMethod(IType type, IField field, int methodType)
    throws Exception
  {
    String name = Signature.getSignatureSimpleName(field.getTypeSignature());
    boolean isArray = Signature.getArrayCount(field.getTypeSignature()) > 0;

    String signature = null;
    switch(methodType){
      case TYPE_GET:
        return GetterSetterUtil.getGetter(field);
      case TYPE_GET_INDEX:
        if(isArray){
          signature = GetterSetterUtil.getGetterName(field, null) + "(int)";
        }
        break;
      case TYPE_SET:
        return GetterSetterUtil.getSetter(field);
      case TYPE_SET_INDEX:
        if(isArray){
          signature = GetterSetterUtil.getSetterName(field, null) +
            "(int, " + name + ')';
        }
    }

    if(signature != null){
      IMethod[] methods = type.getMethods();
      for(int ii = 0; ii < methods.length; ii++){
        String sig = MethodUtils.getMinimalMethodSignature(methods[ii], null);
        if(sig.equals(signature)){
          return methods[ii];
        }
      }
    }
    return null;
  }

  private IMethod getBeanMethod(IType type, IField field, boolean last)
    throws Exception
  {
    IMethod result = null;
    boolean isArray = Signature.getArrayCount(field.getTypeSignature()) > 0;

    // regular getter
    IMethod method = GetterSetterUtil.getGetter(field);
    if(method != null && method.exists() && !last){
      return method;
    }else if(method != null && method.exists()){
      result = method;
    }

    // index getter
    if(isArray){
      method = type.getMethod(
          GetterSetterUtil.getGetterName(field, null), INT_ARG);
      if(method.exists() && !last){
        return method;
      }else if(method.exists()){
        result = method;
      }
    }

    // regular setter
    method = GetterSetterUtil.getSetter(field);
    if(method != null && method.exists() && !last){
      return method;
    }else if(method != null && method.exists()){
      result = method;
    }

    // index setter
    if(!isArray){
      String elementType = Signature.getElementType(field.getTypeSignature());
      method = type.getMethod(
          GetterSetterUtil.getSetterName(field, null),
          new String[]{INT_SIG, elementType});
      if(method.exists()){
        result = method;
      }
    }

    return result;
  }
}
