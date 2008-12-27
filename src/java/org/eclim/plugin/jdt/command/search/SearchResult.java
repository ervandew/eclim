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
package org.eclim.plugin.jdt.command.search;

import org.eclim.util.file.Position;

/**
 * Extension to Position for search results.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SearchResult
  extends Position
{
  private String archive;
  private String element;

  /**
   * Constructs a new instance.
   */
  public SearchResult (
      String archive, String element, String file, int offset, int length)
  {
    super(file, offset, length);
    this.archive = archive;
    this.element = element;
  }

  /**
   * Get archive.
   *
   * @return archive as String.
   */
  public String getArchive()
  {
    return this.archive;
  }

  /**
   * Get element.
   *
   * @return element as String.
   */
  public String getElement()
  {
    return this.element;
  }
}
