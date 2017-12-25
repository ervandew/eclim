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

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.file.FileUtils;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

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
    "REQUIRED l lang ARG," +
    "REQUIRED d dir ARG"
)
public class IncludeEntryCommand
  extends AbstractSettingEntryCommand
{
  @Override
  protected ICLanguageSettingEntry createEntry(CommandLine commandLine)
  {
    String dir = commandLine.getValue(Options.DIR_OPTION);
    dir = FileUtils.removeTrailingSlash(dir);
    Path path = new Path(dir);
    if (path.isAbsolute()){
      return new CIncludePathEntry(path, ICSettingEntry.LOCAL);
    }
    return new CIncludePathEntry(path, ICSettingEntry.VALUE_WORKSPACE_PATH);
  }
}
