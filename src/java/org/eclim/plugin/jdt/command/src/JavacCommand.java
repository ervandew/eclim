/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

import org.apache.tools.ant.taskdefs.Javac;

import org.apache.tools.ant.taskdefs.Javac.ImplementationSpecificArgument;

import org.apache.tools.ant.types.Path;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.ClasspathUtils;
import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.IJavaProject;

import com.google.gson.Gson;

/**
 * Command to run javac on the project source files.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "javac", options = "REQUIRED p project ARG")
public class JavacCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    IJavaProject javaProject = JavaUtils.getJavaProject(project);

    Project antProject = new Project();
    BuildLogger buildLogger = new DefaultLogger();
    buildLogger.setMessageOutputLevel(Project.MSG_INFO);
    buildLogger.setOutputPrintStream(getContext().out);
    buildLogger.setErrorPrintStream(getContext().err);
    antProject.addBuildListener(buildLogger);
    antProject.setBasedir(ProjectUtils.getPath(project));
    //antProject.setProperty(
    //    "build.compiler", "org.eclipse.jdt.core.JDTCompilerAdapter");

    Javac javac = new Javac();
    javac.setTaskName("javac");
    javac.setProject(antProject);
    javac.setFork(true);
    File outputDir = new File(
        ProjectUtils.getFilePath(
          project,
          javaProject.getOutputLocation().toOSString()));
    outputDir.mkdirs();
    javac.setDestdir(outputDir);

    // add default args
    String setting = ProjectUtils.getSetting(project, "org.eclim.java.compile.args");
    if (setting != null && !setting.trim().equals(StringUtils.EMPTY)){
      String[] defaultArgs = (String[])new Gson().fromJson(setting, String[].class);
      if (defaultArgs != null && defaultArgs.length > 0){
        for(String arg : defaultArgs){
          if (!arg.startsWith("-")){
            continue;
          }
          ImplementationSpecificArgument a = javac.createCompilerArg();
          a.setValue(arg);
        }
      }
    }

    // construct classpath
    Path classpath = new Path(antProject);
    String[] paths = ClasspathUtils.getClasspath(javaProject);
    for (String path : paths){
      Path.PathElement pe = classpath.createPathElement();
      pe.setPath(path);
    }
    javac.setClasspath(classpath);

    // construct sourcepath
    String sourcepath =
      ProjectUtils.getSetting(project, "org.eclim.java.compile.sourcepath");
    if (sourcepath != null && !sourcepath.trim().equals(StringUtils.EMPTY)){
      paths = StringUtils.split(sourcepath, " ");
    }else{
      paths = ClasspathUtils.getSrcPaths(javaProject);
    }
    for (String path : paths){
      Path src = javac.createSrc();
      src.setPath(path);
    }
    javac.setIncludes("**/*.java");

    try{
      javac.execute();
    }catch(BuildException be){
      // just print the message: should just indicate that something didn't compile.
      println(be.getMessage());
    }

    return null;
  }
}
