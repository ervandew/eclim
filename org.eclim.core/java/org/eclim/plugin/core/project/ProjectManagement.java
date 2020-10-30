/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.core.project;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;
import org.eclim.plugin.core.util.XmlUtils;

import org.eclim.util.CollectionUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

/**
 * Class that handles registering and retrieving of {@link ProjectManager}s.
 *
 * @author Eric Van Dewoestine
 */
public class ProjectManagement
{
  private static final Logger logger =
    Logger.getLogger(ProjectManagement.class);

  private static HashMap<String, ProjectManager> managers =
    new HashMap<String, ProjectManager>();

  private static XPathExpression xpath;
  private static DocumentBuilderFactory factory;

  /**
   * Registers a ProjectManager.
   *
   * @param nature The project nature that the manager will manage.
   * @param manager The ProjectManager.
   * @return The ProjectManager.
   */
  public static ProjectManager addProjectManager(
      String nature, ProjectManager manager)
  {
    logger.debug("add project manager: nature: {} manager: {}", nature, manager);
    managers.put(nature, manager);
    return manager;
  }

  /**
   * Gets a ProjectManager.
   *
   * @param nature The nature to get the ProjectManager for.
   * @return The ProjectManager or null if none.
   */
  public static ProjectManager getProjectManager(String nature)
  {
    return managers.get(nature);
  }

  /**
   * Gets an array of all register project manager nature names.
   *
   * @return Array of nature names.
   */
  public static String[] getProjectManagerNatures()
  {
    Set<String> registered = managers.keySet();
    return registered.toArray(new String[registered.size()]);
  }

  /**
   * Gets an array of all register project managers.
   *
   * @return Array of ProjectManager.
   */
  public static ProjectManager[] getProjectManagers()
  {
    Collection<ProjectManager> registered = managers.values();
    return registered.toArray(new ProjectManager[registered.size()]);
  }

  /**
   * Creates a project.
   *
   * @param name The project name to use.
   * @param folder The folder to create the project at.
   * @param commandLine The command line for the project create command.
   */
  public static void create(
      String name, String folder, CommandLine commandLine)
  {
    String[] aliases = StringUtils.split(
        commandLine.getValue(Options.NATURE_OPTION), ',');
    // convert from aliases to real nature names.
    ArrayList<String> natures = new ArrayList<String>();
    for (int ii = 0; ii < aliases.length; ii++){
      if(!ProjectNatureFactory.NONE.equals(aliases[ii])){
        String[] ids = ProjectNatureFactory.getNaturesForAlias(aliases[ii]);
        if (ids != null){
          for (String id : ids){
            natures.add(id);
          }
        }else{
          String[] registered = ProjectNatureFactory.getNatureAliases();
          StringBuffer supported = new StringBuffer();
          for (String key : registered){
            if(supported.length() > 0){
              supported.append(", ");
            }
            supported.append(key).append('=')
              .append(ProjectNatureFactory.getNatureForAlias(key));
          }
          throw new IllegalArgumentException(
              "Unable to find nature for alias '" + aliases[ii] + "'.  " +
              "Supported aliases include: " + supported);
        }
      }
    }

    deleteStaleProject(name, folder);
    IProject project = createProject(
        name, folder, (String[])natures.toArray(new String[natures.size()]));
    try{
      project.open(null);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    // calling refresh for those project created against an existing code base.
    // performing a preemptive refresh prevents ProjectUtils.getFile
    // (IFile.refreshLocal) from kicking off a rebuild workspace job, which in
    // turn can cause issues with some select and completion engines
    refresh(project, commandLine, false);

    for (int ii = 0; ii < natures.size(); ii++){
      ProjectManager manager = getProjectManager((String)natures.get(ii));
      if(manager != null){
        manager.create(project, commandLine);
      }
    }

    Preferences.getInstance().clearProjectValueCache(project);
  }

  /**
   * Handle creation of project if necessary.
   *
   * @param name  The project name.
   * @param folder The project folder.
   * @param natures Array of natures.
   *
   * @return The created project.
   */
  protected static IProject createProject(
      String name, String folder, String[] natures)
  {
    // create the project if it doesn't already exist.
    IProject project = ProjectUtils.getProject(name, true);
    if(!project.exists()){
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IPath location = new Path(folder);

      // location must not overlap the workspace (unless it's a nested project)
      IPath workspaceLocation = workspace.getRoot().getRawLocation();
      IPath relLocation = null;
      if (location.toOSString().toLowerCase().startsWith(
            workspaceLocation.toOSString().toLowerCase()))
      {
        relLocation = location.removeFirstSegments(
            location.matchingFirstSegments(workspaceLocation));
      }

      // project is outside the workspace otherwise not at the top of the
      // workspace
      if (relLocation == null || relLocation.segmentCount() != 1) {
        IProjectDescription description = workspace.newProjectDescription(name);
        description.setLocation(location);
        description.setNatureIds(natures);
        try{
          project.create(description, null/*monitor*/);
        }catch(CoreException ce){
          throw new RuntimeException(ce);
        }

      // project is at the workspace root, requires jumping through eclipse
      // hoops
      }else{
        String relName = relLocation.toString();
        // hack for windows... manually remove drive letter
        relName = relName.replaceFirst("^[a-zA-Z]:", "");

        project = ProjectUtils.getProject(relName, true);
        if(!project.exists()){
          IProjectDescription description = workspace.newProjectDescription(relName);
          description.setNatureIds(natures);
          try{
            project.create(description, null/*monitor*/);
          }catch(CoreException ce){
            throw new RuntimeException(ce);
          }
          // FIXME: eclipse will ignore this name change.  need to find the
          // proper way to rename a project in the workspace root if we want to
          // support this.
          /*project.open(null);
          description = project.getDescription();
          description.setName(name);*/
        }
      }

    // project exists, so just add any missing requested natures
    }else{
      try{
        IProjectDescription desc = project.getDescription();
        String[] natureIds = desc.getNatureIds();
        ArrayList<String> modified = new ArrayList<String>();
        ArrayList<String> newNatures = new ArrayList<String>();
        CollectionUtils.addAll(modified, natureIds);
        for(String natureId : natures){
          if (!modified.contains(natureId)){
            modified.add(natureId);
            newNatures.add(natureId);
          }
        }

        desc.setNatureIds((String[])modified.toArray(new String[modified.size()]));
        project.setDescription(desc, new NullProgressMonitor());
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
    }

    return project;
  }

  /**
   * Handle deleting the stale project if it exists.
   *
   * @param name  The project name.
   * @param folder The project folder.
   */
  protected static void deleteStaleProject(String name, String folder)
  {
    // check for same project location w/ diff project name, or a stale
    // .project file.
    File projectFile = new File(folder + File.separator + ".project");
    if(projectFile.exists()){
      if(xpath == null){
        xpath = XmlUtils.createXPathExpression(
            "/projectDescription/name/text()");
        factory = DocumentBuilderFactory.newInstance();
      }

      String projectName = null;
      try{
        Document document = factory.newDocumentBuilder().parse(projectFile);
        projectName = (String)xpath.evaluate(document);
      }catch(IOException ioe){
        throw new RuntimeException(ioe);
      }catch(SAXException se){
        throw new RuntimeException(se);
      }catch(ParserConfigurationException pce){
        throw new RuntimeException(pce);
      }catch(XPathExpressionException xpee){
        throw new RuntimeException(xpee);
      }

      if(!projectName.equals(name)){
        IProject project = ProjectUtils.getProject(projectName);
        if(project.exists()){
          try{
            project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
          }catch(CoreException ce){
            throw new RuntimeException(ce);
          }
        }else{
          projectFile.delete();
        }
      }
    }
  }

  /**
   * Updates a project.
   *
   * @param project The project.
   * @param commandLine The command line for the project create command.
   * @return A List of Error instances or an empty List if there were no errors.
   */
  public static List<Error> update(IProject project, CommandLine commandLine)
  {
    ProjectUtils.assertExists(project);

    ArrayList<Error> errors = new ArrayList<Error>();

    try{
      for (String nature : managers.keySet()){
        if(project.hasNature(nature)){
          ProjectManager manager = ProjectManagement.getProjectManager(nature);
          List<Error> errs = manager.update(project, commandLine);
          if(errs != null){
            errors.addAll(errs);
          }
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return errors;
  }

  /**
   * Removes the nature(s) from a project that this manager manages, or deletes
   * the project if no other natures exist for the project.
   *
   * @param project The project.
   * @param commandLine The command line for the project delete command.
   */
  public static void delete(IProject project, CommandLine commandLine)
  {
    ProjectUtils.assertExists(project);

    try{
      if(!project.isOpen()){
        project.open(null);
      }

      Preferences.getInstance().clearProjectValueCache(project);

      for (String nature : managers.keySet()){
        if(project.hasNature(nature)){
          ProjectManager manager = ProjectManagement.getProjectManager(nature);
          manager.delete(project, commandLine);
        }
      }
    }catch(Exception e){
      logger.debug("Failed to perform nature level delete.", e);
    }finally{
      try{
        project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
    }
  }

  /**
   * Refreshes a project by synchronizing it against the files on disk.
   *
   * @param project The project.
   * @param commandLine The command line for the project refresh command.
   */
  public static void refresh(IProject project, CommandLine commandLine)
  {
    refresh(project, commandLine, true);
  }

  private static void refresh(
      IProject project, CommandLine commandLine, boolean refreshNatures)
  {
    ProjectUtils.assertExists(project);

    try{
      project.refreshLocal(IResource.DEPTH_INFINITE, null);

      if (refreshNatures){
        for (String nature : managers.keySet()){
          if(project.hasNature(nature)){
            ProjectManager manager = ProjectManagement.getProjectManager(nature);
            manager.refresh(project, commandLine);
          }
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    Preferences.getInstance().clearProjectValueCache(project);
  }
}
