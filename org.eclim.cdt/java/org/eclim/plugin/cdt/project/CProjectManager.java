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
package org.eclim.plugin.cdt.project;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.plugin.cdt.PluginResources;

import org.eclim.plugin.cdt.util.CUtils;

import org.eclim.plugin.core.project.ProjectManager;

import org.eclim.plugin.core.util.XmlUtils;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.core.index.IIndexManager;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;

import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;

import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;

import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;

import org.eclipse.cdt.managedbuilder.internal.dataprovider.ConfigurationDataProvider;

import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.wizard.IWizard;

import org.eclipse.swt.widgets.Composite;

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

  @Override
  public void create(final IProject project, CommandLine commandLine)
  {
    // TODO: - support other project types:
    //         executable, shared library, static library
    //       - support specifying the toolchain to use.

    final IBuildPropertyManager buildManager =
      ManagedBuildManager.getBuildPropertyManager();
    final IBuildPropertyValue[] vs = buildManager
      .getPropertyType(MBSWizardHandler.ARTIFACT).getSupportedValues();
    Arrays.sort(vs, BuildListComparator.getInstance());

    MBSWizardHandler handler = null;

    toolchainLoop:
    for (IBuildPropertyValue value : vs){
      final IToolChain[] toolChains = ManagedBuildManager
        .getExtensionsToolChains(MBSWizardHandler.ARTIFACT, value.getId(), false);
      if (toolChains != null && toolChains.length > 0){
        for (IToolChain tc : toolChains){
          if (!tc.isAbstract() &&
              !tc.isSystemObject() &&
              tc.isSupported() &&
              ManagedBuildManager.isPlatformOk(tc))
          {
            handler = new LocalMBSWizardHandler(
                value, EclimPlugin.getShell(), null, tc);

            // stop once we've found a toolchain w/ a config
            CfgHolder[] cfgs = handler.getCfgItems(false);
            if (cfgs != null &&
                cfgs.length > 0 &&
                cfgs[0].getConfiguration() != null)
            {
              break toolchainLoop;
            }
          }
        }

        // handle case where no toolchain w/ a config was found, like on
        // windows when eclipse can't find cygwin or mingw.
        IToolChain ntc = ManagedBuildManager
          .getExtensionToolChain(ConfigurationDataProvider.PREF_TC_ID);
        handler = new LocalSTDWizardHandler(
            value, EclimPlugin.getShell(), null, ntc);
      }
    }

    try{
      handler.createProject(project, true, true, new NullProgressMonitor());
      ICProject cproject = CUtils.getCProject(project);
      CCorePlugin.getIndexManager().reindex(cproject);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Error> update(IProject project, CommandLine commandLine)
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
    String cprojectValue = null;
    try{
      cprojectValue = IOUtils.toString(new FileInputStream(dotcproject));
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }
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

  @Override
  public void delete(IProject project, CommandLine commandLine)
  {
  }

  @Override
  public void refresh(IProject project, CommandLine commandLine)
  {
    ICProject cproject = CUtils.getCProject(project);
    CCorePlugin.getIndexManager().reindex(cproject);
    CCorePlugin.getIndexManager()
      .joinIndexer(IIndexManager.FOREVER, new NullProgressMonitor());
  }

  @Override
  public void refresh(IProject project, IFile file)
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

  private class LocalMBSWizardHandler
    extends MBSWizardHandler
  {
    private IToolChain toolchain;

    public LocalMBSWizardHandler(
        IBuildPropertyValue val, Composite p, IWizard w, IToolChain tc)
    {
      super(val, p, w);
      this.toolchain = tc;
    }

    public IToolChain[] getSelectedToolChains()
    {
      return new IToolChain[]{toolchain};
    }

    protected void doCustom(IProject project)
    {
      // no-op
    }

    public CfgHolder[] getCfgItems(boolean defaults)
    {
      CfgHolder[] cfgs = super.getCfgItems(defaults);
      if (cfgs == null || cfgs.length == 0){
        IToolChain tc = ManagedBuildManager
          .getExtensionToolChain(ConfigurationDataProvider.PREF_TC_ID);
        cfgs = new CfgHolder[1];
        cfgs[0] = new CfgHolder(tc, null);
      }
      return cfgs;
    }

    /*public CfgHolder[] getCfgItems(boolean defaults)
    {
      CfgHolder[] cfg = super.getCfgItems(true);

      if (cfg == null || cfg.length == 0){
        IConfiguration config = ManagedBuildManager
          .getExtensionConfiguration(
              "org.eclipse.cdt.build.core.prefbase.cfg");
        //.getExtensionConfiguration(ConfigurationDataProvider.PREF_CFG_ID);

        IToolChain noTc = ManagedBuildManager
          .getExtensionToolChain(ConfigurationDataProvider.PREF_TC_ID);

        cfg = new CfgHolder[1];
        cfg[0] = new CfgHolder(noTc, config);
        logger.warn("No suitable toolchain config found, " +
            "using 'No Toolchain' config.");
      }
      return cfg;
    }*/
  }

  private class LocalSTDWizardHandler
    extends LocalMBSWizardHandler
  {
    public LocalSTDWizardHandler(
        IBuildPropertyValue val, Composite p, IWizard w, IToolChain tc)
    {
      super(val, p, w, tc);
    }

    /**** Copied from STDWizardHandler ****/
    public void createProject(
        IProject project,
        boolean defaults,
        boolean onFinish,
        IProgressMonitor monitor)
      throws CoreException
    {
      try {
        monitor.beginTask("", 100); //$NON-NLS-1$

        setProjectDescription(project, defaults, onFinish, monitor);

        doTemplatesPostProcess(project);
        doCustom(project);
        monitor.worked(30);
      } finally {
        monitor.done();
      }
    }

    /**** Copied from STDWizardHandler ****/
    private void setProjectDescription(
        IProject project,
        boolean defaults,
        boolean onFinish,
        IProgressMonitor monitor)
      throws CoreException
    {
        ICProjectDescriptionManager mngr =
          CoreModel.getDefault().getProjectDescriptionManager();
        ICProjectDescription des =
          mngr.createProjectDescription(project, false, !onFinish);
        ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
        ManagedProject mProj = new ManagedProject(des);
        info.setManagedProject(mProj);
        monitor.worked(20);
        cfgs = CfgHolder.unique(getCfgItems(false));
        cfgs = CfgHolder.reorder(cfgs);
        int work = 50 / cfgs.length;
        for (int i = 0; i < cfgs.length; i++) {
          String s = (cfgs[i].getToolChain() == null) ?
            "0" : ((ToolChain)(cfgs[i].getToolChain())).getId();  //$NON-NLS-1$
          Configuration cfg = new Configuration(
              mProj,
              (ToolChain)cfgs[i].getToolChain(),
              ManagedBuildManager.calculateChildId(s, null),
              cfgs[i].getName());
          cfgs[i].setConfiguration(cfg);
          IBuilder bld = cfg.getEditableBuilder();
          if (bld != null) {
            if(bld.isInternalBuilder()){
              IConfiguration prefCfg =
                ManagedBuildManager.getPreferenceConfiguration(false);
              IBuilder prefBuilder = prefCfg.getBuilder();
              cfg.changeBuilder(
                  prefBuilder,
                  ManagedBuildManager.calculateChildId(cfg.getId(), null),
                  prefBuilder.getName());
              bld = cfg.getEditableBuilder();
              bld.setBuildPath(null);
            }
            bld.setManagedBuildOn(false);
          }
          cfg.setArtifactName(mProj.getDefaultArtifactName());
          CConfigurationData data = cfg.getConfigurationData();
          des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
          monitor.worked(work);
        }
        mngr.setProjectDescription(project, des);
    }
  }
}
