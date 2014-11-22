/**
 * Copyright (C) 2012-2014 Tyler Dodge, Daniel Leong, Eric Van Dewoestine
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

import org.eclim.plugin.core.command.project.EclimLaunchManager.OutputHandler;

import org.eclim.plugin.core.util.ProjectUtils;
import org.eclim.plugin.core.util.VimClient;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.runtime.jobs.Job;

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
    "OPTIONAL v vim_instance_name ARG," + // not required for List
    "OPTIONAL x vim_executable ARG," + // not required for List
    "OPTIONAL l list NOARG," +
    "OPTIONAL d debug NOARG," +
    "OPTIONAL c force NOARG," +
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
    final boolean force = commandLine.hasOption(Options.FORCE_OPTION);
    final String configName = commandLine.getValue(Options.NAME_OPTION);
    final String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    final String vimInstanceId = commandLine.getValue(Options.VIM_INSTANCE_OPTION);
    final String vimExecutable = commandLine.getValue(Options.VIM_EXECUTABLE_OPTION);
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
    } else if (!projectConfigs.isEmpty()) {
      // just get the first
      chosen = projectConfigs.get(0);
    } else {
      return Services.getMessage("project.execute.noconfig", projectName);
    }

    if (chosen == null) {
      return Services.getMessage("project.execute.invalid", projectName);
    }

    // do we need to force?
    if (EclimLaunchManager.isRunning(chosen.getName()) && !force) {
      EclimLaunchManager.terminate(chosen.getName());
    }

    // prepare the progress monitor
    final String completionMessage = chosen.getName().equals(projectName) ?
      Services.getMessage("project.executed.exact", projectName) :
      Services.getMessage("project.executed", chosen.getName(), projectName);

    final IProgressMonitor monitor;
    final OutputHandler handler;
    final boolean hasVim = vimInstanceId != null && !"".equals(vimInstanceId);
    if (hasVim) {
      final VimClient client = new VimClient(vimExecutable, vimInstanceId);
      monitor = new VimUpdatingProgressMonitor(client, completionMessage);
      handler = new VimOutputHandler(client, chosen.getName());
    } else {
      monitor = new NullUpdatingProgressMonitor(completionMessage);
      handler = new NullOutputHandler();
    }

    final LaunchJob launchJob = new LaunchJob(chosen, monitor, handler);
    if (!(monitor instanceof NullUpdatingProgressMonitor)) {
      // launch after a short delay; this is required
      //  so vim isn't blocked waiting on the result
      launchJob.schedule();
      return null;
    } else {
      // just run interactively; there's no progress to post
      try {
        IStatus status = launchJob.run(null);
        if (status.getSeverity() != IStatus.OK) {
          Throwable exception = status.getException();
          while (exception.getCause() != null){
            exception = exception.getCause();
          }
          return Services.getMessage("project.execute.fail",
              projectName,
              exception.getMessage());
        }
        return completionMessage;
      } catch (Throwable e) {
        logger.error("Unexpected error while launching", e);
        return Services.getMessage("project.execute.fail",
            projectName, e.getMessage());
      }
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

  abstract static class UpdatingProgressMonitor implements IProgressMonitor
  {

    final String completionMessage;
    double totalProgress = 0.0;
    String baseTask;
    String currentTask;

    public UpdatingProgressMonitor(final String completionMessage)
    {
      this.completionMessage = completionMessage;
    }

    @Override
    public void beginTask(String name, int totalWork)
    {
        logger.info("Begin: " + name + " / " + totalWork);
        baseTask = name;
        currentTask = baseTask;
    }

    @Override
    public void done()
    {
      totalProgress = 1;

      try {
        sendMessage(completionMessage);
      } catch (Exception e) {
        logger.error("Couldn't send message", e);
      }
    }

    @Override
    public void internalWorked(double work)
    {
        logger.info("Internal..." + work);
        totalProgress += work;
        sendProgress();
    }

    @Override
    public boolean isCanceled()
    {
        return false;
    }

    @Override
    public void setCanceled(boolean value)
    {
      // nop
    }

    @Override
    public void setTaskName(String name)
    {
      // nop
    }

    @Override
    public void subTask(String name)
    {
      if (name == null || "".equals(name)) {
        return; // don't bother
      }

      logger.info("subtask: " + name);
      if (baseTask != null && !"".equals(baseTask)) {
        currentTask = baseTask + " - " + name;
      } else {
        currentTask = name;
      }

      sendProgress();
    }

    @Override
    public void worked(int work)
    {
      // nop
    }

    void sendProgress()
    {
      try {
        sendProgress(Math.min(1, totalProgress), currentTask);
      } catch (final Exception e) {
        // no worries
        logger.error("Couldn't send progress", e);
      }
    }

    public abstract void sendMessage(String message)
      throws Exception;

    public abstract void sendProgress(double percent, String label)
      throws Exception;
  }

  static class NullUpdatingProgressMonitor extends UpdatingProgressMonitor
  {

    NullUpdatingProgressMonitor(final String completionMessage)
    {
      super(completionMessage);
    }

    @Override
    public void sendMessage(String message)
    {
      logger.info("Message: {}", message);
    }

    @Override
    public void sendProgress(double percent, String label)
    {
      logger.info("Progress({}): {}", percent, label);
    }

  }

  static class VimUpdatingProgressMonitor extends UpdatingProgressMonitor
  {

    final VimClient client;

    public VimUpdatingProgressMonitor(final VimClient client,
        final String completionMessage)
    {
      super(completionMessage);
      this.client = client;
    }

    @Override
    public void sendMessage(String message)
      throws Exception
    {
      client.remoteFunctionExpr("eclim#util#Echo", message);
    }

    @Override
    public void sendProgress(final double percent, final String label)
      throws Exception
    {
      client.remoteFunctionExpr("eclim#project#run#onLaunchProgress",
          String.valueOf(percent), label);
    }

  }

  static class NullOutputHandler implements OutputHandler
  {
    @Override public void prepare(String launchId)
      throws Exception
    {
      // NB client-specific errors can be returned here in the future,
      //  possibly via constructor
      throw new Exception(
          "Vim must be running in server mode:\n" +
          "Example: vim --servername <name>");
    }

    @Override public void sendErr(String line) {}
    @Override public void sendOut(String line) {}
    @Override public void sendTerminated() {}
  }

  static class VimOutputHandler implements OutputHandler
  {

    static final class PendingOutput
    {
      final String type, line;
      PendingOutput(String type, String line)
      {
        this.type = type;
        this.line = line;
      }
    }

    final VimClient client;
    final String configName;
    final ArrayList<PendingOutput> pendingOutput = new ArrayList<PendingOutput>();

    String bufNo;

    public VimOutputHandler(VimClient client, String configName)
    {
      this.client = client;
      this.configName = configName;
    }

    @Override
    public void prepare(String launchId)
      throws Exception
    {
      final String rawResult = client.remoteFunctionExpr(
          "eclim#project#run#onPrepareOutput", configName, launchId);
      if (rawResult == null) {
        throw new Exception("Timeout preparing output buffer");
      }

      bufNo = rawResult.trim();

      for (PendingOutput output : pendingOutput) {
        sendLine(output.type, output.line);
      }
      pendingOutput.clear();
    }

    @Override
    public void sendErr(String line)
    {
      sendLine("err", line);
    }

    @Override
    public void sendOut(String line)
    {
      sendLine("out", line);
    }

    @Override
    public void sendTerminated()
    {
      sendLine("terminated", "");
    }

    void sendLine(String type, String line)
    {
      if (bufNo == null) {
        // not prepared yet; queue for later
        pendingOutput.add(new PendingOutput(type, line));
        return;
      }
      try {
        final String clean = line.trim()
          .replaceAll("\n", "\\\\r")
          .replaceAll("\t", "    ");

        // functionExpr is safer, in case they're in input mode
        client.remoteFunctionExpr("eclim#project#run#onOutput", bufNo, type, clean);
      } catch (Exception e) {
        // no worries
      }
    }
  }

  static class LaunchJob extends Job
  {
    final ILaunchConfiguration config;
    final IProgressMonitor monitor;
    final OutputHandler output;

    public LaunchJob(ILaunchConfiguration config, IProgressMonitor monitor,
        final OutputHandler output)
    {
      super("Eclim Launch");

      this.config = config;
      this.monitor = monitor;
      this.output = output;
    }

    @Override
    public IStatus run(IProgressMonitor ignore)
    {
      logger.info("Launching: " + config);
      try {

        ILaunch launch = DebugUITools.buildAndLaunch(config, "run", monitor);
        if (launch != null) {
          EclimLaunchManager.manage(launch, output);
        }
        logger.info("Launched: " + launch);
      } catch (IllegalArgumentException e) {
        logger.error("Launch terminated; async not supported", e);
        return new Status(Status.ERROR, "eclim", "Unable to capture async output", e);
      } catch (Exception e) {
        logger.error("Launch terminated; Unexpected Error", e);
        return new Status(Status.ERROR, "eclim", "Error while launching", e);
      }
      return Status.OK_STATUS;
    }
  }
}
