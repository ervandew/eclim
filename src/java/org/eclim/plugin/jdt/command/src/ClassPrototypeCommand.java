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
package org.eclim.plugin.jdt.command.src;

import java.io.File;
import java.io.FileWriter;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.SystemUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Command that creates a source prototype of the specified class.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ClassPrototypeCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ClassPrototypeCommand.class);

  private static final String INDENT = "\t";
  private static final String IMPORT_PATTERN = "(<.*>|\\[\\]|\\.[0-9])$";

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String className = _commandLine.getValue(Options.CLASSNAME_OPTION);
      String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

      File file = new File(
        SystemUtils.JAVA_IO_TMPDIR + '/' + className.replace('.', '/') + ".java");
      if(!file.exists()){
        new File(FilenameUtils.getFullPath(file.getAbsolutePath())).mkdirs();
        file.deleteOnExit();
        FileWriter out = null;
        try{
          IJavaProject javaProject = JavaUtils.getJavaProject(projectName);

          IType type = javaProject.findType(className);
          if(type == null){
            throw new IllegalArgumentException(
                Services.getMessage("type.not.found",
                  new Object[]{projectName, className}));
          }

          String prototype = prototype(type);
          out = new FileWriter(file);
          out.write(prototype);
        }finally{
          IOUtils.closeQuietly(out);
        }
      }
      return file.getAbsolutePath();
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Top level method for generating a prototype of the supplied type.
   *
   * @param _type The type.
   * @return The resulting prototype.
   */
  protected String prototype (IType _type)
    throws Exception
  {
    Set imports = new TreeSet();
    StringBuffer buffer = new StringBuffer();
    buffer.append(Services.getMessage("prototype.header"));
    buffer.append("package ")
      .append(_type.getPackageFragment().getElementName())
      .append(";\n");
    prototype(buffer, _type, "", imports);

    // insert the imports
    StringBuffer importClauses = new StringBuffer().append("\n\n");
    for(Iterator ii = imports.iterator(); ii.hasNext();){
      String className = (String)ii.next();
      importClauses.append("import ").append(className).append(";\n");
    }

    buffer.insert(buffer.indexOf(";") + 1, importClauses.toString());

    return buffer.toString();
  }

  /**
   * Prototype the supplied type.
   *
   * @param _buffer The buffer to append to.
   * @param _type The type.
   * @param _indent The indent.
   * @param _imports Keep track of imports.
   */
  protected void prototype (
      StringBuffer _buffer, IType _type, String _indent, Set _imports)
    throws Exception
  {
    _buffer.append(_indent);
    prototypeFlags(_buffer, _type);

    int flags = _type.getFlags();
    if(Flags.isEnum(flags)){
      _buffer.append("enum ");
    }else if(Flags.isInterface(flags)){
      _buffer.append("interface ");
    }else if(Flags.isAnnotation(flags)){
      _buffer.append("@interface ");
    }else{
      _buffer.append("class ");
    }
    _buffer.append(_type.getElementName());

    // extends
    String superclass = _type.getSuperclassName();
    if(superclass != null){
      _buffer.append('\n').append(_indent).append(INDENT)
        .append("extends ").append(superclass);
    }

    // implements
    String[] interfaces = _type.getSuperInterfaceNames();
    if(interfaces != null && interfaces.length > 0){
      _buffer.append('\n').append(_indent).append(INDENT).append("implements ");
      for(int ii = 0; ii < interfaces.length; ii++){
        if(ii != 0){
          _buffer.append(", ");
        }
        _buffer.append(interfaces[ii]);
      }
    }

    _buffer.append('\n').append(_indent).append("{\n");

    int length = _buffer.length();

    // fields
    IField[] fields = _type.getFields();
    for(int ii = 0; ii < fields.length; ii++){
      prototypeField(_buffer, fields[ii], _indent + INDENT, _imports);
    }

    // methods
    IMethod[] methods = _type.getMethods();
    if(methods != null && methods.length > 0){
      for(int ii = 0; ii < methods.length; ii++){
        if(length != _buffer.length()){
          _buffer.append('\n');
        }
        length = _buffer.length();
        prototypeMethod(_buffer, methods[ii], _indent + INDENT, _imports);
      }
    }

    // inner classes, enums, etc.
    IType[] types = _type.getTypes();
    if(types != null && types.length > 0){
      if(length != _buffer.length()){
        _buffer.append('\n');
      }
      for(int ii = 0; ii < types.length; ii++){
        if(ii > 0){
          _buffer.append('\n');
        }
        prototype(_buffer, types[ii], _indent + INDENT, _imports);
        _buffer.append('\n');
      }
    }

    _buffer.append(_indent).append("}");
  }

  /**
   * Prototypes the supplied field.
   *
   * @param _buffer The buffer to append to.
   * @param _field The field.
   * @param _indent The current indentation.
   * @param _imports Keep track of imports.
   */
  protected void prototypeField (
      StringBuffer _buffer, IField _field, String _indent, Set _imports)
    throws Exception
  {
    String fieldName = _field.getElementName();
    if(fieldName.indexOf("$") == -1){
      _buffer.append(_indent);
      prototypeFlags(_buffer, _field);

      String type = _field.getTypeSignature();
      String typeName = Signature.getSignatureSimpleName(type);
      _buffer.append(typeName).append(' ').append(_field.getElementName());

      addImport(_imports, type);

      Object defaultValue = _field.getConstant();
      if(defaultValue != null){
        _buffer.append(" = ");
        if(typeName.equals("char")){
          _buffer.append('\'').append(defaultValue).append('\'');
        }else if(typeName.equals("int") ||
            typeName.equals("long") ||
            typeName.equals("short") ||
            typeName.equals("double") ||
            typeName.equals("float") ||
            typeName.equals("boolean") ||
            typeName.equals("byte"))
        {
          _buffer.append(defaultValue);
        }else if(defaultValue instanceof String){
          _buffer.append('"').append(defaultValue).append('"');
        }else{
          logger.warn("Unhandled constant value: '{}' '{}'",
              defaultValue.getClass().getName(), defaultValue);
        }
      }
      _buffer.append(";\n");
    }
  }

  /**
   * Prototypes the supplied method.
   *
   * @param _buffer The buffer to append to.
   * @param _method The method.
   * @param _indent The current indentation.
   * @param _imports Keep track of imports.
   */
  protected void prototypeMethod (
      StringBuffer _buffer, IMethod _method, String _indent, Set _imports)
    throws Exception
  {
    String methodName = _method.getElementName();
    if(methodName.indexOf("$") == -1 && !methodName.equals("<clinit>")){
      _buffer.append(_indent);
      prototypeFlags(_buffer, _method);
      String returnType = _method.getReturnType();
      String returnTypeName = Signature.getSignatureSimpleName(returnType);

      addImport(_imports, returnType);

      _buffer.append(returnTypeName)
        .append(' ').append(methodName).append(" (");

      // parameters
      String[] paramNames = _method.getParameterNames();
      String[] paramTypes = _method.getParameterTypes();
      if(paramNames.length > 0){
        for(int ii = 0; ii < paramNames.length; ii++){
          if(ii != 0){
            _buffer.append(", ");
          }
          addImport(_imports, paramTypes[ii]);

          String typeName = Signature.getSignatureSimpleName(paramTypes[ii]);
          _buffer.append(typeName).append(' ').append(paramNames[ii]);
        }
      }

      _buffer.append(")");

      // throws
      String[] exceptions = _method.getExceptionTypes();
      if(exceptions.length > 0){
        _buffer.append('\n').append(_indent).append(INDENT).append("throws ");
        for(int ii = 0; ii < exceptions.length; ii++){
          if(ii != 0){
            _buffer.append(", ");
          }
          _buffer.append(Signature.getSignatureSimpleName(exceptions[ii]));
        }
      }

      _buffer.append(";\n");
    }
  }

  /**
   * Prototypes the given member's flags.
   *
   * @param _buffer The buffer to append to.
   * @param _member The member instance.
   */
  protected void prototypeFlags (StringBuffer _buffer, IMember _member)
    throws Exception
  {
    int flags = _member.getFlags();

    if(Flags.isPublic(flags)){
      _buffer.append("public ");
    }else if(Flags.isProtected(flags)){
      _buffer.append("protected ");
    }else if(Flags.isPrivate(flags)){
      _buffer.append("private ");
    }

    if(Flags.isStatic(flags)){
      _buffer.append("static ");
    }
    if(Flags.isFinal(flags)){
      _buffer.append("final ");
    }
    if(Flags.isAbstract(flags)){
      _buffer.append("abstract ");
    }
    if(Flags.isNative(flags)){
      _buffer.append("native ");
    }
    if(Flags.isTransient(flags)){
      _buffer.append("transient ");
    }
    if(Flags.isVolatile(flags)){
      _buffer.append("volatile ");
    }
    if(Flags.isSynchronized(flags)){
      _buffer.append("synchronized ");
    }
  }

  /**
   * Adds the supplied signature to the specified set of imports.
   *
   * @param _imports The imports.
   * @param _signature The signature of the type to add.
   */
  protected void addImport (Set _imports, String _signature)
  {
    String name = Signature.getSignatureSimpleName(_signature);
    if(name.length() > 1 && !name.equals("void")){
      String pckg = Signature.getSignatureQualifier(_signature);
      if(pckg != null && pckg.length() > 0){
        name = pckg + '.' + name;
        _imports.add(name.replaceFirst(IMPORT_PATTERN, ""));
      }
    }
  }
}
