/**
 * Copyright (c) 2005 - 2008
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

import org.eclipse.jdt.core.IJavaModelMarker;

import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jdt.internal.corext.dom.NodeFinder;

import org.eclipse.jdt.ui.text.java.IProblemLocation;

/**
 * Implementation of IProblemLocation.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ProblemLocation
  implements IProblemLocation
{
  private int id;
  private int offset;
  private int length;
  private String[] arguments;
  private boolean error;

  /**
   * Default Constructor.
   */
  public ProblemLocation (IProblem _problem)
  {
    id = _problem.getID();
    offset = _problem.getSourceStart();
    length = _problem.getSourceEnd() - offset;
    arguments = _problem.getArguments();
    error = _problem.isError();
  }

  /**
   * {@inheritDoc}
   */
  public ASTNode getCoveredNode (CompilationUnit _astRoot)
  {
    NodeFinder finder= new NodeFinder(offset, length);
    _astRoot.accept(finder);
    return finder.getCoveredNode();
  }

  /**
   * {@inheritDoc}
   */
  public ASTNode getCoveringNode (CompilationUnit _astRoot)
  {
    NodeFinder finder= new NodeFinder(offset, length);
    _astRoot.accept(finder);
    return finder.getCoveringNode();
  }

  /**
   * {@inheritDoc}
   */
  public int getLength ()
  {
    return length;
  }

  /**
   * {@inheritDoc}
   */
  public int getOffset ()
  {
    return offset;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getProblemArguments ()
  {
    return arguments;
  }

  /**
   * {@inheritDoc}
   */
  public int getProblemId ()
  {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isError ()
  {
    return error;
  }

  /**
   * {@inheritDoc}
   */
  public String getMarkerType ()
  {
    return IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER;
  }
}
