/**
 * Copyright (C) 2014  Eric Van Dewoestine
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

import org.eclipse.jdt.core.search.SearchMatch;

import org.eclipse.jdt.internal.corext.util.JdtFlags;

/**
 * SearchRequestor used to accept only method implemenations.
 *
 * See org.eclipse.jdt.internal.ui.javaeditor.JavaElementImplementationHyperlink
 * for other possible ways in which implementors may be searched for.
 *
 * @author Eric Van Dewoestine
 */
public class ImplementorsSearchRequestor
  extends SearchRequestor
{
  @Override
  public void acceptSearchMatch(SearchMatch match)
    throws CoreException
  {
    if(match.getAccuracy() == SearchMatch.A_ACCURATE){
      Object element = match.getElement();
      if (element instanceof IMethod){
        IMethod method = (IMethod)element;
        if (!JdtFlags.isAbstract(method)){
          super.acceptSearchMatch(match);
        }
      }
    }
  }
}
