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
package org.eclim.plugin.jdt.command.doc;

import java.io.File;

import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

import org.apache.tools.ant.taskdefs.Javadoc;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import org.eclim.annotation.Command;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.ClasspathUtils;
import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Handles running javadoc.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "javadoc",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL f file ARG"
)
public class JavadocCommand
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
    String files = commandLine.getValue(Options.FILE_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    IJavaProject javaProject = JavaUtils.getJavaProject(project);

    String dest = ProjectUtils.getSetting(project, "org.eclim.java.doc.dest");
    String packageNames =
      ProjectUtils.getSetting(project, "org.eclim.java.doc.packagenames");

    Project antProject = new Project();
    BuildLogger buildLogger = new DefaultLogger();
    buildLogger.setMessageOutputLevel(Project.MSG_INFO);
    buildLogger.setOutputPrintStream(System.out);
    buildLogger.setErrorPrintStream(System.err);
    antProject.addBuildListener(buildLogger);
    antProject.setBasedir(ProjectUtils.getPath(project));
    Javadoc javadoc = new Javadoc();
    javadoc.setTaskName("javadoc");
    javadoc.setProject(antProject);
    javadoc.setDestdir(new File(ProjectUtils.getFilePath(project, dest)));
    if (packageNames != null && !packageNames.trim().equals(StringUtils.EMPTY)){
      javadoc.setPackagenames(packageNames);
    }

    // construct classpath
    Path classpath = new Path(antProject);
    String[] paths = ClasspathUtils.getClasspath(javaProject);
    for (String path : paths){
      Path.PathElement pe = classpath.createPathElement();
      pe.setPath(path);
    }
    javadoc.setClasspath(classpath);

    if (files == null){
      // construct sourcepath
      String sourcepath =
        ProjectUtils.getSetting(project, "org.eclim.java.doc.sourcepath");
      if (sourcepath != null && !sourcepath.trim().equals(StringUtils.EMPTY)){
        paths = StringUtils.split(sourcepath, " ");
      }else{
        paths = ClasspathUtils.getSrcPaths(javaProject);
      }
      for (String path : paths){
        FileSet fileset = new FileSet();
        fileset.setProject(antProject);
        fileset.setDir(new File(ProjectUtils.getFilePath(project, path)));
        fileset.setIncludes("**/*.java");
        javadoc.addFileset(fileset);
      }
    }else{
      paths = StringUtils.split(files);
      for (String path : paths){
        javadoc.addSource(
            new Javadoc.SourceFile(
              new File(ProjectUtils.getFilePath(project, path))));
      }
    }

    javadoc.execute();

    return StringUtils.EMPTY;
  }
}
