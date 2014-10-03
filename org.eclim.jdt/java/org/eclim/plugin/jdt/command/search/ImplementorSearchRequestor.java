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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

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
  private final List<IJavaElement> candidateParents =
    new ArrayList<IJavaElement>();

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
  @Override
  public void acceptSearchMatch(SearchMatch match)
    throws CoreException
  {
    final IMethod declared = findTargetDeclaration(match);
    if (declared != null) {
      
      try {
        // attempt to fix the Match to point to the declaration
        int declaration = JavaUtils.getElementOffset(declared);
        match.setOffset(declaration);
      } catch (final Exception e) {
        // oh well
      }

      super.acceptSearchMatch(match);
    } else {
      candidateParents.add((IJavaElement) match.getElement());
    }
  }

  /**
   * @return A SearchPattern if there are still more
   *  candidates, else null
   */
  public SearchPattern getNextSearch()
  {
    if (candidateParents.isEmpty()) {
      return null;
    }

    Iterator<IJavaElement> iter = candidateParents.iterator();
    SearchPattern result = createSearchFor(iter.next());
    iter.remove();

    while (iter.hasNext()) {
      result = SearchPattern.createOrPattern(result,
          createSearchFor(iter.next()));

      iter.remove();
    }

    return result;
  }

  public IMethod getTarget()
  {
    return target;
  }

  private SearchPattern createSearchFor(final IJavaElement parent)
  {
    return SearchPattern.createPattern(parent, 
        IJavaSearchConstants.SUPERTYPE_TYPE_REFERENCE);
  }

  private IMethod findTargetDeclaration(final SearchMatch match)
    throws CoreException
  {
    if (!(match.getElement() instanceof IType)) {
      return null;
    }

    final IType type = (IType) match.getElement();
    IMethod[] found = type.findMethods(target);
    if (found != null && found.length > 0) {
      // just the first, I guess
      return found[0];
    }

    return null;
  }

}
