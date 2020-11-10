/**
 * Copyright (C) 2012 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.junit;

import java.io.File;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

import org.apache.tools.ant.taskdefs.optional.junit.BatchTest;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.Environment.Variable;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.ClasspathUtils;
import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;

import org.eclipse.jdt.junit.JUnitCore;

import org.osgi.framework.Bundle;

/**
 * Command to handle execution of junit tests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_junit",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL d debug NOARG," +
    "OPTIONAL h halt NOARG," +
    "OPTIONAL t test ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG"
)
public class JUnitCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String testName = commandLine.getValue(Options.TEST_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int offset = getOffset(commandLine);
    boolean debug = commandLine.hasOption(Options.DEBUG_OPTION);
    boolean halt = commandLine.hasOption(Options.HALT_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);

    IJavaProject javaProject = JavaUtils.getJavaProject(project);
    JUnitTask junit = createJUnitTask(javaProject, debug, halt);

    String[] vmargs =
      getPreferences().getArrayValue(project, "org.eclim.java.junit.jvmargs");
    for(String vmarg : vmargs){
      if (!vmarg.startsWith("-")){
        continue;
      }
      Argument a = junit.createJvmarg();
      a.setValue(vmarg);
    }

    String[] props =
      getPreferences().getArrayValue(project, "org.eclim.java.junit.sysprops");
    for(String prop : props){
      String[] sysprop = StringUtils.split(prop, "=", 2);
      if (sysprop.length != 2){
        continue;
      }
      if (sysprop[0].startsWith("-D")){
        sysprop[0] = sysprop[0].substring(2);
      }
      Variable var = new Variable();
      var.setKey(sysprop[0]);
      var.setValue(sysprop[1]);
      junit.addConfiguredSysproperty(var);
    }

    String[] envs =
      getPreferences().getArrayValue(project, "org.eclim.java.junit.envvars");
    for(String env : envs){
      String[] envvar = StringUtils.split(env, "=", 2);
      if (envvar.length != 2){
        continue;
      }
      Variable var = new Variable();
      var.setKey(envvar[0]);
      var.setValue(envvar[1]);
      junit.addEnv(var);
    }

    if (file != null){
      ICompilationUnit src = JavaUtils.getCompilationUnit(javaProject, file);
      IMethod method = null;
      if (offset != -1){
        IJavaElement element = src.getElementAt(offset);
        if(element != null && element.getElementType() == IJavaElement.METHOD){
          method = (IMethod)element;
        }
      }

      JUnit4TestFinder finder = new JUnit4TestFinder();
      IType type = src.getTypes()[0];
      if (!finder.isTest(type)){
        src = JUnitUtils.findTest(javaProject, type);
        if (src == null){
          println(Services.getMessage("junit.testing.test.not.found"));
          return null;
        }

        if (method != null){
          method = JUnitUtils.findTestMethod(src, method);
          if (method == null){
            println(Services.getMessage("junit.testing.test.method.not.found"));
            return null;
          }
        }
      }

      JUnitTest test = new JUnitTest();
      test.setName(JavaUtils.getFullyQualifiedName(src));
      if (method != null){
        IAnnotation testAnnotation = method.getAnnotation("Test");
        if (testAnnotation == null || !testAnnotation.exists()){
          println(Services.getMessage(
                "junit.testing.test.method.not.annotated",
                method.getElementName()));
          return null;
        }
        test.setMethods(method.getElementName());
      }
      junit.addTest(test);

    }else if (testName != null){
      if (testName.indexOf('*') != -1){
        createBatchTest(javaProject, junit, testName);
      }else{
        JUnitTest test = new JUnitTest();
        test.setName(testName);
        junit.addTest(test);
      }

    }else{
      ArrayList<String> names = new ArrayList<String>();
      IType[] types = JUnitCore.findTestTypes(javaProject, null);
      for (IType type : types) {
        names.add(type.getFullyQualifiedName());
      }
      Collections.sort(names);

      for (String name : names){
        JUnitTest test = new JUnitTest();
        test.setName(name);
        junit.addTest(test);
      }
    }

    try{
      junit.init();
      junit.execute();
    }catch(BuildException be){
      if(debug){
        be.printStackTrace(getContext().err);
      }
    }

    return null;
  }

  private JUnitTask createJUnitTask(
      IJavaProject javaProject, boolean debug, boolean halt)
    throws Exception
  {
    Project antProject = new Project();
    BuildLogger buildLogger = new DefaultLogger();
    buildLogger.setEmacsMode(true);
    buildLogger.setMessageOutputLevel(debug ? Project.MSG_DEBUG : Project.MSG_INFO);
    buildLogger.setOutputPrintStream(getContext().out);
    buildLogger.setErrorPrintStream(getContext().err);
    antProject.addBuildListener(buildLogger);
    antProject.setBasedir(ProjectUtils.getPath(javaProject.getProject()));

    // construct classpath
    Path classpath = new Path(antProject);
    String[] paths = ClasspathUtils.getClasspath(javaProject);
    for (String path : paths){
      Path.PathElement pe = classpath.createPathElement();
      pe.setPath(path);
    }

    // add some ant jar files otherwise ant will fail to load them.
    Bundle bundle = Platform.getBundle("org.apache.ant");
    String pathName = FileLocator.getBundleFile(bundle).getPath();
    classpath.createPathElement().setPath(pathName + "/lib/ant.jar");
    classpath.createPathElement().setPath(pathName + "/lib/ant-junit.jar");
    classpath.createPathElement().setPath(pathName + "/lib/ant-junit4.jar");

    bundle = Platform.getBundle("org.hamcrest.core");
    pathName = FileLocator.getBundleFile(bundle).getPath();
    classpath.createPathElement().setPath(pathName);

    bundle = Platform.getBundle("org.eclim.jdt");
    pathName = FileLocator.getBundleFile(bundle).getPath();
    classpath.createPathElement().setPath(pathName + "/eclim.jdt.jar");

    JUnitTask junit = new JUnitTask();
    junit.setProject(antProject);
    junit.setTaskName("junit");
    junit.setFork(true);

    IProject project = javaProject.getProject();
    String cwd = getPreferences().getValue(project, "org.eclim.java.junit.cwd");
    junit.setDir(new File(
          cwd != null && cwd.trim().length() > 0 ? cwd :
          ProjectUtils.getPath(project)));

    junit.setHaltonerror(halt);
    junit.setHaltonfailure(halt);
    junit.createClasspath().append(classpath);

    // we need to add ant.jar to the classpath for the ant test runner to work,
    // but then JUnitTask will complain about multiple ant jars, so prevent
    // that.
    Field forkedPathChecked =
      JUnitTask.class.getDeclaredField("forkedPathChecked");
    forkedPathChecked.setAccessible(true);
    forkedPathChecked.set(junit, true);

    FormatterElement formatter = new FormatterElement();
    junit.addFormatter(formatter);
    formatter.setClassname("org.eclim.plugin.jdt.command.junit.ResultFormatter");
    formatter.setUseFile(false);

    return junit;
  }

  private void createBatchTest(
      IJavaProject javaProject, JUnitTask junit, String pattern)
    throws Exception
  {
    if (!pattern.endsWith(".java")){
      pattern += ".java";
    }
    BatchTest batch = junit.createBatchTest();
    IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
    for (IClasspathEntry entry : entries){
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE){
        String path = ProjectUtils.getFilePath(
            javaProject.getProject(), entry.getPath().toOSString());
        FileSet fileset = new FileSet();
        fileset.setDir(new File(path));
        fileset.setIncludes(pattern);
        batch.addFileSet(fileset);
      }
    }
  }
}
