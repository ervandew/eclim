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

import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

import org.apache.tools.ant.taskdefs.Java;

import org.apache.tools.ant.types.Commandline.Argument;
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

import org.eclipse.jdt.core.IJavaProject;

/**
 * Command to run the project's main class.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL a args ANY"
)
public class JavaCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    IJavaProject javaProject = JavaUtils.getJavaProject(project);

    Project antProject = new Project();
    BuildLogger buildLogger = new DefaultLogger();
    buildLogger.setMessageOutputLevel(Project.MSG_INFO);
    buildLogger.setOutputPrintStream(System.out);
    buildLogger.setErrorPrintStream(System.err);
    antProject.addBuildListener(buildLogger);
    antProject.setBasedir(ProjectUtils.getPath(project));

    String mainClass =
      ProjectUtils.getSetting(project, "org.eclim.java.run.mainclass");

    if (mainClass == null || mainClass.trim().equals(StringUtils.EMPTY)){
      throw new RuntimeException(Services.getMessage(
            "setting.not.set", "org.eclim.java.run.mainclass"));
    }

    Java java = new Java();
    java.setTaskName("java");
    java.setProject(antProject);
    java.setClassname(mainClass);

    // construct classpath
    Path classpath = new Path(antProject);
    String[] paths = ClasspathUtils.getClasspath(javaProject);
    for (String path : paths){
      Path.PathElement pe = classpath.createPathElement();
      pe.setPath(path);
    }

    java.setClasspath(classpath);

    // add any supplied command line args
    String[] args = commandLine.getValues(Options.ARGS_OPTION);
    if (args != null && args.length > 0){
      for(String arg : args){
        Argument a = java.createArg();
        a.setValue(arg);
      }
    }

    java.execute();

    return StringUtils.EMPTY;
  }
}
