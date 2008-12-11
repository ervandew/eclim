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
package org.eclim.plugin.pdt.command.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.dltk.core.search.SearchMatch;

/**
 * SearchRequestor used to collect search results.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class SearchRequestor
  extends org.eclipse.dltk.core.search.SearchRequestor
{
  private ArrayList<SearchMatch> matches = new ArrayList<SearchMatch>();

  /**
   * {@inheritDoc}
   * @see org.eclipse.dltk.core.search.SearchRequestor#acceptSearchMatch(SearchMatch)
   */
  @Override
  public void acceptSearchMatch (SearchMatch _match)
    throws CoreException
  {
    if(_match.getAccuracy() == SearchMatch.A_ACCURATE){
      matches.add(_match);
    }
  }

  /**
   * Gets a list of all the matches found.
   *
   * @return List of SearchMatch.
   */
  public List<SearchMatch> getMatches ()
  {
    //Collections.sort(matches, MATCH_COMPARATOR);
    return matches;
  }
}
