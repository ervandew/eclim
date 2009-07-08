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
package org.eclim.plugin.pdt.command.search;

import java.util.ArrayList;

import org.eclim.annotation.Command;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclipse.dltk.core.IModelElement;

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
  /**
   * {@inheritDoc}
   * @see org.eclim.plugin.dltk.command.search.SearchCommand#getNature()
   */
  @Override
  protected String getNature()
  {
    return ProjectNatureFactory.getNatureForAlias("php");
  }

  /**
   * {@inheritDoc}
   * @see org.eclim.plugin.dltk.command.search.SearchCommand#getMessage(Object)
   */
  @Override
  protected String getMessage(Object e)
  {
    IModelElement element = (IModelElement)e;
    ArrayList<IModelElement> lineage = new ArrayList<IModelElement>();
    while (element.getElementType() != IModelElement.SOURCE_MODULE){
      lineage.add(0, element);
      element = element.getParent();
    }

    StringBuffer fullyQualified = new StringBuffer();
    for(IModelElement el : lineage){
      if (fullyQualified.length() != 0){
        fullyQualified.append(" -> ");
      }
      if (el.getElementType() == IModelElement.TYPE){
        fullyQualified.append("class ");
      }
      if (el.getElementType() == IModelElement.METHOD){
        fullyQualified.append("function ");
      }
      fullyQualified.append(el.getElementName());
    }

    return fullyQualified.toString();
  }
}
