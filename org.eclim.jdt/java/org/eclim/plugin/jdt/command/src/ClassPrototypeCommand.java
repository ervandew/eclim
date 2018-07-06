/**
 * Copyright (C) 2005 - 2018  Eric Van Dewoestine
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
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.SystemUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ToolFactory;

import org.eclipse.jdt.internal.core.JrtPackageFragmentRoot;

import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;

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
              Services.getMessage(
                "type.not.found", javaProject.getElementName(), className));
        }
        prototype(type, out);
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
   * @throws Exception any exception while attempting to construct the
   * prototype.
   */
  protected void prototype(String file, Writer writer)
    throws Exception
  {
    try{
      byte[] bytes = IOUtils.toByteArray(new File(file));
      ClassFileBytesDisassembler disassembler =
        ToolFactory.createDefaultClassFileBytesDisassembler();
      writer.write(disassembler.disassemble(bytes, "\n"));
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }
  }

  /**
   * Top level method for generating a prototype of the supplied IType.
   *
   * @param type The IType to create the prototype for.
   * @param writer The writer to output the prototype to.
   * @throws Exception any exception while attempting to construct the
   * prototype.
   */
  protected void prototype(IType type, Writer writer)
    throws Exception
  {
    String fqn = JavaUtils.getFullyQualifiedName(type);
    IClassFile classFile = type.getClassFile();
    if (classFile == null){
      throw new IllegalArgumentException(Services.getMessage(
            "class.not.found", type.getJavaProject().getElementName(), fqn));
    }

    IPackageFragmentRoot root = (IPackageFragmentRoot)
      classFile.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);

    boolean inModule = false;
    if (root instanceof JrtPackageFragmentRoot){
      String jar = ((JrtPackageFragmentRoot)root).getJar().getName();
      inModule = jar.endsWith("jrt-fs.jar");
    }

    IClassFileReader reader;
    if (inModule){
      IModuleDescription module = root.getModuleDescription();
      String name = module.getElementName();
      String jmod = SystemUtils.JAVA_HOME + "/jmods/" + name + ".jmod";
      if (!new File(jmod).exists()){
        throw new IllegalArgumentException(
            Services.getMessage("module.not.found", name));
      }

      reader = ToolFactory.createDefaultClassFileReader(
          jmod,
          "classes/" + fqn.replace('.', '/') + ".class",
          IClassFileReader.ALL);
    }else{
      reader = ToolFactory.createDefaultClassFileReader(
          classFile, IClassFileReader.ALL);
    }

    IClassFileDisassembler disassembler =
      ToolFactory.createDefaultClassFileDisassembler();
    String contents = disassembler.disassemble(reader, "\n");
    writer.write(contents);
  }
}
