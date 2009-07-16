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
package org.eclim.plugin.cdt.project;

import java.io.FileInputStream;

import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.plugin.cdt.PluginResources;

import org.eclim.plugin.core.project.ProjectManager;

import org.eclim.plugin.core.util.XmlUtils;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;

import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Manager for c/c++ projects.
 *
 * @author Eric Van Dewoestine
 */
public class CProjectManager
  implements ProjectManager
{
  private static final String CPROJECT_XSD =
    "/resources/schema/eclipse/cproject.xsd";

  private static final Pattern ENTRY_PATTERN =
    Pattern.compile(".*?:\\s+/.*?/(.*)\\s+for .*");

  private static final Pattern INCLUDE_PATTERN =
    Pattern.compile(".*?\\((.*?)\\).*");

  /**
   * {@inheritDoc}
   * @see ProjectManager#create(IProject,CommandLine)
   */
  public void create(IProject project, CommandLine commandLine)
    throws Exception
  {
    // TODO: - support other project types:
    //         executable, shared library, static library
    //       - support specifying the toolchain to use.
    //       - post project create hooks both of these (like on ruby project
    //         create)?

    IBuildPropertyManager buildManager =
      ManagedBuildManager.getBuildPropertyManager();
    IBuildPropertyValue[] vs = buildManager
      .getPropertyType(MBSWizardHandler.ARTIFACT).getSupportedValues();

    MBSWizardHandler handler = null;
    for (IBuildPropertyValue value : vs){
      final IToolChain[] toolChains = ManagedBuildManager
        .getExtensionsToolChains(MBSWizardHandler.ARTIFACT, value.getId(), false);
      if (toolChains != null && toolChains.length > 0){
        handler = new MBSWizardHandler(value, EclimPlugin.getShell(), null){
          public IToolChain[] getSelectedToolChains(){
            for (IToolChain tc : toolChains){
              if (!tc.isAbstract() &&
                  !tc.isSystemObject() &&
                  tc.isSupported() &&
                  ManagedBuildManager.isPlatformOk(tc))
              {
                return new IToolChain[]{tc};
              }
            }
            return new IToolChain[0];
          }


          protected void doCustom(IProject project) {
            // no-op
          }
        };
      }
    }
    handler.createProject(project, true, true, new NullProgressMonitor());
  }

  /**
   * {@inheritDoc}
   * @see ProjectManager#update(IProject,CommandLine)
   */
  public List<Error> update(IProject project, CommandLine commandLine)
    throws Exception
  {
    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    List<Error> errors = XmlUtils.validateXml(
        project.getName(),
        ".cproject",
        resources.getResource(CPROJECT_XSD).toString());
    if(errors.size() > 0){
      return errors;
    }

    // FIXME: force load of file from disk
    ICProjectDescriptionManager manager =
      CoreModel.getDefault().getProjectDescriptionManager();
    //manager.updateProjectDescriptions(new IProject[]{project}, null);

    ICProjectDescription desc = manager.getProjectDescription(project, false);

    ICConfigurationDescription config = desc.getDefaultSettingConfiguration();
    PathEntryTranslator.PathEntryCollector cr =
      PathEntryTranslator.collectEntries(project, config);

    IPathEntry[] entries = cr.getEntries(
        PathEntryTranslator.INCLUDE_USER, config);
    ICProject cproject = CoreModel.getDefault().create(project);
    String dotcproject = project.getFile(".cproject").getRawLocation().toOSString();
    FileOffsets offsets = FileOffsets.compile(dotcproject);
    String cprojectValue = IOUtils.toString(new FileInputStream(dotcproject));
    for (IPathEntry entry : entries){
      ICModelStatus status =
        CoreModel.validatePathEntry(cproject, entry, true, true);
      if(!status.isOK()){
        errors.add(createErrorFromStatus(
              offsets, dotcproject, cprojectValue, entry, status));
      }
    }
    return errors;
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
  }

  /**
   * Creates an Error from the supplied IJavaModelStatus.
   *
   * @param offsets File offsets for the classpath file.
   * @param filename The filename of the error.
   * @param contents The contents of the file as a String.
   * @param status The IJavaModelStatus.
   * @return The Error.
   */
  private Error createErrorFromStatus(
      FileOffsets offsets,
      String filename,
      String contents,
      IPathEntry entry,
      ICModelStatus status)
    throws Exception
  {
    int line = 0;
    int col = 0;

    // get the pattern to search for from the status message.
    Matcher matcher = null;
    String pattern = null;
    switch(entry.getEntryKind()){
      case IPathEntry.CDT_OUTPUT:
      case IPathEntry.CDT_SOURCE:
        matcher = ENTRY_PATTERN.matcher(status.getString());
        pattern = "name=\"" + matcher.replaceFirst("$1") + "\"";
        break;
      case IPathEntry.CDT_INCLUDE:
      case IPathEntry.CDT_INCLUDE_FILE:
        matcher = INCLUDE_PATTERN.matcher(status.getString());
        pattern = "value=\"" + matcher.replaceFirst("$1") + "\"";
        break;
    }

    if (matcher != null){
      // find the pattern in the classpath file.
      matcher = Pattern.compile("\\Q" + pattern + "\\E").matcher(contents);
      if(matcher.find()){
        int[] position = offsets.offsetToLineColumn(matcher.start());
        line = position[0];
        col = position[1];
      }
    }

    return new Error(status.getMessage(), filename, line, col, false);
  }
}
