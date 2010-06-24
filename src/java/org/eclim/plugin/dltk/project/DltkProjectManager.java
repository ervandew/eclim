/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
package org.eclim.plugin.dltk.project;

import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.project.ProjectManager;

import org.eclim.plugin.core.util.ProjectUtils;
import org.eclim.plugin.core.util.XmlUtils;

import org.eclim.plugin.dltk.PluginResources;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelStatus;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.SourceParserUtil;

import org.eclipse.dltk.internal.core.BuildpathEntry;

import org.eclipse.dltk.internal.ui.wizards.BuildpathDetector;

/**
 * Implementation of {@link ProjectManager} for dltk projects.
 *
 * @author Eric Van Dewoestine
 */
public abstract class DltkProjectManager
  implements ProjectManager
{
  private static final String BUILDPATH = ".buildpath";
  private static final String BUILDPATH_XSD =
    "/resources/schema/eclipse/buildpath.xsd";

  /**
   * {@inheritDoc}
   * @see ProjectManager#create(IProject,CommandLine)
   */
  public void create(IProject project, CommandLine commandLine)
    throws Exception
  {
    String dependsString = commandLine.getValue(Options.DEPENDS_OPTION);

    IScriptProject scriptProject = DLTKCore.create(project);

    if (!project.getFile(BUILDPATH).exists()) {
      IDLTKLanguageToolkit toolkit = getLanguageToolkit();
      BuildpathDetector detector = new BuildpathDetector(project, toolkit);
      detector.detectBuildpath(null);
      IBuildpathEntry[] detected = detector.getBuildpath();

      // remove any entries the detector may have added that are not valid for
      // this project (currently happens on php projects with the
      // org.eclipse.dltk.launching.INTERPRETER_CONTAINER entry).
      ArrayList<IBuildpathEntry> entries = new ArrayList<IBuildpathEntry>();
      for (IBuildpathEntry entry : detected){
        IModelStatus status = BuildpathEntry
          .validateBuildpathEntry(scriptProject, entry, true);
        if(status.isOK()){
          entries.add(entry);
        }
      }
      detected = entries.toArray(new IBuildpathEntry[entries.size()]);

      IBuildpathEntry[] depends =
        createOrUpdateDependencies(scriptProject, dependsString);

      IBuildpathEntry[] buildpath = merge(
          new IBuildpathEntry[][]{detected, depends});
            //scriptProject.readRawClasspath(), detected, depends, container

      scriptProject.setRawBuildpath(buildpath, null);
    }
    scriptProject.makeConsistent(null);
    scriptProject.save(null, false);
  }

  /**
   * {@inheritDoc}
   * @see ProjectManager#update(IProject,CommandLine)
   */
  public List<Error> update(IProject project, CommandLine commandLine)
    throws Exception
  {
    IScriptProject scriptProject = DLTKCore.create(project);
    scriptProject.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);

    // validate that .buildpath xml is well formed and valid.
    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    List<Error> errors = XmlUtils.validateXml(
        scriptProject.getProject().getName(),
        BUILDPATH,
        resources.getResource(BUILDPATH_XSD).toString());
    if(errors.size() > 0){
      return errors;
    }

    String dotbuildpath = scriptProject.getProject().getFile(BUILDPATH)
      .getRawLocation().toOSString();

    IBuildpathEntry[] entries = scriptProject.readRawBuildpath();
    FileOffsets offsets = FileOffsets.compile(dotbuildpath);
    String buildpath = IOUtils.toString(new FileInputStream(dotbuildpath));
    errors = new ArrayList<Error>();
    for(IBuildpathEntry entry : entries){
      IModelStatus status = BuildpathEntry.validateBuildpathEntry(
          scriptProject, entry, true);
      if(!status.isOK()){
        errors.add(createErrorForEntry(
              entry, status, offsets, dotbuildpath, buildpath));
      }
    }

    // always set the buildpath anyways, so that the user can correct the file.
    //if(status.isOK() && errors.isEmpty()){
      scriptProject.setRawBuildpath(entries, null);
      scriptProject.makeConsistent(null);
    //}

    if(errors.size() > 0){
      return errors;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * @see ProjectManager#delete(IProject,CommandLine)
   */
  public void delete(IProject project, CommandLine commandLine)
    throws Exception
  {
  }

  /**
   * {@inheritDoc}
   * @see ProjectManager#refresh(IProject,CommandLine)
   */
  public void refresh(IProject project, CommandLine commandLine)
    throws Exception
  {
    SourceParserUtil.clearCache();
  }

  /**
   * {@inheritDoc}
   * @see ProjectManager#refresh(IProject,IFile)
   */
  public void refresh(IProject project, IFile file)
    throws Exception
  {
  }

  /**
   * Abstract method for subclasses to override which provides the appropriate
   * dltk language toolkit.
   *
   * @return The IDLTKLanguageToolkit to use.
   */
  public abstract IDLTKLanguageToolkit getLanguageToolkit();

  /**
   * Creates an Error from the supplied IModelStatus.
   *
   * @param entry The build path entry.
   * @param status The IModelStatus.
   * @param offsets File offsets for the buildpath file.
   * @param filename The filename of the error.
   * @param contents The contents of the file as a String.
   * @return The Error.
   */
  protected Error createErrorForEntry(
      IBuildpathEntry entry,
      IModelStatus status,
      FileOffsets offsets,
      String filename,
      String contents)
    throws Exception
  {
    int line = 1;
    int col = 1;

    String path = entry.getPath().toOSString();
    Matcher matcher =
      Pattern.compile("path\\s*=(['\"])\\s*\\Q" + path + "\\E\\s*\\1")
        .matcher(contents);
    if(matcher.find()){
      int[] position = offsets.offsetToLineColumn(matcher.start());
      line = position[0];
      col = position[1];
    }

    return new Error(status.getMessage(), filename, line, col, false);
  }

  /**
   * Creates or updates the projects dependencies on other projects.
   *
   * @param project The project.
   * @param depends The comma seperated list of project names.
   */
  protected IBuildpathEntry[] createOrUpdateDependencies(
      IScriptProject project, String depends)
    throws Exception
  {
    if(depends != null){
      String[] dependPaths = StringUtils.split(depends, ',');
      IBuildpathEntry[] entries = new IBuildpathEntry[dependPaths.length];
      for(int ii = 0; ii < dependPaths.length; ii++){
        IProject theProject = ProjectUtils.getProject(dependPaths[ii]);
        if(!theProject.exists()){
          throw new IllegalArgumentException(Services.getMessage(
              "project.depends.not.found", dependPaths[ii]));
        }
        IScriptProject otherProject = DLTKCore.create(theProject);
        entries[ii] = DLTKCore.newProjectEntry(otherProject.getPath(), true);
      }
      return entries;
    }
    return new IBuildpathEntry[0];
  }

  /**
   * Merges the supplied buildpath entries into one.
   *
   * @param entries The array of buildpath entry arrays to merge.
   *
   * @return The union of all entry arrays.
   */
  protected IBuildpathEntry[] merge(IBuildpathEntry[][] entries)
  {
    ArrayList<IBuildpathEntry> union = new ArrayList<IBuildpathEntry>();
    if(entries != null){
      for(IBuildpathEntry[] values : entries){
        if(values != null){
          for(IBuildpathEntry entry : values){
            if(!union.contains(entry)){
              union.add(entry);
            }
          }
        }
      }
    }
    return (IBuildpathEntry[])union.toArray(new IBuildpathEntry[union.size()]);
  }
}
