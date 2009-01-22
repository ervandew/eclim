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

import java.util.List;
import java.util.Map;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclim.project.ProjectManager;

import org.eclipse.cdt.core.model.CoreModel;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;

import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;

import org.eclipse.core.resources.IProject;

/**
 * Manager for c/c++ projects.
 *
 * @author Eric Van Dewoestine
 */
public class CProjectManager
  implements ProjectManager
{
  /**
   * {@inheritDoc}
   * @see ProjectManager#create(IProject,CommandLine)
   */
  public void create(IProject project, CommandLine commandLine)
    throws Exception
  {
    ICProjectDescriptionManager manager =
      CoreModel.getDefault().getProjectDescriptionManager();
    ICProjectDescription desc = manager.createProjectDescription(project, false);
    ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);

    // FIXME: handle user specified toolchain alias
    Map toolChains = ManagedBuildManager.getExtensionToolChainMap();
    IToolChain toolchain = null;
    for (Object key : toolChains.keySet()){
      IToolChain tc = (IToolChain)toolChains.get(key);
      if (tc.isAbstract() ||
          tc.isSystemObject() ||
          !tc.isSupported() ||
          !ManagedBuildManager.isPlatformOk(tc))
      {
        continue;
      }
      if (tc.getId().endsWith(".base")){
        toolchain = tc;
      }
    }

    ManagedProject mproject = new ManagedProject(desc);
    info.setManagedProject(mproject);

    // TODO: support other project types:
    //       executable, shared library, static library

    String tcId = (toolchain == null) ? "0" : toolchain.getId();
    String cfgName = (toolchain == null) ? "Default" : toolchain.getName();
    Configuration cfg = new Configuration(
        mproject,
        (ToolChain)toolchain,
        ManagedBuildManager.calculateChildId(tcId, null),
        cfgName);

    IBuilder builder = cfg.getEditableBuilder();
    if (builder != null) {
      if(builder.isInternalBuilder()){
        IConfiguration prefCfg =
          ManagedBuildManager.getPreferenceConfiguration(false);
        IBuilder prefBuilder = prefCfg.getBuilder();
        cfg.changeBuilder(
            prefBuilder,
            ManagedBuildManager.calculateChildId(cfg.getId(), null),
            prefBuilder.getName());
        builder = cfg.getEditableBuilder();
        builder.setBuildPath(null);
      }
      builder.setManagedBuildOn(false);
    }
    cfg.setArtifactName(project.getName());
    CConfigurationData data = cfg.getConfigurationData();
    desc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);

    manager.setProjectDescription(project, desc);
  }

  /**
   * {@inheritDoc}
   * @see ProjectManager#update(IProject,CommandLine)
   */
  public List<Error> update(IProject project, CommandLine commandLine)
    throws Exception
  {
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
  }
}
