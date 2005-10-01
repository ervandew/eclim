/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.command.project;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.FilenameUtils;

import org.eclim.Services;

import org.eclim.client.Options;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.command.project.classpath.Parser;
import org.eclim.command.project.classpath.Dependency;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Command to update a project.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectUpdateCommand
  extends AbstractCommand
{
  private static final Log log = LogFactory.getLog(ProjectUpdateCommand.class);

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String projectName = _commandLine.getValue(Options.NAME_OPTION);
      String buildfile = _commandLine.getValue(Options.BUILD_FILE_OPTION);
      return update(projectName, buildfile);
    }catch(Throwable t){
      return t;
    }
  }

  /**
   * Updates a project.
   *
   * @param _name The project name.
   * @param _buildfile The buildfile saved, if request originated from one.
   * @return The result.
   */
  protected Object update (String _name, String _buildfile)
    throws Exception
  {
    IJavaModel model = JavaCore.create(
        ResourcesPlugin.getWorkspace().getRoot());
    IJavaProject javaProject = model.getJavaProject(_name);
    if(!javaProject.exists()){
      throw new IllegalArgumentException(Services.getMessage(
            "project.not.found", new Object[]{_name}));
    }

    // .classpath updated.
    if(_buildfile == null){
      javaProject.setRawClasspath(javaProject.readRawClasspath(), null);

    // ivy.xml, project.xml, etc updated.
    }else{
      String filename = FilenameUtils.getName(_buildfile);
      Parser parser = (Parser)Services.getService(filename, Parser.class);
      javaProject.setRawClasspath(
          merge(javaProject, parser.parse(_buildfile)), null);
    }

    javaProject.makeConsistent(null);

    return Services.getMessage("project.updated", new Object[]{_name});
  }

  /**
   * Merges the supplied project's classpath with the specified dependencies.
   *
   * @param _project The project.
   * @param _dependencies The dependencies.
   * @return The classpath entries.
   */
  protected IClasspathEntry[] merge (
      IJavaProject _project, Dependency[] _dependencies)
    throws Exception
  {
    Collection results = new ArrayList();

    // load the results with all the entries.
    IClasspathEntry[] entries = _project.getRawClasspath();
    for(int ii = 0; ii < entries.length; ii++){
      results.add(entries[ii]);
    }

    // merge the dependencies with the classpath entires.
    for(int ii = 0; ii < _dependencies.length; ii ++){
      IClasspathEntry match = null;
      for(int jj = 0; jj < entries.length; jj++){
        if(entries[jj].getEntryKind() == IClasspathEntry.CPE_LIBRARY){
          String path = entries[jj].getPath().toOSString();
          String pattern = _dependencies[ii].getName() +
            Dependency.VERSION_SEPARATOR;

          // exact match
          if(path.endsWith(_dependencies[ii].toString())){
            match = entries[jj];
            break;

          // different version match
          }else if(path.indexOf(pattern) != -1){
            results.remove(entries[jj]);
            break;
          }
        }
      }

      if(match == null){
        // FIXME: how to set this base path?
        IPath path = new Path("/home/ervandew/CDSTiger/tiger/lib")
          .append(_dependencies[ii].toString());
        results.add(JavaCore.newLibraryEntry(path, null, null, true));
      }
    }

    return (IClasspathEntry[])
      results.toArray(new IClasspathEntry[results.size()]);
  }
}
