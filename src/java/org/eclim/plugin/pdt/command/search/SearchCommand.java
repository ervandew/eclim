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
package org.eclim.plugin.pdt.command.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.eclipse.ui.EclimEditorSite;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;

import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.core.search.SearchParticipant;
import org.eclipse.dltk.core.search.SearchPattern;

import org.eclipse.dltk.internal.corext.util.SearchUtils;

import org.eclipse.dltk.internal.ui.search.DLTKSearchQuery;
import org.eclipse.dltk.internal.ui.search.DLTKSearchScopeFactory;

import org.eclipse.php.internal.core.PHPLanguageToolkit;

import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;

import org.eclipse.ui.part.FileEditorInput;

/**
 * Command to search php code.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
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
  public static final String TYPE_METHOD = "function";
  public static final String TYPE_FIELD = "field";

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.NAME_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String offset = commandLine.getValue(Options.OFFSET_OPTION);
    String length = commandLine.getValue(Options.LENGTH_OPTION);
    String pattern = commandLine.getValue(Options.PATTERN_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    int type = getType(commandLine.getValue(Options.TYPE_OPTION));
    int context = getContext(commandLine.getValue(Options.TYPE_OPTION));

    IDLTKSearchScope scope = getScope(
        commandLine.getValue(Options.SCOPE_OPTION), type, project);

    SearchEngine engine = new SearchEngine();
    IProject[] projects = DLTKSearchScopeFactory.getInstance().getProjects(scope);

    IDLTKLanguageToolkit toolkit = scope.getLanguageToolkit();

    SearchPattern searchPattern = null;

    // element search
    if(file != null && offset != null && length != null){
      IFile ifile = ProjectUtils.getFile(project, file);

      // FIXME: find an easier way to get an ISourceModule
      IEditorSite site = new EclimEditorSite();
      IEditorInput input = new FileEditorInput(ifile);
      PHPStructuredEditor editor = new PHPStructuredEditor(){
        public void update()
        {
          // no-op to prevent StructuredTextEditor from running it.
        }

        protected void installOverrideIndicator(boolean provideAST)
        {
          // no-op to prevent PHPStructuredEditor from running it.
        }
      };
      editor.init(site, input);
      editor.setInput(input);

      ISourceModule src = (ISourceModule)editor.getModelElement();
      IModelElement[] elements = src.codeSelect(
          Integer.parseInt(offset), Integer.parseInt(length));
      IModelElement element = null;
      if(elements != null && elements.length > 0){
        element = elements[0];
      }
      //ScriptModelUtil.reconcile(unit);
      if (element != null && element.exists()) {
        searchPattern = SearchPattern.createPattern(
            element, context, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE, toolkit);
      }
    }else{
      int mode = getMode(pattern) | SearchPattern.R_ERASURE_MATCH;
      if (false /* case sensitive */){
        mode  |= SearchPattern.R_CASE_SENSITIVE;
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

      return SearchFilter.instance.filter(commandLine, requestor.getMatches());
    }
    return StringUtils.EMPTY;
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
  protected IDLTKSearchScope getScope(String scope, int type, IProject project)
    throws Exception
  {
    boolean includeInterpreterEnvironment = false;
    DLTKSearchScopeFactory factory = DLTKSearchScopeFactory.getInstance();

    IDLTKLanguageToolkit toolkit = PHPLanguageToolkit.getDefault();
    IDLTKSearchScope searchScope = null;

    if (SCOPE_PROJECT.equals(scope)){
      String[] names = new String[]{project.getName()};
      searchScope = factory.createProjectSearchScope(
          names, includeInterpreterEnvironment, toolkit);
    }else{ // workspace
      searchScope = factory.createWorkspaceScope(includeInterpreterEnvironment,
          toolkit);
    }
    return searchScope;
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
    }else if(TYPE_METHOD.equals(type)){
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
}
