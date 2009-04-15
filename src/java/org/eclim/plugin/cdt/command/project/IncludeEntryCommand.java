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
package org.eclim.plugin.cdt.command.project;

import java.util.Iterator;
import java.util.List;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.Path;

/**
 * Command to add/delete an include entry to/from the specified project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "c_project_include",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED a action ARG," +
    "REQUIRED d dir ARG," +
    "REQUIRED l lang ARG"
)
public class IncludeEntryCommand
  extends AbstractCommand
{
  private static final String ADD = "add";
  private static final String DELETE = "delete";

  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
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
   * Add a new include entry.
   *
   * @param commandLine The command line args.
   * @return The result.
   */
  private String add(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String lang = commandLine.getValue(Options.LANG_OPTION);
    String dir = commandLine.getValue(Options.DIR_OPTION);
    dir = FileUtils.removeTrailingSlash(dir);

    IProject project = ProjectUtils.getProject(projectName);
    ICProjectDescription desc =
      CCorePlugin.getDefault().getProjectDescription(project, true);
    ICConfigurationDescription[] configs = desc.getConfigurations();
    ICIncludePathEntry include = new CIncludePathEntry(
        new Path(dir),
        CIncludePathEntry.VALUE_WORKSPACE_PATH);

    for(ICConfigurationDescription config : configs){
      ICFolderDescription fdesc = config.getRootFolderDescription();
      ICLanguageSetting[] ls = fdesc.getLanguageSettings();
      for (ICLanguageSetting l : ls){
        String name = StringUtils.split(l.getName())[0].toLowerCase();
        if (name.equals(lang)){
          List<ICLanguageSettingEntry> lst =
            l.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
          lst.add(include);
          l.setSettingEntries(ICSettingEntry.INCLUDE_PATH, lst);
        }
      }
    }

    CCorePlugin.getDefault().setProjectDescription(project, desc);

    return Services.getMessage("include.entry.added");
  }

  /**
   * Delete a include entry.
   *
   * @param commandLine The command line args.
   * @return The result.
   */
  private String delete(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String lang = commandLine.getValue(Options.LANG_OPTION);
    String dir = commandLine.getValue(Options.DIR_OPTION);
    dir = FileUtils.removeTrailingSlash(dir);
    IProject project = ProjectUtils.getProject(projectName);
    ICProjectDescription desc =
      CCorePlugin.getDefault().getProjectDescription(project, true);
    ICConfigurationDescription[] configs = desc.getConfigurations();
    boolean deleted = false;
    for(ICConfigurationDescription config : configs){
      ICFolderDescription fdesc = config.getRootFolderDescription();
      ICLanguageSetting[] ls = fdesc.getLanguageSettings();
      for (ICLanguageSetting l : ls){
        String name = StringUtils.split(l.getName())[0].toLowerCase();
        if (name.equals(lang)){
          List<ICLanguageSettingEntry> lst =
            l.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
          Iterator<ICLanguageSettingEntry> iterator = lst.iterator();
          while (iterator.hasNext()){
            if (iterator.next().getName().equals(dir)){
              iterator.remove();
            }
          }
          l.setSettingEntries(ICSettingEntry.INCLUDE_PATH, lst);
          deleted = true;
        }
      }
    }

    if(deleted){
      CCorePlugin.getDefault().setProjectDescription(project, desc);
      return Services.getMessage("include.entry.deleted");
    }
    return Services.getMessage("include.entry.not.found", dir);
  }
}
