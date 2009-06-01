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
package org.eclim.plugin.pdt.project;

import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.pdt.PluginResources;

import org.eclim.plugin.core.project.ProjectManager;

import org.eclim.plugin.core.util.ProjectUtils;
import org.eclim.plugin.core.util.XmlUtils;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelStatus;
import org.eclipse.dltk.core.IScriptProject;

import org.eclipse.dltk.internal.core.BuildpathEntry;

import org.eclipse.dltk.internal.ui.wizards.BuildpathDetector;

import org.eclipse.php.internal.ui.PHPUILanguageToolkit;

/**
 * Implementation of {@link ProjectManager} for php projects.
 *
 * @author Eric Van Dewoestine
 */
public class PhpProjectManager
  implements ProjectManager
{
  private static final String BUILDPATH_XSD =
    "/resources/schema/eclipse/buildpath.xsd";

  private static final Pattern STATUS_PATTERN = Pattern.compile(".*: '(.*)'.*");

  /**
   * {@inheritDoc}
   */
  public void create(IProject project, CommandLine commandLine)
    throws Exception
  {
    String dependsString = commandLine.getValue(Options.DEPENDS_OPTION);

    IScriptProject scriptProject = DLTKCore.create(project);
    IDLTKLanguageToolkit toolkit =
      PHPUILanguageToolkit.getInstance().getCoreToolkit();
    BuildpathDetector detector = new BuildpathDetector(project, toolkit);
    detector.detectBuildpath(null);
    IBuildpathEntry[] detected = detector.getBuildpath();
    IBuildpathEntry[] depends =
      createOrUpdateDependencies(scriptProject, dependsString);

    IBuildpathEntry[] buildpath = merge(
        new IBuildpathEntry[][]{detected, depends});
          //scriptProject.readRawClasspath(), detected, depends, container

    scriptProject.setRawBuildpath(buildpath, null);
    scriptProject.makeConsistent(null);
    scriptProject.save(null, false);
  }

  /**
   * {@inheritDoc}
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
        ".buildpath",
        resources.getResource(BUILDPATH_XSD).toString());
    if(errors.size() > 0){
      return errors;
    }

    String dotbuildpath = scriptProject.getProject().getFile(".buildpath")
      .getRawLocation().toOSString();

    IBuildpathEntry[] entries = scriptProject.readRawBuildpath();
    FileOffsets offsets = FileOffsets.compile(dotbuildpath);
    String buildpath = IOUtils.toString(new FileInputStream(dotbuildpath));
    errors = new ArrayList<Error>();
    for(IBuildpathEntry entry : entries){
      IModelStatus status = BuildpathEntry.validateBuildpathEntry(
          scriptProject, entry, true);
      if(!status.isOK()){
        errors.add(createErrorFromStatus(offsets, dotbuildpath, buildpath, status));
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
   */
  public void refresh(IProject project, CommandLine commandLine)
    throws Exception
  {
  }

  /**
   * {@inheritDoc}
   */
  public void delete(IProject project, CommandLine commandLine)
    throws Exception
  {
  }

  /**
   * Creates an Error from the supplied IModelStatus.
   *
   * @param offsets File offsets for the buildpath file.
   * @param filename The filename of the error.
   * @param contents The contents of the file as a String.
   * @param status The IModelStatus.
   * @return The Error.
   */
  protected Error createErrorFromStatus(
      FileOffsets offsets, String filename, String contents, IModelStatus status)
    throws Exception
  {
    int line = 1;
    int col = 1;

    // get the pattern to search for from the status message.
    Matcher matcher = STATUS_PATTERN.matcher(status.getMessage());
    String pattern = matcher.replaceFirst("$1");

    // find the pattern in the buildpath file.
    matcher = Pattern.compile("\\Q" + pattern + "\\E").matcher(contents);
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
