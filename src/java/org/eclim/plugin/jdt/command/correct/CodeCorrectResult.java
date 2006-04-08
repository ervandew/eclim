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
package org.eclim.plugin.jdt.command.correct;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Holds information about a correction proposal.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
