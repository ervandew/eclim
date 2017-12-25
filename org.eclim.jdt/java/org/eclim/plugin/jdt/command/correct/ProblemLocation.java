/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
import org.eclipse.jdt.core.dom.NodeFinder;

import org.eclipse.jdt.ui.text.java.IProblemLocation;

/**
 * Implementation of IProblemLocation.
 *
 * @author Eric Van Dewoestine
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
   *
   * @param problem An IProblem instance.
   */
  public ProblemLocation(IProblem problem)
  {
    id = problem.getID();
    offset = problem.getSourceStart();
    length = (problem.getSourceEnd() + 1) - offset;
    arguments = problem.getArguments();
    error = problem.isError();
  }

  @Override
  public ASTNode getCoveredNode(CompilationUnit astRoot)
  {
    NodeFinder finder = new NodeFinder(astRoot, offset, length);
    return finder.getCoveredNode();
  }

  @Override
  public ASTNode getCoveringNode(CompilationUnit astRoot)
  {
    NodeFinder finder = new NodeFinder(astRoot, offset, length);
    return finder.getCoveringNode();
  }

  @Override
  public int getLength()
  {
    return length;
  }

  @Override
  public int getOffset()
  {
    return offset;
  }

  @Override
  public String[] getProblemArguments()
  {
    return arguments;
  }

  @Override
  public int getProblemId()
  {
    return id;
  }

  @Override
  public boolean isError()
  {
    return error;
  }

  @Override
  public String getMarkerType()
  {
    return IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER;
  }
}
