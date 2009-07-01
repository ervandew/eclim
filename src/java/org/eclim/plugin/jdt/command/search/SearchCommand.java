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
package org.eclim.plugin.jdt.command.search;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Position;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;

import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * Command to handle java search requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_search",
  options =
    "OPTIONAL n project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL l length ARG," +
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
  public static final String CONTEXT_IMPLEMENTORS = "implementors";
  public static final String CONTEXT_REFERENCES = "references";

  public static final String SCOPE_ALL = "all";
  public static final String SCOPE_PROJECT = "project";
  public static final String SCOPE_TYPE = "type";

  public static final String TYPE_ALL = "all";
  public static final String TYPE_ANNOTATION = "annotation";
  public static final String TYPE_CLASS = "class";
  public static final String TYPE_CLASS_OR_ENUM = "classOrEnum";
  public static final String TYPE_CLASS_OR_INTERFACE = "classOrInterface";
  public static final String TYPE_CONSTRUCTOR = "constructor";
  public static final String TYPE_ENUM = "enum";
  public static final String TYPE_FIELD = "field";
  public static final String TYPE_INTERFACE = "interface";
  public static final String TYPE_METHOD = "method";
  public static final String TYPE_PACKAGE = "package";
  public static final String TYPE_TYPE = "type";

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    List<SearchMatch> matches = executeSearch(commandLine);

    ArrayList<SearchResult> results = new ArrayList<SearchResult>();
    for(SearchMatch match : matches){
      if (match.getElement() != null){
        int elementType = ((IJavaElement)match.getElement()).getElementType();
        if (elementType != IJavaElement.PACKAGE_FRAGMENT &&
            elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT){
          SearchResult result = createSearchResult(match);
          if(result != null){
            results.add(result);
          }
        }
      }
    }
    return SearchFilter.instance.filter(commandLine, results);
  }

  /**
   * Executes the search.
   *
   * @param commandLine The command line for the search.
   * @return The search results.
   */
  public List<SearchMatch> executeSearch(CommandLine commandLine)
    throws Exception
  {
    int context = -1;
    if(commandLine.hasOption(Options.CONTEXT_OPTION)){
      context = getContext(commandLine.getValue(Options.CONTEXT_OPTION));
    }
    String project = commandLine.getValue(Options.NAME_OPTION);
    String scope = commandLine.getValue(Options.SCOPE_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String offset = commandLine.getValue(Options.OFFSET_OPTION);
    String length = commandLine.getValue(Options.LENGTH_OPTION);
    String pat = commandLine.getValue(Options.PATTERN_OPTION);

    SearchPattern pattern = null;

    // element search
    if(file != null && offset != null && length != null){
      String encoding = commandLine.getValue(Options.ENCODING_OPTION);
      String filepath = ProjectUtils.getFilePath(project, file);

      int charOffset = FileUtils.byteOffsetToCharOffset(
          filepath, getOffset(commandLine), encoding);
      Position position = new Position(
          file, charOffset, Integer.parseInt(length));
      IJavaElement element = getElement(project, position);
      if(element != null){
        // user requested a contextual search.
        if(context == -1){
          context = getElementContextualContext(element);
        }
        pattern = SearchPattern.createPattern(element, context);
      }

    // pattern search
    }else if(pat != null){
      if(context == -1){
        context = IJavaSearchConstants.DECLARATIONS;
      }

      int matchType = SearchPattern.R_EXACT_MATCH;

      // wild card character supplied, use pattern matching.
      if(pat.indexOf('*') != -1 || pat.indexOf('?') != -1){
        matchType = SearchPattern.R_PATTERN_MATCH;

      // all upper case, add camel case support.
      }else if(pat.equals(pat.toUpperCase())){
        matchType |= SearchPattern.R_CAMELCASE_MATCH;
      }

      boolean caseSensitive =
        !commandLine.hasOption(Options.CASE_INSENSITIVE_OPTION);
      if(caseSensitive){
        matchType |= SearchPattern.R_CASE_SENSITIVE;
      }

      int type = getType(commandLine.getValue(Options.TYPE_OPTION));

      pattern = SearchPattern.createPattern(pat, type, context, matchType);

    // bad search request
    }else{
      throw new IllegalArgumentException(
          Services.getMessage("java_search.indeterminate"));
    }

    IJavaProject javaProject = project != null ?
      JavaUtils.getJavaProject(project) : null;
    List<SearchMatch> matches = search(pattern, getScope(scope, javaProject));
    return matches;
  }

  /**
   * Executes the search.
   *
   * @param pattern The search pattern.
   * @param scope The scope of the search (file, project, all, etc).
   *
   * @return List of matches.
   */
  protected List<SearchMatch> search(
      SearchPattern pattern, IJavaSearchScope scope)
    throws CoreException
  {
    SearchRequestor requestor = new SearchRequestor();
    if(pattern != null){
      SearchEngine engine = new SearchEngine();
      SearchParticipant[] participants = new SearchParticipant[]{
        SearchEngine.getDefaultSearchParticipant()};
      engine.search(pattern, participants, scope, requestor, null);
    }
    return requestor.getMatches();
  }

  /**
   * Gets a IJavaElement by its position.
   *
   * @param project The project the file is in.
   * @param position The element's position.
   * @return The element.
   */
  protected IJavaElement getElement(String project, Position position)
    throws Exception
  {
    ICompilationUnit src = JavaUtils.getCompilationUnit(project,
        position.getFilename());
    IJavaElement[] elements = src.codeSelect(
        position.getOffset(), position.getLength());
    if(elements != null && elements.length > 0){
      return elements[0];
    }
    return null;
  }

  /**
   * Creates a SearchResult from the supplied SearchMatch.
   *
   * @param match The SearchMatch.
   * @return The SearchResult.
   */
  protected SearchResult createSearchResult(SearchMatch match)
    throws Exception
  {
    IJavaElement element = (IJavaElement)match.getElement();
    IJavaElement parent = JavaUtils.getPrimaryElement(element);

    String archive = null;
    String file = null;
    String elementName = JavaUtils.getFullyQualifiedName(parent);
    if(parent.getElementType() == IJavaElement.CLASS_FILE){
      IPackageFragmentRoot root = (IPackageFragmentRoot)
        parent.getParent().getParent();
      archive = root.getPath().toOSString();

      String classFile = elementName.replace('.', File.separatorChar);
      file = "jar:file://" + archive + '!' + classFile + ".class";

      // if a source path attachment exists, use it.
      IPath srcPath = root.getSourceAttachmentPath();
      if(srcPath != null){
        String rootPath;
        IProject elementProject = root.getJavaProject().getProject();

        // determine if src path is project relative or file system absolute.
        if(srcPath.isAbsolute() &&
           elementProject.getName().equals(srcPath.segment(0))){
          rootPath = ProjectUtils.getFilePath(elementProject,
              srcPath.toString());
        }else{
          rootPath = srcPath.toOSString();
        }
        String srcFile = FileUtils.toUrl(
            rootPath + File.separator + classFile + ".java");

        // see if source file exists at source path.
        FileSystemManager fsManager = VFS.getManager();
        FileObject fileObject = fsManager.resolveFile(srcFile);
        if(fileObject.exists()){
          file = srcFile;
        }
      }
    }else{
      IPath location = match.getResource().getLocation();
      file = location != null ? location.toOSString() : null;
    }

    elementName = JavaUtils.getFullyQualifiedName(element);
    return new SearchResult(
        archive, elementName, file, match.getOffset(), match.getLength());
  }

  /**
   * Gets the search scope to use.
   *
   * @param scope The string name of the scope.
   * @param project The current project.
   *
   * @return The IJavaSearchScope equivalent.
   */
  protected IJavaSearchScope getScope(String scope, IJavaProject project)
    throws Exception
  {
    if(project == null){
      return SearchEngine.createWorkspaceScope();
    }else if(SCOPE_PROJECT.equals(scope)){
      return SearchEngine.createJavaSearchScope(new IJavaElement[]{project});
    }
    return SearchEngine.createWorkspaceScope();
  }

  /**
   * Determines the appropriate context to used base on the elements context.
   *
   * @param element The IJavaElement.
   * @return The int context
   */
  protected int getElementContextualContext(IJavaElement element)
  {
    Class<?> theClass = element.getClass();

    // type declaration
    if(theClass.equals(org.eclipse.jdt.internal.core.SourceType.class)){
      return IJavaSearchConstants.IMPLEMENTORS;
    }

    // field / method declaration
    if (theClass.equals(org.eclipse.jdt.internal.core.SourceField.class) ||
        theClass.equals(org.eclipse.jdt.internal.core.SourceMethod.class)){
      return IJavaSearchConstants.ALL_OCCURRENCES;
    }

    return IJavaSearchConstants.DECLARATIONS;
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
      return IJavaSearchConstants.ALL_OCCURRENCES;
    }else if(CONTEXT_IMPLEMENTORS.equals(context)){
      return IJavaSearchConstants.IMPLEMENTORS;
    }else if(CONTEXT_REFERENCES.equals(context)){
      return IJavaSearchConstants.REFERENCES;
    }
    return IJavaSearchConstants.DECLARATIONS;
  }

  /**
   * Translates the string type to the int equivalent.
   *
   * @param type The String type.
   * @return The int type.
   */
  protected int getType(String type)
  {
    if(TYPE_ANNOTATION.equals(type)){
      return IJavaSearchConstants.ANNOTATION_TYPE;
    }else if(TYPE_CLASS.equals(type)){
      return IJavaSearchConstants.CLASS;
    }else if(TYPE_CLASS_OR_ENUM.equals(type)){
      return IJavaSearchConstants.CLASS_AND_ENUM;
    }else if(TYPE_CLASS_OR_INTERFACE.equals(type)){
      return IJavaSearchConstants.CLASS_AND_INTERFACE;
    }else if(TYPE_CONSTRUCTOR.equals(type)){
      return IJavaSearchConstants.CONSTRUCTOR;
    }else if(TYPE_ENUM.equals(type)){
      return IJavaSearchConstants.ENUM;
    }else if(TYPE_FIELD.equals(type)){
      return IJavaSearchConstants.FIELD;
    }else if(TYPE_INTERFACE.equals(type)){
      return IJavaSearchConstants.INTERFACE;
    }else if(TYPE_METHOD.equals(type)){
      return IJavaSearchConstants.METHOD;
    }else if(TYPE_PACKAGE.equals(type)){
      return IJavaSearchConstants.PACKAGE;
    }
    return IJavaSearchConstants.TYPE;
  }
}
