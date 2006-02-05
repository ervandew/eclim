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

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.apache.commons.io.FilenameUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclipse.jdt.internal.ui.wizards.ClassPathDetector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.launching.JavaRuntime;

import org.jaxen.XPath;

import org.jaxen.dom.DOMXPath;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

/**
 * Command to create a project.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectCreateCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ProjectCreateCommand.class);

  private static DocumentBuilderFactory factory;
  private static XPath xpath;

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String folder = _commandLine.getValue(Options.FOLDER_OPTION);
      String depends = _commandLine.getValue(Options.DEPENDS_OPTION);
      if(folder.endsWith("/") || folder.endsWith("\\")){
        folder = folder.substring(0, folder.length() - 1);
      }
      String name = FilenameUtils.getBaseName(folder).replace(' ', '_');
      logger.debug("Creating project '{}' at folder '{}'", name, folder);

      return create(name, folder, depends);
    }catch(Throwable t){
      return t;
    }
  }

  /**
   * Creates a new project.
   *
   * @param _name The project name.
   * @param _folder The project root folder.
   * @param _depends Comma seperated project names this project depends on.
   * @return The result.
   */
  protected Object create (String _name, String _folder, String _depends)
    throws Exception
  {
    deleteStaleProject(_name, _folder);
    IProject project = createProject(_name, _folder);

    project.open(null);

    IJavaProject javaProject = JavaCore.create(project);
    ClassPathDetector detector = new ClassPathDetector(project, null);
    IClasspathEntry[] detected = detector.getClasspath();
    IClasspathEntry[] depends =
      createOrUpdateDependencies(javaProject, _depends);
    IClasspathEntry[] container = new IClasspathEntry[]{
      JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER))
    };

    IClasspathEntry[] classpath = merge(
        new IClasspathEntry[][]{
          javaProject.readRawClasspath(), detected, depends, container
        });

    javaProject.setRawClasspath(classpath, null);
    javaProject.makeConsistent(null);
    javaProject.save(null, false);

    return Services.getMessage("project.created", _name);
  }

  /**
   * Handle deleting the stale project if it exists.
   *
   * @param _name  The project name.
   * @param _folder The project folder.
   */
  protected void deleteStaleProject (String _name, String _folder)
    throws Exception
  {
    // check for same project location w/ diff project name, or a stale
    // .project file.
    File projectFile = new File(_folder + File.separator + ".project");
    if(projectFile.exists()){
      if(xpath == null){
        xpath = new DOMXPath("/projectDescription/name/text()");
        factory = DocumentBuilderFactory.newInstance();
      }
      Document document = factory.newDocumentBuilder().parse(projectFile);
      String projectName = "";
      Object result = xpath.selectSingleNode(document);
      if(result != null){
        projectName = ((Text)result).getData();
      }

      if(!projectName.equals(_name)){
        IProject project =
          ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if(project.exists()){
          project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
        }else{
          projectFile.delete();
        }
      }
    }
  }

  /**
   * Handle creation of project if necessary.
   *
   * @param _name  The project name.
   * @param _folder The project folder.
   *
   * @return The created project.
   */
  protected IProject createProject (String _name, String _folder)
    throws CoreException
  {
    // create the project if it doesn't already exist.
    IProject project =
      ResourcesPlugin.getWorkspace().getRoot().getProject(_name);
    if(!project.exists()){
      IPath location = new Path(_folder);
      IProjectDescription description =
        ResourcesPlugin.getWorkspace().newProjectDescription(_name);
      // location must not overlap the workspace.
      description.setLocation(location);
      description.setNatureIds(new String[]{JavaCore.NATURE_ID});

      project.create(description, null/*monitor*/);
    }

    // check if the existing project is located elsewhere.
    File path = project.getLocation().toFile();
    if(!path.equals(new File(_folder))){
      throw new IllegalArgumentException(Services.getMessage(
          "project.name.exists",
          new Object[]{_name, path.toString()}));
    }

    return project;
  }

  /**
   * Creates or updates the projects dependecies other other projects.
   *
   * @param _project The project.
   * @param _depends The comma seperated list of project names.
   */
  protected IClasspathEntry[] createOrUpdateDependencies (
      IJavaProject _project, String _depends)
    throws CoreException
  {
    if(_depends != null){
      String[] dependPaths = StringUtils.split(_depends, ',');
      IClasspathEntry[] entries = new IClasspathEntry[dependPaths.length];
      for(int ii = 0; ii < dependPaths.length; ii++){
        IProject theProject =
          ResourcesPlugin.getWorkspace().getRoot().getProject(dependPaths[ii]);
        if(!theProject.exists()){
          throw new IllegalArgumentException(Services.getMessage(
              "project.depends.not.found", dependPaths[ii]));
        }
        IJavaProject otherProject = JavaCore.create(theProject);
        entries[ii] = JavaCore.newProjectEntry(otherProject.getPath(), true);
      }
      return entries;
    }
    return new IClasspathEntry[0];
  }

  /**
   * Merges the supplied classpath entries into one.
   *
   * @param _entries The array of classpath entry arrays to merge.
   *
   * @return The union of all entry arrays.
   */
  protected IClasspathEntry[] merge (IClasspathEntry[][] _entries)
  {
    Collection union = new ArrayList();
    if(_entries != null){
      for(int ii = 0; ii < _entries.length; ii++){
        if(_entries[ii] != null){
          union = CollectionUtils.union(union, Arrays.asList(_entries[ii]));
        }
      }
    }
    return (IClasspathEntry[])union.toArray(new IClasspathEntry[union.size()]);
  }
}
