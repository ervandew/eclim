/**
 * Copyright (c) 2005 - 2007
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
package org.eclim.plugin.pdt.command.search;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.command.filter.PositionFilter;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.php.internal.ui.search.IPHPSearchConstants;
import org.eclipse.php.internal.ui.search.IPHPSearchScope;
import org.eclipse.php.internal.ui.search.PHPSearchEngine;
import org.eclipse.php.internal.ui.search.PHPSearchResult;

import org.eclipse.php.internal.ui.search.decorators.PHPDataDecorator;

import org.eclipse.search.ui.text.Match;

/**
 * Command to search php code.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class SearchCommand
  extends AbstractCommand
{
  public static final String SCOPE_ALL = "all";
  public static final String SCOPE_PROJECT = "project";

  public static final String TYPE_CLASS = "class";
  public static final String TYPE_FUNCTION = "function";
  public static final String TYPE_CONSTANT = "constant";

  private static final PHPSearchEngine SEARCH = new PHPSearchEngine();

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);
    String pattern = _commandLine.getValue(Options.PATTERN_OPTION);
    IProject project = ProjectUtils.getProject(projectName);

    // TODO: if type == -1, run 3 searches?
    int type = getType(_commandLine.getValue(Options.TYPE_OPTION));
    IPHPSearchScope scope = getScope(
        _commandLine.getValue(Options.SCOPE_OPTION), type, project);

    PHPSearchResult result = new PHPSearchResult(null);
    SEARCH.search(pattern, scope, result, true, new NullProgressMonitor());

    IWorkspaceRoot root = project.getWorkspace().getRoot();

    List<Position> results = new ArrayList<Position>();
    for(Object element : result.getElements()){
      PHPDataDecorator decorator = (PHPDataDecorator)element;
      String file = decorator.getUserData().getFileName();
      IResource resource = root.findMember(file);
      if (resource != null){
        file = resource.getRawLocation().toOSString();
      }
      String name = decorator.getName();
      Match[] matches = result.getMatches(element);
      for(Match match : matches){
        Position position = new Position(file, match.getOffset(), 0);
        position.setMessage(name);
        results.add(position);
      }
    }

    return PositionFilter.instance.filter(_commandLine, results);
  }

  /**
   * Gets the search scope to use.
   *
   * @param _scope The string name of the scope.
   * @param _type The type to search for.
   * @param _project The current project.
   *
   * @return The IPHPSearchScope equivalent.
   */
  protected IPHPSearchScope getScope (String _scope, int _type, IProject _project)
    throws Exception
  {
    if(SCOPE_PROJECT.equals(_scope)){
      return PHPSearchEngine.createPHPSearchScope(_type, new Object[]{_project});
    }
    return PHPSearchEngine.createWorkspaceScope(_type);
  }

  /**
   * Translates the string type to the int equivalent.
   *
   * @param _type The String type.
   * @return The int type.
   */
  protected int getType (String _type)
  {
    if(TYPE_CLASS.equals(_type)){
      return IPHPSearchConstants.CLASS;
    }else if(TYPE_FUNCTION.equals(_type)){
      return IPHPSearchConstants.FUNCTION;
    }else if(TYPE_CONSTANT.equals(_type)){
      return IPHPSearchConstants.CONSTANT;
    }
    return -1;
  }
}
