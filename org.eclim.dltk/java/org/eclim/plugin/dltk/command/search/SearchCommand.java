/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.dltk.command.search;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectManager;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.dltk.project.DltkProjectManager;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;

import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.core.search.SearchMatch;
import org.eclipse.dltk.core.search.SearchParticipant;
import org.eclipse.dltk.core.search.SearchPattern;

import org.eclipse.dltk.internal.corext.util.SearchUtils;

import org.eclipse.dltk.internal.ui.search.DLTKSearchScopeFactory;

/**
 * Command for dltk project search requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "dltk_search",
  options =
    "OPTIONAL n project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL l length ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL p pattern ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL x context ARG," +
    "OPTIONAL s scope ARG," +
    "OPTIONAL i case_insensitive NOARG"
)
public class SearchCommand
  extends AbstractCommand
{
  public static final String CONTEXT_ALL = "all";
  public static final String CONTEXT_DECLARATIONS = "declarations";
  //public static final String CONTEXT_IMPLEMENTORS = "implementors";
  public static final String CONTEXT_REFERENCES = "references";

  public static final String SCOPE_ALL = "all";
  public static final String SCOPE_PROJECT = "project";

  public static final String TYPE_CLASS = "class";
  public static final String TYPE_METHOD = "method";
  public static final String TYPE_FUNCTION = "function";
  public static final String TYPE_FIELD = "field";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.NAME_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String offset = commandLine.getValue(Options.OFFSET_OPTION);
    String length = commandLine.getValue(Options.LENGTH_OPTION);
    String pattern = commandLine.getValue(Options.PATTERN_OPTION);
    int type = getType(commandLine.getValue(Options.TYPE_OPTION));
    int context = getContext(commandLine.getValue(Options.CONTEXT_OPTION));

    IProject project = projectName != null ?
      ProjectUtils.getProject(projectName) : null;
    IDLTKSearchScope scope = getScope(
        commandLine.getValue(Options.SCOPE_OPTION), type, project);

    SearchEngine engine = new SearchEngine();
    //IProject[] projects = DLTKSearchScopeFactory.getInstance().getProjects(scope);

    IDLTKLanguageToolkit toolkit = scope.getLanguageToolkit();

    SearchPattern searchPattern = null;

    // element search
    if(file != null && offset != null && length != null){
      IFile ifile = ProjectUtils.getFile(project, file);

      ISourceModule src = DLTKCore.createSourceModuleFrom(ifile);
      IModelElement[] elements = getElements(
          src, getOffset(commandLine), Integer.parseInt(length));
      IModelElement element = null;
      if(elements != null && elements.length > 0){
        element = elements[0];
      }

      //ScriptModelUtil.reconcile(src);
      if (element != null && element.exists()) {
        searchPattern = SearchPattern.createPattern(
            element, context, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE, toolkit);
      }
    }else{
      int mode = getMode(pattern) | SearchPattern.R_ERASURE_MATCH;

      boolean caseSensitive =
        !commandLine.hasOption(Options.CASE_INSENSITIVE_OPTION);
      if (caseSensitive){
        mode |= SearchPattern.R_CASE_SENSITIVE;
      }

      if (type == IDLTKSearchConstants.UNKNOWN){
        SearchPattern byType = SearchPattern.createPattern(
            pattern, IDLTKSearchConstants.TYPE, context, mode, toolkit);
        SearchPattern byMethod = SearchPattern.createPattern(
            pattern, IDLTKSearchConstants.METHOD, context, mode, toolkit);
        SearchPattern byField = SearchPattern.createPattern(
            pattern, IDLTKSearchConstants.FIELD, context, mode, toolkit);
        searchPattern = SearchPattern.createOrPattern(
            byType, SearchPattern.createOrPattern(byMethod, byField));
      }else{
        searchPattern = SearchPattern.createPattern(
            pattern, type, context, mode, toolkit);
      }
    }

    if (searchPattern != null){
      SearchRequestor requestor = new SearchRequestor();
      engine.search(
          searchPattern,
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          scope,
          requestor,
          new NullProgressMonitor());

      return requestor.getMatches();
    }
    return null;
  }

  /**
   * Find the elements at the given offset.
   *
   * @param src The source to find the elements in.
   * @param offset The offset to find the elements at.
   * @param length The length of the element.
   * @return Array of IModelElement.
   */
  protected IModelElement[] getElements(
      ISourceModule src, int offset, int length)
  {
    try{
      return src.codeSelect(offset, length);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  /**
   * Gets the search scope to use.
   *
   * @param scope The string name of the scope.
   * @param type The type to search for.
   * @param project The current project.
   *
   * @return The IDLTKSearchScope.
   */
  protected IDLTKSearchScope getScope(
      String scope, int type, IProject project)
  {
    boolean includeInterpreterEnvironment = false;
    DLTKSearchScopeFactory factory = DLTKSearchScopeFactory.getInstance();

    IDLTKLanguageToolkit toolkit = null;
    ProjectManager manager = ProjectManagement.getProjectManager(getNature());
    if(manager instanceof DltkProjectManager){
      DltkProjectManager dltkManager = (DltkProjectManager)manager;
      toolkit = dltkManager.getLanguageToolkit(dltkManager.getNatureId());
    }
    if (toolkit == null && project != null){
      try{
        for(String nature : ProjectManagement.getProjectManagerNatures()){
          if(project.hasNature(nature)){
            manager = ProjectManagement.getProjectManager(nature);
            if(manager instanceof DltkProjectManager){
              DltkProjectManager dltkManager = (DltkProjectManager)manager;
              toolkit = dltkManager.getLanguageToolkit(dltkManager.getNatureId());
              break;
            }
          }
        }
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
    }
    IDLTKSearchScope searchScope = null;

    if (SCOPE_PROJECT.equals(scope)){
      String[] names = new String[]{project.getName()};
      searchScope = factory.createProjectSearchScope(
          names, includeInterpreterEnvironment, toolkit);
    }else{ // workspace
      searchScope = factory.createWorkspaceScope(
          includeInterpreterEnvironment, toolkit);
    }
    return searchScope;
  }

  /**
   * Gets the nature to use for the current search.
   *
   * @return The eclipse nature.
   */
  protected String getNature()
  {
    return null;
  }

  /**
   * Translates the string context to the int equivalent.
   *
   * @param context The String context.
   * @return The int context
   */
  protected int getContext(String context)
  {
    if(CONTEXT_ALL.equals(context)){
      return IDLTKSearchConstants.ALL_OCCURRENCES;
    //}else if(CONTEXT_IMPLEMENTORS.equals(context)){
    //  return IDLTKSearchConstants.IMPLEMENTORS;
    }else if(CONTEXT_REFERENCES.equals(context)){
      return IDLTKSearchConstants.REFERENCES;
    }
    return IDLTKSearchConstants.DECLARATIONS;
  }

  /**
   * Translates the string type to the int equivalent.
   *
   * @param type The String type.
   * @return The int type.
   */
  protected int getType(String type)
  {
    if(TYPE_CLASS.equals(type)){
      return IDLTKSearchConstants.TYPE;
    }else if(TYPE_METHOD.equals(type) || TYPE_FUNCTION.equals(type)){
      return IDLTKSearchConstants.METHOD;
    }else if(TYPE_FIELD.equals(type)){
      return IDLTKSearchConstants.FIELD;
    }
    return IDLTKSearchConstants.UNKNOWN;
  }

  /**
   * Determines the pattern mode to use based on the supplied search pattern.
   *
   * @param pattern The search pattern
   * @return The pattern matching mode.
   */
  private int getMode(String pattern)
  {
    if (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1) {
      return SearchPattern.R_PATTERN_MATCH;
    }
    if (SearchUtils.isCamelCasePattern(pattern)) {
      return SearchPattern.R_CAMELCASE_MATCH;
    }
    return SearchPattern.R_EXACT_MATCH;
  }

  /**
   * Get the search result element name for the given element.
   *
   * @param el The search result element.
   * @return The name.
   */
  protected String getElement(Object el)
  {
    IModelElement element = (IModelElement)el;
    ArrayList<IModelElement> lineage = new ArrayList<IModelElement>();
    while (element.getElementType() != IModelElement.SOURCE_MODULE){
      lineage.add(0, element);
      element = element.getParent();
    }

    StringBuffer fullyQualified = new StringBuffer();
    for(IModelElement e : lineage){
      if (fullyQualified.length() != 0){
        fullyQualified.append(getElementSeparator());
      }
      if (e.getElementType() == IModelElement.TYPE){
        fullyQualified.append(getElementTypeName()).append(' ');
      }
      if (e.getElementType() == IModelElement.FIELD){
        fullyQualified.append(getElementFieldName()).append(' ');
      }
      if (e.getElementType() == IModelElement.METHOD){
        if (e.getParent().getElementType() == IModelElement.TYPE){
          fullyQualified.append(getElementMethodName()).append(' ');
        }else{
          fullyQualified.append(getElementFunctionName()).append(' ');
        }
      }
      fullyQualified.append(e.getElementName());
    }

    return fullyQualified.toString();
  }

  protected String getElementSeparator()
  {
    return " ";
  }

  protected String getElementTypeName()
  {
    return "type";
  }

  protected String getElementFieldName()
  {
    return "field";
  }

  protected String getElementMethodName()
  {
    return "method";
  }

  protected String getElementFunctionName()
  {
    return "function";
  }

  private class SearchRequestor
    extends org.eclipse.dltk.core.search.SearchRequestor
  {
    private ArrayList<Position> matches = new ArrayList<Position>();

    @Override
    public void acceptSearchMatch(SearchMatch match)
      throws CoreException
    {
      if (match.getAccuracy() == SearchMatch.A_ACCURATE){
        IModelElement element = (IModelElement)match.getElement();
        ArrayList<IModelElement> lineage = new ArrayList<IModelElement>();
        while (element.getElementType() != IModelElement.SOURCE_MODULE){
          lineage.add(0, element);
          element = element.getParent();
        }
        StringBuffer fullyQualified = new StringBuffer();
        for(IModelElement el : lineage){
          if (fullyQualified.length() != 0){
            fullyQualified.append(" -> ");
          }
          if (el.getElementType() == IModelElement.TYPE){
            fullyQualified.append("type ");
          }
          if (el.getElementType() == IModelElement.METHOD){
            fullyQualified.append("method ");
          }
          fullyQualified.append(el.getElementName());
        }

        String filename = match.getResource()
          .getLocation().toOSString().replace('\\', '/');
        File file = new File(filename);
        if (!file.exists() || !file.isFile()){
          // ignoring results that don't have a file that exists.
          return;
        }

        Position position = Position.fromOffset(
            filename, StringUtils.EMPTY, match.getOffset(), match.getLength());

        int index = matches.indexOf(position);
        String name = SearchCommand.this.getElement(match.getElement());
        if(index == -1){
          position.setMessage(name);
          if(!matches.contains(position)){
            matches.add(position);
          }
        }else if (!StringUtils.EMPTY.equals(name)){
          // the second occurrence should be the one with the name.
          position = matches.get(index);
          position.setMessage(name);
        }
      }
    }

    /**
     * Gets a list of all the matches found.
     *
     * @return List of SearchMatch.
     */
    public List<Position> getMatches()
    {
      //Collections.sort(matches, MATCH_COMPARATOR);
      return matches;
    }
  }
}
