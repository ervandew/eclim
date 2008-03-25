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
