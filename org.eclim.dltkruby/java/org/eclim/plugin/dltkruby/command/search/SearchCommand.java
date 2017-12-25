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
package org.eclim.plugin.dltkruby.command.search;

import org.eclim.annotation.Command;

import org.eclim.plugin.core.project.ProjectNatureFactory;

/**
 * Command for ruby project search requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "ruby_search",
  options =
    "OPTIONAL n project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL l length ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL p pattern ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL x context ARG," +
    "OPTIONAL s scope ARG," +
    "OPTIONAL i case_insensitive NOARG"
)
public class SearchCommand
  extends org.eclim.plugin.dltk.command.search.SearchCommand
{
  @Override
  protected String getNature()
  {
    return ProjectNatureFactory.getNatureForAlias("ruby");
  }

  @Override
  protected String getElementSeparator()
  {
    return " : ";
  }
}
