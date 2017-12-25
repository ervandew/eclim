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
package org.eclim.plugin.cdt.command.project;

import java.util.ArrayList;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Command to add/delete a source entry to/from the specified project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "c_project_src",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED a action ARG," +
    "REQUIRED d dir ARG," +
    "OPTIONAL e excludes ARG"
)
public class SourceEntryCommand
  extends AbstractCommand
{
  private static final String ADD = "add";
  private static final String DELETE = "delete";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String action = commandLine.getValue(Options.ACTION_OPTION);

    if(ADD.equals(action)){
      return add(commandLine);
    }
    if(DELETE.equals(action)){
      return delete(commandLine);
    }
    throw new RuntimeException(Services.getMessage("unknown.action", action));
  }

  /**
   * Add a new source entry.
   *
   * @param commandLine The command line args.
   * @return The result.
   */
  private String add(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String dir = commandLine.getValue(Options.DIR_OPTION);
    String excludes = commandLine.getValue(Options.EXCLUDES_OPTION);
    dir = FileUtils.removeTrailingSlash(dir);

    IProject project = ProjectUtils.getProject(projectName);
    ICProjectDescription desc =
      CCorePlugin.getDefault().getProjectDescription(project, true);
    ICConfigurationDescription[] configs = desc.getConfigurations();

    ArrayList<IPath> excludePaths = new ArrayList<IPath>();
    if(excludes != null){
      for(String exclude : StringUtils.split(excludes, ',')){
        excludePaths.add(new Path(exclude));
      }
    }
    ICSourceEntry source = new CSourceEntry(
        new Path(dir),
        excludePaths.toArray(new IPath[excludePaths.size()]),
        CSourceEntry.VALUE_WORKSPACE_PATH);

    for(ICConfigurationDescription config : configs){
      ICSourceEntry[] sources = config.getSourceEntries();
      ArrayList<ICSourceEntry> keep = new ArrayList<ICSourceEntry>();
      for(ICSourceEntry entry : sources){
        String name = entry.getFullPath().removeFirstSegments(1).toString();
        if(!name.equals(dir)){
          keep.add(entry);
        }
      }
      keep.add(source);
      config.setSourceEntries(keep.toArray(new ICSourceEntry[keep.size()]));
    }

    CCorePlugin.getDefault().setProjectDescription(project, desc);

    return Services.getMessage("entry.added");
  }

  /**
   * Delete a source entry.
   *
   * @param commandLine The command line args.
   * @return The result.
   */
  private String delete(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String dir = commandLine.getValue(Options.DIR_OPTION);
    dir = FileUtils.removeTrailingSlash(dir);

    IProject project = ProjectUtils.getProject(projectName);
    ICProjectDescription desc =
      CCorePlugin.getDefault().getProjectDescription(project, true);
    ICConfigurationDescription[] configs = desc.getConfigurations();
    boolean deleted = false;
    for(ICConfigurationDescription config : configs){
      ICSourceEntry[] sources = config.getSourceEntries();
      ArrayList<ICSourceEntry> keep = new ArrayList<ICSourceEntry>();
      for(ICSourceEntry entry : sources){
        String name = entry.getFullPath().removeFirstSegments(1).toString();
        if(!name.equals(dir)){
          keep.add(entry);
        }
      }
      if(sources.length != keep.size()){
        deleted = true;
        config.setSourceEntries(keep.toArray(new ICSourceEntry[keep.size()]));
      }
    }

    if(deleted){
      CCorePlugin.getDefault().setProjectDescription(project, desc);
      return Services.getMessage("entry.deleted");
    }
    return Services.getMessage("entry.not.found", dir);
  }
}
