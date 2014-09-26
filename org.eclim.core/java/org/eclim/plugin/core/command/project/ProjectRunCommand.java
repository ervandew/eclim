/**
 * Copyright (C) 2012 Tyler Dodge
 * With changes by Daniel Leong (2014)
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
package org.eclim.plugin.core.command.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;

/**
 * Command to execute a run configuration
 *
 * @author Daniel Leong, Tyler Dodge
 */
@Command(
  name = "project_run",
  options =
    "OPTIONAL p project ARG," +
    "OPTIONAL l list NOARG," +
    "OPTIONAL d debug NOARG," +
    "OPTIONAL n name ARG"
)
public class ProjectRunCommand
  extends AbstractCommand
{
    private static final Logger logger =
        Logger.getLogger(ProjectRunCommand.class);


  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    final boolean list = commandLine.hasOption(Options.LIST_OPTION);
    final String configName = commandLine.getValue(Options.NAME_OPTION);
    final String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    final String group;
    if (commandLine.hasOption(Options.DEBUG_OPTION)) {
      group = IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP;
    } else {
      group = IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
    }

    // find the actual mode for that group
    final String mode = getGroupMode(group);
    if (mode == null) {
        throw new IllegalStateException("Invalid group mode. Should never happen");
    }

    // get the requested project
    final IProject project;
    if (projectName != null) {
      project = ProjectUtils.getProject(projectName, true);
      if (!project.exists()) {
        return Services.getMessage("project.not.found", projectName);
      }
    } else {
      project = null;
    }

    final ILaunchConfiguration[] configs = DebugPlugin.getDefault()
        .getLaunchManager().getLaunchConfigurations();
    final List<ILaunchConfiguration> projectConfigs =
        new ArrayList<ILaunchConfiguration>();
    for (final ILaunchConfiguration config : configs) {
      IProject configProject = getProject(config);
      if (configProject == null) {
        continue;
      }
      if (project == null || project.equals(configProject)) {
        projectConfigs.add(config);
      }
    }

    if (list) {
      ArrayList<HashMap<String, Object>> results =
        new ArrayList<HashMap<String, Object>>();
      for (final ILaunchConfiguration config : projectConfigs) {
        final HashMap<String, Object> result =
            new HashMap<String, Object>();
        result.put("name", config.getName());
        result.put("type", config.getType().getName());
        result.put("project", getProject(config).getName());
        results.add(result);
      }
      return results;
    }

    if (project == null) {
      return Services.getMessage("project.execute.needproject");
    }

    final ILaunchConfiguration chosen;
    if (configName != null) {
      chosen = findConfiguration(projectConfigs, configName);
    } else {
      // just get the first
      chosen = projectConfigs.get(0);
    }

    if (chosen == null) {
      return Services.getMessage("project.execute.invalid", projectName);
    }

    // Without this, buildAndLaunch throws NPE;
    //  perhaps we could dump the output like the :JUnit command?
    final IProgressMonitor monitor = new IProgressMonitor() {

        @Override
        public void beginTask(String name, int totalWork)
        {
            logger.info("Begin: " + name + " / " + totalWork);
        }

        @Override
        public void done()
        {
            logger.info("Done!");
        }

        @Override
        public void internalWorked(double work)
        {
            logger.info("Internal..." + work);
        }

        @Override
        public boolean isCanceled()
        {
            return false;
        }

        @Override
        public void setCanceled(boolean value)
        {
            logger.info("Cancel!" + value);
        }

        @Override
        public void setTaskName(String name)
        {
            logger.info("Now called: " + name);
        }

        @Override
        public void subTask(String name)
        {
            logger.info("subtask: " + name);
        }

        @Override
        public void worked(int work)
        {
            logger.info("worked... " + work);
        }
    };

    logger.info("Launching: " + chosen + " in mode: " + mode);
    ILaunch launch = DebugUITools.buildAndLaunch(chosen, "run", monitor);
    logger.info("Launched: " + launch);

    if (chosen.getName().equals(projectName)) {
      return Services.getMessage("project.executed.exact", projectName);
    } else {
      return Services.getMessage("project.executed", chosen.getName(), projectName);
    }
  }

  ILaunchConfiguration findConfiguration(
      final Iterable<ILaunchConfiguration> configs,
      final String name)
  {
    for (final ILaunchConfiguration config : configs) {
      if (config.getName().startsWith(name)) {
        return config;
      }
    }

    return null;
  }

  String getGroupMode(final String groupId)
  {
    final ILaunchGroup[] groups = DebugUITools.getLaunchGroups();
    for (final ILaunchGroup group : groups) {
      if (groupId.equals(group.getIdentifier())) {
        return group.getMode();
      }
    }

    return null;
  }

  IProject getProject(ILaunchConfiguration config)
    throws Exception
  {

    final IResource[] resources = config.getMappedResources();
    if (resources == null) {
      return null;
    }

    if (resources.length == 0) {
      return null;
    }

    return resources[0].getProject();
  }
}
