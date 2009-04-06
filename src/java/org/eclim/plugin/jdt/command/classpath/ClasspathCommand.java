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
package org.eclim.plugin.jdt.command.classpath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Command which returns a list of classpath entries.
 *
 * @author Eric Van Dewoestine
 */
public class ClasspathCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ClasspathCommand.class);

  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);
    IJavaProject javaProject = JavaUtils.getJavaProject(name);

    Set<IJavaProject> visited = new HashSet<IJavaProject>();
    List<String> paths = new ArrayList<String>();
    collect(javaProject, paths, visited, true);
    return StringUtils.join(paths, "\n");
  }

  private static void collect(
      IJavaProject javaProject,
      List<String> paths,
      Set<IJavaProject> visited,
      boolean isFirstProject)
    throws Exception
  {
    if(visited.contains(javaProject)){
      return;
    }
    visited.add(javaProject);

    try{
      IPath out = javaProject.getOutputLocation();
      out = out.addTrailingSeparator();
      String path = ProjectUtils.getFilePath(
          javaProject.getProject(), out.toOSString());
      paths.add(path);
    }catch(JavaModelException ignore){
      // ignore... just signals that no output dir was configured.
    }

    IProject project = javaProject.getProject();
    String name = project.getName();

    IClasspathEntry[] entries = null;
    try {
      entries = javaProject.getResolvedClasspath(true);
    }catch(JavaModelException jme){
      // this may or may not be a problem.
      logger.warn(
          "Unable to retreive resolved classpath for project: " + name, jme);
      return;
    }

    for(IClasspathEntry entry : entries) {
      switch (entry.getEntryKind()) {
        case IClasspathEntry.CPE_LIBRARY :
        case IClasspathEntry.CPE_CONTAINER :
        case IClasspathEntry.CPE_VARIABLE :
          String path = entry.getPath().toOSString();
          if(path.startsWith("/" + name + "/")){
            path = ProjectUtils.getFilePath(project, path);
          }
          paths.add(path);
          break;
        case IClasspathEntry.CPE_PROJECT :
          if (isFirstProject || entry.isExported()){
            javaProject =
              JavaUtils.getJavaProject(entry.getPath().segment(0));
            if (javaProject != null){
              collect(javaProject, paths, visited, false);
            }
          }
          break;
      }
    }
  }
}
