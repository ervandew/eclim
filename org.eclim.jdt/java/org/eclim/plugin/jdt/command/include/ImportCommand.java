/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
import java.util.Collections;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.command.search.SearchCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.Position;

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
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_import",
  options =
    "REQUIRED n project ARG," +
    "REQUIRED p pattern ARG," +
    "OPTIONAL t type ARG"
)
public class ImportCommand
  extends SearchCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.NAME_OPTION);
    String pattern = commandLine.getValue(Options.PATTERN_OPTION);

    List<String> results =
      findImport(JavaUtils.getJavaProject(project), pattern);

    Collections.sort(results);
    return results;
  }

  protected List<String> findImport(IJavaProject project, String pattern)
    throws Exception
  {
    ArrayList<String> results = new ArrayList<String>();
    SearchPattern searchPattern =
      SearchPattern.createPattern(pattern,
          IJavaSearchConstants.TYPE,
          IJavaSearchConstants.DECLARATIONS,
          SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
    IJavaSearchScope scope =
      SearchEngine.createJavaSearchScope(new IJavaElement[]{project});
    List<SearchMatch> matches = super.search(searchPattern, scope);
    for(SearchMatch match : matches){
      if(match.getAccuracy() == SearchMatch.A_ACCURATE){
        Position result = createPosition(project.getProject(), match);
        IType element = (IType)match.getElement();
        if(Flags.isPublic(element.getFlags())){
          String imprt = result.getMessage().replace('$', '.');
          if(!results.contains(imprt)){
            results.add(imprt);
          }
        }
      }
    }
    return results;
  }
}
