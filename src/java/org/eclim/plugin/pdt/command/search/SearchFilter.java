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
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

import org.eclim.util.file.Position;

import org.eclim.util.vim.VimUtils;

import org.eclipse.dltk.core.IModelElement;

import org.eclipse.dltk.core.search.SearchMatch;

public class SearchFilter
  implements OutputFilter<List<SearchMatch>>
{
  public static final SearchFilter instance = new SearchFilter();

  /**
   * {@inheritDoc}
   */
  public String filter(CommandLine commandLine, List<SearchMatch> results)
  {
    try{
      StringBuffer buffer = new StringBuffer();
      if(results != null){
        for(SearchMatch result : results){
          IModelElement element = (IModelElement)result.getElement();
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

          String filename = result.getResource().getLocation().toOSString();
          if (!filename.endsWith(".php")){
            // currently ignoring results that don't have a php file to view.
            continue;
          }
          Position position = new Position(
              filename, result.getOffset(), result.getLength());
          String lineColumn = VimUtils.translateLineColumn(position);
          if(buffer.length() > 0){
            buffer.append('\n');
          }
          buffer.append(position.getFilename())
            .append('|')
            .append(lineColumn)
            .append('|')
            .append(fullyQualified);
        }
      }
      return buffer.toString();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
}
