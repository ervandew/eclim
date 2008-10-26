/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.project;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.xpath.XPathExpression;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.project.ProjectNatureFactory;

import org.eclim.util.ProjectUtils;
import org.eclim.util.XmlUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.w3c.dom.Document;

/**
 * Class that handles registering and retrieving of {@link ProjectManager}s.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ProjectManagement
{
  private static final Logger logger =
    Logger.getLogger(ProjectManagement.class);

  private static HashMap<String,ProjectManager> managers =
    new HashMap<String,ProjectManager>();

  private static XPathExpression xpath;
  private static DocumentBuilderFactory factory;

  /**
   * Registers a ProjectManager.
   *
   * @param _nature The project nature that the manager will manage.
   * @param _manager The ProjectManager.
   * @return The ProjectManager.
   */
  public static ProjectManager addProjectManager (
      String _nature, ProjectManager _manager)
  {
    managers.put(_nature, _manager);
    return _manager;
  }

  /**
   * Gets a ProjectManager.
   *
   * @param _nature The nature to get the ProjectManager for.
   * @return The ProjectManager or null if none.
   */
  public static ProjectManager getProjectManager (String _nature)
  {
    return (ProjectManager)managers.get(_nature);
  }

  /**
   * Creates a project.
   *
   * @param _name The project name to use.
   * @param _folder The folder to create the project at.
   * @param _commandLine The command line for the project create command.
   */
  public static void create (
      String _name, String _folder, CommandLine _commandLine)
    throws Exception
  {
    String[] aliases = StringUtils.split(
        _commandLine.getValue(Options.NATURE_OPTION), ',');
    // convert from aliases to real nature names.
    ArrayList<String> natures = new ArrayList<String>();
    for (int ii = 0; ii < aliases.length; ii++){
      if(!ProjectNatureFactory.NONE.equals(aliases[ii])){
        natures.add(ProjectNatureFactory.getNatureForAlias(aliases[ii]));
      }
    }

    deleteStaleProject(_name, _folder);
    IProject project = createProject(
        _name, _folder, (String[])natures.toArray(new String[natures.size()]));
    project.open(null);

    for (int ii = 0; ii < natures.size(); ii++){
      ProjectManager manager = getProjectManager((String)natures.get(ii));
      if(manager != null){
        manager.create(project, _commandLine);
      }
    }
  }

  /**
   * Handle creation of project if necessary.
   *
   * @param _name  The project name.
   * @param _folder The project folder.
   * @param _natures Array of natures.
   *
   * @return The created project.
   */
  protected static IProject createProject (
      String _name, String _folder, String[] _natures)
    throws Exception
  {
    // create the project if it doesn't already exist.
    IProject project = ProjectUtils.getProject(_name, true);
    if(!project.exists()){
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IPath location = new Path(_folder);

      // location must not overlap the workspace.
      IPath workspaceLocation = workspace.getRoot().getRawLocation();
      if(location.toOSString().toLowerCase().startsWith(
            workspaceLocation.toOSString().toLowerCase()))
      {
        String tmpName = location.removeFirstSegments(
            location.matchingFirstSegments(workspaceLocation)).toString();
        // hack for windows... manually remove drive letter
        tmpName = tmpName.replaceFirst("^[A-Z]:", "");

        project = ProjectUtils.getProject(tmpName, true);
        if(!project.exists()){
          IProjectDescription description = workspace.newProjectDescription(tmpName);
          description.setNatureIds(_natures);
          project.create(description, null/*monitor*/);
          // FIXME: eclipse will ignore this name change.  need to find the
          // proper way to rename a project if we want to support this.
          /*project.open(null);
          description = project.getDescription();
          description.setName(_name);*/
        }

      }else{
        IProjectDescription description = workspace.newProjectDescription(_name);
        description.setLocation(location);
        description.setNatureIds(_natures);
        project.create(description, null/*monitor*/);
      }

    /*}else{
      // check if the existing project is located elsewhere.
      File path = project.getLocation().toFile();
      if(!path.equals(new File(_folder))){
        throw new IllegalArgumentException(Services.getMessage(
            "project.name.exists", _name, path.toString()));
      }*/
    }

    return project;
  }

  /**
   * Handle deleting the stale project if it exists.
   *
   * @param _name  The project name.
   * @param _folder The project folder.
   */
  protected static void deleteStaleProject (String _name, String _folder)
    throws Exception
  {
    // check for same project location w/ diff project name, or a stale
    // .project file.
    File projectFile = new File(_folder + File.separator + ".project");
    if(projectFile.exists()){
      if(xpath == null){
        xpath = XmlUtils.createXPathExpression(
            "/projectDescription/name/text()");
        factory = DocumentBuilderFactory.newInstance();
      }
      Document document = factory.newDocumentBuilder().parse(projectFile);
      String projectName = (String)xpath.evaluate(document);

      if(!projectName.equals(_name)){
        IProject project = ProjectUtils.getProject(projectName);
        if(project.exists()){
          project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
        }else{
          projectFile.delete();
        }
      }
    }
  }

  /**
   * Updates a project.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project create command.
   */
  public static List<Error> update (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    ProjectUtils.assertExists(_project);

    ArrayList<Error> errors = new ArrayList<Error>();

    for (String nature : managers.keySet()){
      if(_project.hasNature(nature)){
        ProjectManager manager = ProjectManagement.getProjectManager(nature);
        List<Error> errs = manager.update(_project, _commandLine);
        if(errs != null){
          errors.addAll(errs);
        }
      }
    }
    return errors;
  }

  /**
   * Removes the nature(s) from a project that this manager manages, or deletes
   * the project if no other natures exist for the project.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project delete command.
   */
  public static void delete (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    ProjectUtils.assertExists(_project);

    try{
      if(!_project.isOpen()){
        _project.open(null);
      }

      for (String nature : managers.keySet()){
        if(_project.hasNature(nature)){
          ProjectManager manager = ProjectManagement.getProjectManager(nature);
          manager.delete(_project, _commandLine);
        }
      }
    }catch(Exception e){
      logger.debug("Failed to perform nature level delete.", e);
    }finally{
      _project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
    }
  }

  /**
   * Refreshes a project by synchronizing it against the files on disk.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project refresh command.
   */
  public static void refresh (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    ProjectUtils.assertExists(_project);

    _project.refreshLocal(IResource.DEPTH_INFINITE, null);

    for (String nature : managers.keySet()){
      if(_project.hasNature(nature)){
        ProjectManager manager = ProjectManagement.getProjectManager(nature);
        manager.refresh(_project, _commandLine);
      }
    }
  }
}
