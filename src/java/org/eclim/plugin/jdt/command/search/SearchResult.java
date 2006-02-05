/**
 * Copyright (c) 2004 - 2005
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.jdt.command.search;

import org.eclim.util.file.Position;

/**
 * Extension to Position for search results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
      String _archive, String _element, String _file, int _offset, int _length)
  {
    super(_file, _offset, _length);
    archive = _archive;
    element = _element;
  }

  /**
   * Get archive.
   *
   * @return archive as String.
   */
  public String getArchive ()
  {
    return this.archive;
  }

  /**
   * Get element.
   *
   * @return element as String.
   */
  public String getElement ()
  {
    return this.element;
  }
}
