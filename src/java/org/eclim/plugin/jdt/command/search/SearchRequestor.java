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
package org.eclim.plugin.jdt.command.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;

import org.eclim.plugin.jdt.util.IJavaElementComparator;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.search.SearchMatch;

/**
 * Extension to SearchRequestor that adds getMatches().
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SearchRequestor
  extends org.eclipse.jdt.core.search.SearchRequestor
{
  private List matches = new ArrayList();

  /**
   * {@inheritDoc}
   */
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
  public List getMatches ()
  {
    Collections.sort(matches,
        new BeanComparator("element", new IJavaElementComparator()));
    return matches;
  }
}
