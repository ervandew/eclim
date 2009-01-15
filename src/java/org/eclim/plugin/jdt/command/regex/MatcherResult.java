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
package org.eclim.plugin.jdt.command.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents matched result.
 *
 * @author Eric Van Dewoestine
 */
public class MatcherResult
{
  private int startLine;
  private int startColumn;
  private int endLine;
  private int endColumn;

  private ArrayList<MatcherResult> groups = new ArrayList<MatcherResult>();

  public int getStartLine()
  {
    return startLine;
  }

  public void setStartLine(int startLine)
  {
    this.startLine = startLine;
  }

  public int getStartColumn()
  {
    return startColumn;
  }

  public void setStartColumn(int startColumn)
  {
    this.startColumn = startColumn;
  }

  public int getEndLine()
  {
    return endLine;
  }

  public void setEndLine(int endLine)
  {
    this.endLine = endLine;
  }

  public int getEndColumn()
  {
    return endColumn;
  }

  public void setEndColumn(int endColumn)
  {
    this.endColumn = endColumn;
  }

  public List<MatcherResult> getGroupMatches()
  {
    return groups;
  }

  public void addGroupMatch(MatcherResult match)
  {
    this.groups.add(match);
  }
}
