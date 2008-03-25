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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.plugin.jdt.command.search.SearchCommand;
import org.eclim.plugin.jdt.command.search.SearchResult;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Command to retrieve possible imports for a given pattern.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ImportCommand
  extends SearchCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    ArrayList<ImportResult> results = new ArrayList<ImportResult>();
    String project = _commandLine.getValue(Options.NAME_OPTION);
    String pat = _commandLine.getValue(Options.PATTERN_OPTION);

    SearchPattern pattern =
      SearchPattern.createPattern(pat,
          IJavaSearchConstants.TYPE,
          IJavaSearchConstants.DECLARATIONS,
          SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
    IJavaProject javaProject = JavaUtils.getJavaProject(project);
    IJavaSearchScope scope =
      SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject});
    List<SearchMatch> matches = super.search(pattern, scope);
    for(SearchMatch match : matches){
      if(match.getAccuracy() == SearchMatch.A_ACCURATE){
        SearchResult result = createSearchResult(match);
        IType element = (IType)match.getElement();
        if(Flags.isPublic(element.getFlags())){
          ImportResult ir = new ImportResult(
                result.getElement(), element.getElementType());
          if(!results.contains(ir)){
            results.add(ir);
          }
        }
      }
    }
    return ImportFilter.instance.filter(_commandLine, results);
  }
}
