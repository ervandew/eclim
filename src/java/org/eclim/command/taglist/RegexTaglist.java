/**
 * Copyright (c) 2004 - 2006
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
package org.eclim.command.taglist;

/**
 * Handles processing tags from a file using a series of regex patterns.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class RegexTaglist
{
  private String file;

  /**
   * Constructs a new instance.
   *
   * @param _file The file to be processed.
   */
  public RegexTaglist (String _file)
  {
    file = _file;
  }

  /**
   * Adds the supplied tag matching pattern.
   *
   * @param _name The name of the tag.
   * @param _kind Character kind for the tag.
   * @param _pattern Regex pattern for matching this tag.
   * @param _replace The replacement that represents the tag value.
   */
  public void addPattern (
      String _name, char _kind, String _pattern, String _replace)
  {
System.out.println("#### RegexTaglist.addPattern");
System.out.println("    #### " + _name);
System.out.println("    #### " + _kind);
System.out.println("    #### " + _pattern);
System.out.println("    #### " + _replace);
  }

  /**
   * Executes the processing of the specified tags.
   *
   * @return Array of TagResult.
   */
  public TagResult[] execute ()
  {
System.out.println("#### RegexTaglist.execute");
    return null;
  }
}
