/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.SystemUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Command that creates a source prototype of the specified class.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_class_prototype",
  options =
    "REQUIRED c classname ARG," +
    "OPTIONAL p project ARG," +
    "OPTIONAL f file ARG"
)
public class ClassPrototypeCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ClassPrototypeCommand.class);

  private static final String INDENT = "\t";
  private static final String IMPORT_PATTERN = "(<.*>|\\[\\]|\\.[0-9])$";

  private static final String OBJECT = "java/lang/Object";
  private static final String ANNOTATION = "java/lang/annotation/Annotation";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String className = commandLine.getValue(Options.CLASSNAME_OPTION);

    if (!commandLine.hasOption(Options.PROJECT_OPTION) &&
        !commandLine.hasOption(Options.FILE_OPTION))
    {
      throw new RuntimeException(
          Services.getMessage("prototype.missing.argument"));
    }

    File file = new File(
      SystemUtils.JAVA_IO_TMPDIR + '/' + className.replace('.', '/') + ".java");
    new File(FileUtils.getFullPath(file.getAbsolutePath())).mkdirs();
    file.deleteOnExit();
    FileWriter out = null;
    try{

      out = new FileWriter(file);

      if(commandLine.hasOption(Options.FILE_OPTION)){
        prototype(commandLine.getValue(Options.FILE_OPTION), out);
      }else{
        String projectName = commandLine.getValue(Options.PROJECT_OPTION);
        IJavaProject javaProject = JavaUtils.getJavaProject(projectName);
        IType type = javaProject.findType(className);

        if(type == null){
          throw new IllegalArgumentException(
              Services.getMessage("type.not.found", projectName, className));
        }

        String prototype = prototype(type);
        out.write(prototype);
      }

    }finally{
      IOUtils.closeQuietly(out);
    }
    return file.getAbsolutePath();
  }

  /**
   * Generate a prototype for the supplied file path.
   *
   * @param file The path to the class file.
   * @param writer The writer to output the prototype to.
   */
  protected void prototype(String file, Writer writer)
  {
    FileInputStream in = null;
    try{
      in = new FileInputStream(file);
      ClassReader reader = new ClassReader(in);
      reader.accept(new AsmClassVisitor(new PrintWriter(writer)), false);
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }finally{
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Top level method for generating a prototype of the supplied type.
   *
   * @param type The type.
   * @return The resulting prototype.
   */
  protected String prototype(IType type)
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
  {
    try{
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
    }catch(JavaModelException jme){
      throw new RuntimeException(jme);
    }
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
  {
    try{
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
              typeName.equals("byte"))
          {
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
    }catch(JavaModelException jme){
      throw new RuntimeException(jme);
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
  {
    try{
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
    }catch(JavaModelException jme){
      throw new RuntimeException(jme);
    }
  }

  /**
   * Prototypes the given member's flags.
   *
   * @param buffer The buffer to append to.
   * @param member The member instance.
   */
  protected void prototypeFlags(StringBuffer buffer, IMember member)
  {
    try{
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
    }catch(JavaModelException jme){
      throw new RuntimeException(jme);
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

  private static class AsmClassVisitor
    implements ClassVisitor
  {
    private PrintWriter writer;
    private StringBuffer classBuffer;
    private String name;

    /**
     * Constructs a new instance.
     *
     * @param writer The writer for this instance.
     */
    public AsmClassVisitor(PrintWriter writer)
    {
      super();
      this.writer = writer;
    }

    @Override
    public void visit(
        int version, int access,
        String name, String signature,
        String superName, String[] interfaces)
    {
      writer.print(Services.getMessage("prototype.header.asm"));

      int index = name.lastIndexOf('/');
      if(index != -1){
        String pack = name.substring(0, index).replace('/', '.');
        name = name.substring(index + 1);
        writer.print("package ");
        writer.print(pack);
        writer.println(";\n");
      }
      this.name = name;

      classBuffer = new StringBuffer()
        .append(getAccess(access));
      if((access & Opcodes.ACC_INTERFACE) == 1){
        classBuffer.append("interface ");
      }else if((access & Opcodes.ACC_ENUM) == 1){
        classBuffer.append("enum ");
      }else{
        if (interfaces.length > 0 && interfaces[0].equals(ANNOTATION)){
          classBuffer.append("@interface ");
        }else{
          classBuffer.append("class ");
        }
      }
      classBuffer.append(name).append('\n');

      if (!OBJECT.equals(superName)){
        classBuffer.append(INDENT)
          .append("extends ")
          .append(superName.replace('/', '.'))
          .append('\n');
      }

      StringBuffer ifaces = new StringBuffer();
      for(String iface : interfaces){
        if(!ANNOTATION.equals(iface)){
          if(ifaces.length() > 0){
            ifaces.append(", ");
          }
          ifaces.append(iface.replace('/', '.'));
        }
      }

      if(ifaces.length() > 0){
        classBuffer.append(INDENT)
          .append("implements ")
          .append(ifaces)
          .append('\n');
      }

      classBuffer.append("{\n");
    }

    @Override
    public void visitSource(String source, String debug)
    {
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc)
    {
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
    {
      writer.print('@');
      writer.print(getDescName(desc));
      return new AsmAnnotationVisitor();
    }

    @Override
    public void visitAttribute(Attribute attr)
    {
    }

    @Override
    public void visitInnerClass(
        String name, String outerName, String innerName, int access)
    {
      flushClassDeclaration();
    }

    @Override
    public FieldVisitor visitField(
        int access, String name, String desc, String signature, Object value)
    {
      flushClassDeclaration();
      return new AsmFieldVisitor(access, name, desc, value);
    }

    @Override
    public MethodVisitor visitMethod(
        int access, String name, String desc, String signature, String[] exceptions)
    {
      flushClassDeclaration();
      return new AsmMethodVisitor(
          access, name, signature != null ? signature : desc, exceptions);
    }

    private void flushClassDeclaration()
    {
      if (classBuffer != null){
        writer.write(classBuffer.toString());
        classBuffer = null;
      }
    }

    @Override
    public void visitEnd()
    {
      flushClassDeclaration();
      writer.print('}');
    }

    private String getDescName(String desc)
    {
      if (desc != null){
        // genrics
        int indexStart = desc.indexOf('<');
        int indexEnd = desc.lastIndexOf('>');
        if (indexStart != -1 && indexEnd != -1){
          String outer = desc.substring(0, indexStart);
          String inner = desc.substring(indexStart + 1, indexEnd);
          return getDescName(outer) + '<' + getDescName(inner) + '>';
        }

        int index = desc.lastIndexOf('/');
        if (index != -1){
          String name = desc.substring(index + 1);
          if(name.endsWith(";")){
            name = name.substring(0, name.length() - 1);
          }
          while (desc.startsWith("[")){
            desc = desc.substring(1);
            name += "[]";
          }
          return name;
        }

        String primitive = null;
        if ("V".equals(desc)){
          return "void";
        }else if (desc.endsWith("I")){
          primitive = "int";
        }else if (desc.endsWith("J")){
          primitive = "long";
        }else if (desc.endsWith("S")){
          primitive = "short";
        }else if (desc.endsWith("C")){
          primitive = "char";
        }else if (desc.endsWith("B")){
          primitive = "byte";
        }else if (desc.endsWith("Z")){
          primitive = "boolean";
        }
        if (primitive != null){
          for (int ii = 0; ii < desc.length() - 1; ii++){
            primitive += "[]";
          }
          return primitive;
        }
      }
      return StringUtils.EMPTY;
    }

    private String getAccess(int access)
    {
      StringBuffer buffer = new StringBuffer();
      if((access & Opcodes.ACC_PUBLIC) != 0){
        buffer.append("public ");
      }

      if((access & Opcodes.ACC_PRIVATE) != 0){
        buffer.append("private ");
      }

      if((access & Opcodes.ACC_PROTECTED) != 0){
        buffer.append("protected ");
      }

      if((access & Opcodes.ACC_STATIC) != 0){
        buffer.append("static ");
      }

      if((access & Opcodes.ACC_FINAL) != 0){
        buffer.append("final ");
      }

      if((access & Opcodes.ACC_TRANSIENT) != 0){
        buffer.append("transient ");
      }

      if((access & Opcodes.ACC_NATIVE) != 0){
        buffer.append("native ");
      }

      if((access & Opcodes.ACC_SYNCHRONIZED) != 0){
        buffer.append("synchronized ");
      }

      if((access & Opcodes.ACC_ABSTRACT) != 0){
        buffer.append("abstract ");
      }

      return buffer.toString();
    }

    private String getValueString(Object value)
    {
      if(value instanceof String){
        return new StringBuffer()
          .append('"').append(value).append('"').toString();
      }else if(value instanceof Character){
        return new StringBuffer()
          .append('\'').append(value).append('\'').toString();
      }
      return String.valueOf(value);
    }

    private class AsmAnnotationVisitor
      implements AnnotationVisitor
    {
      private boolean wrote = false;
      private boolean wroteOpenParen = false;

      @Override
      public void visit(String name, Object value)
      {
        if(name != null){
          writeOpenParen();
          writer.print(name);
          writer.print(" = ");
          writer.print(getValueString(value));
          wrote = true;
        }
      }

      @Override
      public void visitEnum(String name, String desc, String value)
      {
        writeOpenParen();
        writer.print(getDescName(desc));
        writer.print('.');
        writer.print(value);
        wrote = true;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String name, String desc)
      {
        return new AsmAnnotationVisitor();
      }

      @Override
      public AnnotationVisitor visitArray(String name)
      {
        return new AsmAnnotationVisitor();
      }

      @Override
      public void visitEnd()
      {
        if (wrote){
          if(wroteOpenParen){
            writer.print(')');
          }
          writer.println();
          wrote = false;
        }
      }

      private void writeOpenParen()
      {
        if (!wroteOpenParen){
          writer.print('(');
          wroteOpenParen = true;
        }else{
          writer.print(", ");
        }
      }
    }

    private class AsmMethodVisitor
      implements MethodVisitor
    {
      private int access;
      private String name;
      private String desc;
      private String[] exceptions;

      /**
       * Constructs a new instance.
       *
       * @param access The access for this instance.
       * @param name The name for this instance.
       * @param desc The desc for this instance.
       * @param exceptions The exceptions for this instance.
       */
      public AsmMethodVisitor(
          int access, String name, String desc, String[] exceptions)
      {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.exceptions = exceptions;
      }

      @Override
      public AnnotationVisitor visitAnnotationDefault()
      {
        return new AsmAnnotationVisitor();
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible)
      {
        return new AsmAnnotationVisitor();
      }

      @Override
      public AnnotationVisitor visitParameterAnnotation(
          int parameter, String desc, boolean visible)
      {
        return new AsmAnnotationVisitor();
      }

      @Override
      public void visitAttribute(Attribute attr)
      {
      }

      @Override
      public void visitCode()
      {
      }

      @Override
      public void visitInsn(int opcode)
      {
      }

      @Override
      public void visitIntInsn(int opcode, int operand)
      {
      }

      @Override
      public void visitVarInsn(int opcode, int var)
      {
      }

      @Override
      public void visitTypeInsn(int opcode, String desc)
      {
      }

      @Override
      public void visitFieldInsn(
          int opcode, String owner, String name, String desc)
      {
      }

      @Override
      public void visitMethodInsn(
          int opcode, String owner, String name, String desc)
      {
      }

      @Override
      public void visitJumpInsn(int opcode, Label operand)
      {
      }

      @Override
      public void visitLabel(Label label)
      {
      }

      @Override
      public void visitLdcInsn(Object cst)
      {
      }

      @Override
      public void visitIincInsn(int var, int increment)
      {
      }

      @Override
      public void visitTableSwitchInsn(
          int min, int max, Label dflt, Label[] labels)
      {
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
      {
      }

      @Override
      public void visitMultiANewArrayInsn(String desc, int dims)
      {
      }

      @Override
      public void visitTryCatchBlock(
          Label start, Label end, Label handler, String type)
      {
      }

      @Override
      public void visitLocalVariable(
          String name, String desc, String signature,
          Label start, Label end, int index)
      {
      }

      @Override
      public void visitLineNumber(int line, Label start)
      {
      }

      @Override
      public void visitMaxs(int maxStack, int maxLocal)
      {
      }

      @Override
      public void visitEnd()
      {
        // skip static initializers
        if ("<clinit>".equals(name)){
          return;
        }

        if ("<init>".equals(name)){
          name = AsmClassVisitor.this.name;
        }

        String ret = "void";
        int index = desc.lastIndexOf(')');
        if(index != -1){
          ret = getDescName(desc.substring(index + 1, desc.length()));
        }

        StringBuffer params = new StringBuffer();
        int openParen = desc.indexOf('(');
        int closeParen = desc.indexOf(')');
        if (closeParen > (openParen + 1)){
          String args = desc.substring(openParen + 1, closeParen);
          StringBuffer buffer = new StringBuffer();
          int paramIndex = 0;
          int typeLevel = 0;
          boolean inType = false;
          for(char c : args.toCharArray()){
            // arrays, generic type
            if (c == '[' || c == '<'){
              inType = false;
              buffer.append(c);
              continue;
            }

            // end of non-primitive or primitive type
            if (c == ';' || (
                  !inType &&
                  Character.isLetter(c) &&
                  Character.isUpperCase(c) &&
                  c != 'L'))
            {
              buffer.append(c);

              // edge case for generics
              if (c == ';'){
                typeLevel -= 1;
                inType = false;
              }

              if (typeLevel > 0){
                continue;
              }

              if(params.length() != 0){
                params.append(", ");
              }
              params.append(getDescName(buffer.toString()));
              params.append(" arg").append(paramIndex++);
              buffer = new StringBuffer();
              continue;
            }

            if ((!inType && c == 'L') || c == '<'){
              typeLevel += 1;
              inType = true;
            }
            buffer.append(c);
          }
        }

        writer.print(INDENT);
        writer.print(getAccess(access));
        writer.print(ret);
        writer.print(' ');
        writer.print(name);
        writer.print('(');
        writer.print(params);
        writer.print(')');
        if(exceptions != null && exceptions.length > 0){
          writer.println();
          writer.print(INDENT);
          writer.print(INDENT);
          writer.print("throws ");
          for(int ii = 0; ii < exceptions.length; ii++){
            if(ii > 0){
              writer.print(", ");
            }
            writer.print(getDescName(exceptions[ii]));
          }
        }
        writer.print(";\n");
      }
    }

    private class AsmFieldVisitor
      implements FieldVisitor
    {
      private int access;
      private String name;
      private String desc;
      private Object value;

      /**
       * Constructs a new instance.
       *
       * @param access The access for this instance.
       * @param name The name for this instance.
       * @param desc The desc for this instance.
       * @param value The value for this instance.
       */
      public AsmFieldVisitor(int access, String name, String desc, Object value)
      {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.value = value;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible)
      {
        return new AsmAnnotationVisitor();
      }

      @Override
      public void visitAttribute(Attribute attr)
      {
      }

      @Override
      public void visitEnd()
      {
        writer.print(INDENT);
        writer.print(getAccess(access));
        writer.print(getDescName(desc));
        writer.print(' ');
        writer.print(name);
        if(value != null){
          writer.print(" = ");
          writer.print(getValueString(value));
        }
        writer.print(";\n");
      }
    }
  }
}
