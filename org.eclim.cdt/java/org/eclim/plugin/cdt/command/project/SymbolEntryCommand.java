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

import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

/**
 * Command to add/delete symbols from the specified project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "c_project_symbol",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED a action ARG," +
    "REQUIRED l lang ARG," +
    "REQUIRED n name ARG," +
    "OPTIONAL v value ARG"
)
public class SymbolEntryCommand
  extends AbstractSettingEntryCommand
{
  @Override
  protected ICLanguageSettingEntry createEntry(CommandLine commandLine)
  {
    String name = commandLine.getValue(Options.NAME_OPTION);
    String value = commandLine.getValue(Options.VALUE_OPTION);
    return new CMacroEntry(name, value, 0);
  }
}
