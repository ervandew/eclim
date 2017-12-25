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
package org.eclim.plugin.pdt.command.search;

import org.eclim.annotation.Command;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;

//import org.eclipse.jface.text.Region;

//import org.eclipse.php.internal.core.typeinference.PHPModelUtils;

/**
 * Command for php project search requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "php_search",
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
    return ProjectNatureFactory.getNatureForAlias("php");
  }

  @Override
  protected String getElementSeparator()
  {
    return " -> ";
  }

  @Override
  protected String getElementTypeName()
  {
    return "class";
  }

  @Override
  protected IModelElement[] getElements(ISourceModule src, int offset, int length)
  {
    IModelElement[] elements = super.getElements(src, offset, length);
    // disabled for now to retain compatibility with eclipse 4.2
    /*if (elements == null || elements.length == 0){
      elements = PHPModelUtils.getTypeInString(src, new Region(offset, length));
    }*/
    return elements;
  }
}
