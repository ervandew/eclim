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
package org.eclim.plugin.jdt.command.include;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.plugin.jdt.command.search.SearchCommand;
import org.eclim.plugin.jdt.command.search.SearchResult;

import org.eclipse.core.runtime.CoreException;

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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ImportCommand
  extends SearchCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    List results = new ArrayList();
    try{
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
      List matches = super.search(pattern, scope);
      for(Iterator ii = matches.iterator(); ii.hasNext();){
        SearchMatch match = (SearchMatch)ii.next();
        if(match.getAccuracy() == SearchMatch.A_ACCURATE){
          SearchResult result = (SearchResult)createSearchResult(match);
          IType element = (IType)match.getElement();
          if(Flags.isPublic(element.getFlags())){
            results.add(new ImportResult(
                  result.getElement(), element.getElementType()));
          }
        }
      }
      return filter(_commandLine, results);
    }catch(Exception e){
      return e;
    }
  }
}
