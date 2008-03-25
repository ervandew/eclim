/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.correct;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Holds information about a correction proposal.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCorrectResult
{
  private int index;
  private IProblem problem;
  private String description;
  private String preview;

  /**
   * Default constructor.
   *
   * @param _index The index of this result in relation to other proposals.
   * @param _problem The associated problem.
   * @param _description The description of the proposed correction.
   * @param _preview A preview of the code after applying the correction.
   */
  public CodeCorrectResult (
      int _index, IProblem _problem, String _description, String _preview)
  {
    index = _index;
    problem = _problem;
    description = _description;
    preview = _preview;
  }

  /**
   * Gets the index of this result.
   *
   * @return The index.
   */
  public int getIndex ()
  {
    return index;
  }

  /**
   * Gets associate problem.
   *
   * @return The problem.
   */
  public IProblem getProblem ()
  {
    return problem;
  }

  /**
   * Gets a description of the proposed correction.
   *
   * @return The description.
   */
  public String getDescription ()
  {
    return description;
  }

  /**
   * Gets a code snip-it preview of the result of applying the proposed
   * correction.
   *
   * @return A preview.
   */
  public String getPreview ()
  {
    return preview;
  }
}
