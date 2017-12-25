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

import java.util.Iterator;
import java.util.List;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.StringUtils;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;

/**
 * Abstract super class for commands which add or delete c setting entries.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractSettingEntryCommand
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
   * Add a new include entry.
   *
   * @param commandLine The command line args.
   * @return The result.
   */
  protected String add(CommandLine commandLine)
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String lang = commandLine.getValue(Options.LANG_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    ICProjectDescription desc =
      CCorePlugin.getDefault().getProjectDescription(project, true);
    ICConfigurationDescription[] configs = desc.getConfigurations();
    ICLanguageSettingEntry entry = createEntry(commandLine);

    for(ICConfigurationDescription config : configs){
      ICFolderDescription fdesc = config.getRootFolderDescription();
      ICLanguageSetting[] ls = fdesc.getLanguageSettings();
      for (ICLanguageSetting l : ls){
        String name = StringUtils.split(l.getName())[0].toLowerCase();
        if (name.equals(lang)){
          List<ICLanguageSettingEntry> lst =
            l.getSettingEntriesList(entry.getKind());
          lst.add(entry);
          l.setSettingEntries(entry.getKind(), lst);
        }
      }
    }

    try{
      CCorePlugin.getDefault().setProjectDescription(project, desc);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    return Services.getMessage("entry.added");
  }

  /**
   * Delete a include entry.
   *
   * @param commandLine The command line args.
   * @return The result.
   */
  protected String delete(CommandLine commandLine)
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String lang = commandLine.getValue(Options.LANG_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    ICProjectDescription desc =
      CCorePlugin.getDefault().getProjectDescription(project, true);
    ICConfigurationDescription[] configs = desc.getConfigurations();
    ICLanguageSettingEntry entry = createEntry(commandLine);

    boolean deleted = false;
    for(ICConfigurationDescription config : configs){
      ICFolderDescription fdesc = config.getRootFolderDescription();
      ICLanguageSetting[] ls = fdesc.getLanguageSettings();
      for (ICLanguageSetting l : ls){
        String name = StringUtils.split(l.getName())[0].toLowerCase();
        if (name.equals(lang)){
          List<ICLanguageSettingEntry> lst =
            l.getSettingEntriesList(entry.getKind());
          Iterator<ICLanguageSettingEntry> iterator = lst.iterator();
          while (iterator.hasNext()){
            if (iterator.next().getName().equals(entry.getName())){
              iterator.remove();
            }
          }
          l.setSettingEntries(entry.getKind(), lst);
          deleted = true;
        }
      }
    }

    if(deleted){
      try{
        CCorePlugin.getDefault().setProjectDescription(project, desc);
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
      return Services.getMessage("entry.deleted");
    }
    return Services.getMessage("entry.not.found", entry.getName());
  }

  /**
   * Creates the language setting entry.
   *
   * @param commandLine The command line instance.
   * @return The ICLanguageSettingEntry.
   */
  protected abstract ICLanguageSettingEntry createEntry(CommandLine commandLine);
}
