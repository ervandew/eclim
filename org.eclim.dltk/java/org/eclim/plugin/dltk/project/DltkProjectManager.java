/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

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

import org.eclipse.core.runtime.CoreException;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelStatus;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.SourceParserUtil;

import org.eclipse.dltk.internal.core.BuildpathEntry;

import org.eclipse.dltk.internal.ui.wizards.BuildpathDetector;

import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.ScriptRuntime;

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

  @Override
  @SuppressWarnings("static-access")
  public void create(IProject project, CommandLine commandLine)
  {
    String[] args = commandLine.getValues(Options.ARGS_OPTION);
    GnuParser parser = new GnuParser();
    org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
    options.addOption(OptionBuilder.hasArg().withLongOpt("interpreter").create());
    org.apache.commons.cli.CommandLine cli = null;
    try{
      cli = parser.parse(options, args);
    }catch(ParseException pe){
      throw new RuntimeException(pe);
    }

    IInterpreterInstall interpreter = null;
    if (cli.hasOption("interpreter")){
      String interpreterName = cli.getOptionValue("interpreter");
      IInterpreterInstallType[] types =
        ScriptRuntime.getInterpreterInstallTypes(getNatureId());

      loop: for (IInterpreterInstallType type : types){
        IInterpreterInstall[] installs = type.getInterpreterInstalls();
        for (IInterpreterInstall install : installs){
          if (install.getName().equals(interpreterName)){
            interpreter = install;
            break loop;
          }
        }
      }

      if (interpreter == null){
        throw new IllegalArgumentException(Services.getMessage(
            "interpreter.name.not.found", interpreterName));
      }
    }

    String dependsString = commandLine.getValue(Options.DEPENDS_OPTION);

    IScriptProject scriptProject = DLTKCore.create(project);

    try{
      if (!project.getFile(BUILDPATH).exists()) {
        IDLTKLanguageToolkit toolkit = getLanguageToolkit(getNatureId());
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

      if (interpreter != null){
        IBuildpathEntry[] buildpath = scriptProject.getRawBuildpath();
        int containerIndex = 0;
        for (int i = 0; i < buildpath.length; i++){
          if (buildpath[i].getEntryKind() == IBuildpathEntry.BPE_CONTAINER){
            containerIndex = i;
            break;
          }
        }

        if (containerIndex == 0){
          throw new RuntimeException("No container buildpath entry found.");
        }

        IBuildpathEntry container = buildpath[containerIndex];
        buildpath[containerIndex] = DLTKCore.newContainerEntry(
            ScriptRuntime.newInterpreterContainerPath(interpreter),
            container.getAccessRules(),
            container.getExtraAttributes(),
            container.isExported());
        scriptProject.setRawBuildpath(buildpath, null);
      }

      scriptProject.makeConsistent(null);
      scriptProject.save(null, false);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  @Override
  public List<Error> update(IProject project, CommandLine commandLine)
  {
    try{
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
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  @Override
  public void delete(IProject project, CommandLine commandLine)
  {
  }

  @Override
  public void refresh(IProject project, CommandLine commandLine)
  {
    SourceParserUtil.clearCache();
  }

  @Override
  public void refresh(IProject project, IFile file)
  {
  }

  /**
   * Get the language toolkit to use.
   *
   * @param natureId The nature id to get the toolkit for.
   * @return The IDLTKLanguageToolkit to use.
   */
  public IDLTKLanguageToolkit getLanguageToolkit(String natureId)
  {
    return DLTKLanguageManager.getLanguageToolkit(natureId);
  }

  /**
   * Abstract method for subclasses to override which provides the appropriate
   * dltk nature id.
   *
   * @return The nature.
   */
  public abstract String getNatureId();

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
   * @return Array of IBuildpathEntry.
   */
  protected IBuildpathEntry[] createOrUpdateDependencies(
      IScriptProject project, String depends)
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
