/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.plugin.jdt.command.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents matched result.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class MatcherResult
{
  private int startLine;
  private int startColumn;
  private int endLine;
  private int endColumn;

  private ArrayList<MatcherResult> groups = new ArrayList<MatcherResult>();

  public int getStartLine ()
  {
    return startLine;
  }

  public void setStartLine (int _startLine)
  {
    startLine = _startLine;
  }

  public int getStartColumn ()
  {
    return startColumn;
  }

  public void setStartColumn (int _startColumn)
  {
    startColumn = _startColumn;
  }

  public int getEndLine ()
  {
    return endLine;
  }

  public void setEndLine (int _endLine)
  {
    endLine = _endLine;
  }

  public int getEndColumn ()
  {
    return endColumn;
  }

  public void setEndColumn (int _endColumn)
  {
    endColumn = _endColumn;
  }

  public List<MatcherResult> getGroupMatches ()
  {
    return groups;
  }

  public void addGroupMatch (MatcherResult _match)
  {
    groups.add(_match);
  }
}
