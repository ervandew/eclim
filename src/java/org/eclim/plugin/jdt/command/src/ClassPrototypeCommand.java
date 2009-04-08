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
package org.eclim.plugin.jdt.command.src;

import java.io.File;
import java.io.FileWriter;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.SystemUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

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
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_class_prototype",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED c classname ARG"
)
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
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String className = commandLine.getValue(Options.CLASSNAME_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);

    File file = new File(
      SystemUtils.JAVA_IO_TMPDIR + '/' + className.replace('.', '/') + ".java");
    if(!file.exists()){
      new File(FileUtils.getFullPath(file.getAbsolutePath())).mkdirs();
      file.deleteOnExit();
      FileWriter out = null;
      try{
        IJavaProject javaProject = JavaUtils.getJavaProject(projectName);

        IType type = javaProject.findType(className);
        if(type == null){
          throw new IllegalArgumentException(
              Services.getMessage("type.not.found", projectName, className));
        }

        String prototype = prototype(type);
        out = new FileWriter(file);
        out.write(prototype);
      }finally{
        IOUtils.closeQuietly(out);
      }
    }
    return file.getAbsolutePath();
  }

  /**
   * Top level method for generating a prototype of the supplied type.
   *
   * @param type The type.
   * @return The resulting prototype.
   */
  protected String prototype(IType type)
    throws Exception
  {
    Set<String> imports = new TreeSet<String>();
    StringBuffer buffer = new StringBuffer();
    buffer.append(Services.getMessage("prototype.header"));
    buffer.append("package ")
      .append(type.getPackageFragment().getElementName())
      .append(";\n");
    prototype(buffer, type, "", imports);

    // insert the imports
    StringBuffer importClauses = new StringBuffer().append("\n\n");
    for(String className : imports){
      importClauses.append("import ").append(className).append(";\n");
    }

    buffer.insert(buffer.indexOf(";") + 1, importClauses.toString());

    return buffer.toString();
  }

  /**
   * Prototype the supplied type.
   *
   * @param buffer The buffer to append to.
   * @param type The type.
   * @param indent The indent.
   * @param imports Keep track of imports.
   */
  protected void prototype(
      StringBuffer buffer, IType type, String indent, Set<String> imports)
    throws Exception
  {
    buffer.append(indent);
    prototypeFlags(buffer, type);

    int flags = type.getFlags();
    if(Flags.isEnum(flags)){
      buffer.append("enum ");
    }else if(Flags.isInterface(flags)){
      buffer.append("interface ");
    }else if(Flags.isAnnotation(flags)){
      buffer.append("@interface ");
    }else{
      buffer.append("class ");
    }
    buffer.append(type.getElementName());

    // extends
    String superclass = type.getSuperclassName();
    if(superclass != null){
      buffer.append('\n').append(indent).append(INDENT)
        .append("extends ").append(superclass);
    }

    // implements
    String[] interfaces = type.getSuperInterfaceNames();
    if(interfaces != null && interfaces.length > 0){
      buffer.append('\n').append(indent).append(INDENT).append("implements ");
      for(int ii = 0; ii < interfaces.length; ii++){
        if(ii != 0){
          buffer.append(", ");
        }
        buffer.append(interfaces[ii]);
      }
    }

    buffer.append('\n').append(indent).append("{\n");

    int length = buffer.length();

    // fields
    IField[] fields = type.getFields();
    for(int ii = 0; ii < fields.length; ii++){
      prototypeField(buffer, fields[ii], indent + INDENT, imports);
    }

    // methods
    IMethod[] methods = type.getMethods();
    if(methods != null && methods.length > 0){
      for(int ii = 0; ii < methods.length; ii++){
        if(length != buffer.length()){
          buffer.append('\n');
        }
        length = buffer.length();
        prototypeMethod(buffer, methods[ii], indent + INDENT, imports);
      }
    }

    // inner classes, enums, etc.
    IType[] types = type.getTypes();
    if(types != null && types.length > 0){
      if(length != buffer.length()){
        buffer.append('\n');
      }
      for(int ii = 0; ii < types.length; ii++){
        if(ii > 0){
          buffer.append('\n');
        }
        prototype(buffer, types[ii], indent + INDENT, imports);
        buffer.append('\n');
      }
    }

    buffer.append(indent).append("}");
  }

  /**
   * Prototypes the supplied field.
   *
   * @param buffer The buffer to append to.
   * @param field The field.
   * @param indent The current indentation.
   * @param imports Keep track of imports.
   */
  protected void prototypeField(
      StringBuffer buffer, IField field, String indent, Set<String> imports)
    throws Exception
  {
    String fieldName = field.getElementName();
    if(fieldName.indexOf("$") == -1){
      buffer.append(indent);
      prototypeFlags(buffer, field);

      String type = field.getTypeSignature();
      String typeName = Signature.getSignatureSimpleName(type);
      buffer.append(typeName).append(' ').append(field.getElementName());

      addImport(imports, type);

      Object defaultValue = field.getConstant();
      if(defaultValue != null){
        buffer.append(" = ");
        if(typeName.equals("char")){
          buffer.append('\'').append(defaultValue).append('\'');
        }else if(typeName.equals("int") ||
            typeName.equals("long") ||
            typeName.equals("short") ||
            typeName.equals("double") ||
            typeName.equals("float") ||
            typeName.equals("boolean") ||
            typeName.equals("byte")){
          buffer.append(defaultValue);
        }else if(defaultValue instanceof String){
          buffer.append('"').append(defaultValue).append('"');
        }else{
          logger.warn("Unhandled constant value: '{}' '{}'",
              defaultValue.getClass().getName(), defaultValue);
        }
      }
      buffer.append(";\n");
    }
  }

  /**
   * Prototypes the supplied method.
   *
   * @param buffer The buffer to append to.
   * @param method The method.
   * @param indent The current indentation.
   * @param imports Keep track of imports.
   */
  protected void prototypeMethod(
      StringBuffer buffer, IMethod method, String indent, Set<String> imports)
    throws Exception
  {
    String methodName = method.getElementName();
    if(methodName.indexOf("$") == -1 && !methodName.equals("<clinit>")){
      buffer.append(indent);
      prototypeFlags(buffer, method);
      String returnType = method.getReturnType();
      String returnTypeName = Signature.getSignatureSimpleName(returnType);

      addImport(imports, returnType);

      buffer.append(returnTypeName)
        .append(' ').append(methodName).append(" (");

      // parameters
      String[] paramNames = method.getParameterNames();
      String[] paramTypes = method.getParameterTypes();
      if(paramNames.length > 0){
        for(int ii = 0; ii < paramNames.length; ii++){
          if(ii != 0){
            buffer.append(", ");
          }
          addImport(imports, paramTypes[ii]);

          String typeName = Signature.getSignatureSimpleName(paramTypes[ii]);
          buffer.append(typeName).append(' ').append(paramNames[ii]);
        }
      }

      buffer.append(")");

      // throws
      String[] exceptions = method.getExceptionTypes();
      if(exceptions.length > 0){
        buffer.append('\n').append(indent).append(INDENT).append("throws ");
        for(int ii = 0; ii < exceptions.length; ii++){
          if(ii != 0){
            buffer.append(", ");
          }
          buffer.append(Signature.getSignatureSimpleName(exceptions[ii]));
        }
      }

      buffer.append(";\n");
    }
  }

  /**
   * Prototypes the given member's flags.
   *
   * @param buffer The buffer to append to.
   * @param member The member instance.
   */
  protected void prototypeFlags(StringBuffer buffer, IMember member)
    throws Exception
  {
    int flags = member.getFlags();

    if(Flags.isPublic(flags)){
      buffer.append("public ");
    }else if(Flags.isProtected(flags)){
      buffer.append("protected ");
    }else if(Flags.isPrivate(flags)){
      buffer.append("private ");
    }

    if(Flags.isStatic(flags)){
      buffer.append("static ");
    }
    if(Flags.isFinal(flags)){
      buffer.append("final ");
    }
    if(Flags.isAbstract(flags)){
      buffer.append("abstract ");
    }
    if(Flags.isNative(flags)){
      buffer.append("native ");
    }
    if(Flags.isTransient(flags)){
      buffer.append("transient ");
    }
    if(Flags.isVolatile(flags)){
      buffer.append("volatile ");
    }
    if(Flags.isSynchronized(flags)){
      buffer.append("synchronized ");
    }
  }

  /**
   * Adds the supplied signature to the specified set of imports.
   *
   * @param imports The imports.
   * @param signature The signature of the type to add.
   */
  protected void addImport(Set<String> imports, String signature)
  {
    String name = Signature.getSignatureSimpleName(signature);
    if(name.length() > 1 && !name.equals("void")){
      String pckg = Signature.getSignatureQualifier(signature);
      if(pckg != null && pckg.length() > 0){
        name = pckg + '.' + name;
        imports.add(name.replaceFirst(IMPORT_PATTERN, ""));
      }
    }
  }
}
