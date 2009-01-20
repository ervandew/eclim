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

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;

import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;

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

    Map toolChains = ManagedBuildManager.getExtensionToolChainMap();
    IToolChain toolChain = null;
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
        toolChain = tc;
      }
    }

    IConfiguration[] configs = ManagedBuildManager.getExtensionConfigurations(
        toolChain,
        "org.eclipse.cdt.build.core.buildArtefactType",
        "org.eclipse.cdt.build.core.buildArtefactType.exe");

    ManagedProject mproject =
      new ManagedProject(project, configs[0].getProjectType());
    info.setManagedProject(mproject);

    for(IConfiguration cf : configs){
      String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
      Configuration config =
        new Configuration(mproject, (Configuration)cf, id, false, true);
      CConfigurationData data = config.getConfigurationData();
      ICConfigurationDescription cfgDes =
        desc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
      config.setConfigurationDescription(cfgDes);
      config.exportArtifactInfo();

      IBuilder builder = config.getEditableBuilder();
      if (builder != null) {
        builder.setManagedBuildOn(true);
      }

      config.setName(cf.getName());
      config.setArtifactName(project.getName());
    }
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
