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

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.core.search.SearchMatch;

/**
 * Extension to eclim SearchRequestor that ensures
 *  the found class declares the given method
 *
 * @author Daniel Leong
 */
public class ImplementorSearchRequestor
  extends SearchRequestor
{

  private final IMethod target;

  public ImplementorSearchRequestor(final IMethod target)
  {
    this.target = target;
    if (target == null) {
      throw new IllegalArgumentException(
              "You must use a regular SearchRequestor if you have no target");
    }
  }

  /**
   * {@inheritDoc}
   */
  public void acceptSearchMatch(SearchMatch match)
    throws CoreException
  {
    if (matchDeclaresTarget(match)) {
      super.acceptSearchMatch(match);
    }
  }

  private boolean matchDeclaresTarget(final SearchMatch match)
    throws CoreException
  {
    if (!(match.getElement() instanceof IType)) {
      return false;
    }

    final IType type = (IType) match.getElement();
    IMethod[] found = type.findMethods(target);
    return found != null && found.length > 0;
  }

}
