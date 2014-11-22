/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclim.plugin.jdt.util.IJavaElementComparator;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jdt.core.search.SearchMatch;

/**
 * Extension to SearchRequestor that adds getMatches().
 *
 * @author Eric Van Dewoestine
 */
public class SearchRequestor
  extends org.eclipse.jdt.core.search.SearchRequestor
{
  private static final SearchMatchComparator MATCH_COMPARATOR =
    new SearchMatchComparator();

  private ArrayList<SearchMatch> matches = new ArrayList<SearchMatch>();

  @Override
  public void acceptSearchMatch(SearchMatch match)
    throws CoreException
  {
    if(match.getAccuracy() == SearchMatch.A_ACCURATE){
      matches.add(match);
    }
  }

  /**
   * Gets a list of all the matches found.
   *
   * @return List of SearchMatch.
   */
  public List<SearchMatch> getMatches()
  {
    Collections.sort(matches, MATCH_COMPARATOR);
    return matches;
  }

  /**
   * Comparator for search matches.
   */
  public static class SearchMatchComparator
    implements Comparator<SearchMatch>
  {
    private static final IJavaElementComparator ELEMENT_COMPATATOR =
      new IJavaElementComparator();

    @Override
    public int compare(SearchMatch o1, SearchMatch o2)
    {
      return ELEMENT_COMPATATOR.compare(
          (IJavaElement)o1.getElement(), (IJavaElement)o2.getElement());
    }

    @Override
    public boolean equals(Object obj)
    {
      if(obj instanceof SearchMatchComparator){
        return true;
      }
      return false;
    }
  }
}
