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

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Position;

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

/**
 * Command to handle java search requests.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
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
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    Object result = executeSearch(_commandLine);
    if(result instanceof Throwable){
      return result;
    }
    try{
      return filter(_commandLine, result);
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Executes the search.
   *
   * @param _commandLine The command line for the search.
   * @return The search results.
   */
  public Object executeSearch (CommandLine _commandLine)
    throws IOException
  {
    List results = new ArrayList();
    try{
      int context = -1;
      if(_commandLine.hasOption(Options.CONTEXT_OPTION)){
        context = getContext(_commandLine.getValue(Options.CONTEXT_OPTION));
      }
      String project = _commandLine.getValue(Options.NAME_OPTION);
      String scope = _commandLine.getValue(Options.SCOPE_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String offset = _commandLine.getValue(Options.OFFSET_OPTION);
      String length = _commandLine.getValue(Options.LENGTH_OPTION);
      String pat = _commandLine.getValue(Options.PATTERN_OPTION);

      SearchPattern pattern = null;

      // element search
      if(file != null && offset != null && length != null){
        Position position = new Position(
            file, Integer.parseInt(offset), Integer.parseInt(length));
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
        int type = getType(_commandLine.getValue(Options.TYPE_OPTION));
        int matchType = (pat.indexOf('*') != -1 || pat.indexOf('?') != -1) ?
          SearchPattern.R_PATTERN_MATCH :
          SearchPattern.R_EXACT_MATCH;

        pattern = SearchPattern.createPattern(pat, type, context, matchType);

      // bad search request
      }else{
        throw new IllegalArgumentException(
            Services.getMessage("java_search.indeterminate"));
      }

      if(pattern != null){
        IJavaProject javaProject = JavaUtils.getJavaProject(project);
        IType type = null;
        if(file.endsWith(".java")){
          type = JavaUtils.getCompilationUnit(project, file).findPrimaryType();
        }
        List matches = search(pattern, getScope(scope, javaProject, type));
        for(Iterator ii = matches.iterator(); ii.hasNext();){
          SearchMatch match = (SearchMatch)ii.next();
          if (match.getElement() != null){
            int elementType = ((IJavaElement)match.getElement()).getElementType();
            if (elementType != IJavaElement.PACKAGE_FRAGMENT &&
                elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT)
            {
              Object result = createSearchResult(match);
              if(result != null){
                results.add(result);
              }
            }
          }
        }
      }
    }catch(Exception e){
      return e;
    }
    return results;
  }

  /**
   * Executes the search.
   *
   * @param _pattern The search pattern.
   * @param _scope The scope of the search (file, project, all, etc).
   *
   * @return List of matches.
   */
  protected List search (SearchPattern _pattern, IJavaSearchScope _scope)
    throws CoreException
  {
    SearchRequestor requestor = new SearchRequestor();
    if(_pattern != null){
      SearchEngine engine = new SearchEngine();
      SearchParticipant[] participants = new SearchParticipant[]{
        SearchEngine.getDefaultSearchParticipant()};
      engine.search(_pattern, participants, _scope, requestor, null);
    }
    return requestor.getMatches();
  }

  /**
   * Gets a IJavaElement by its position.
   *
   * @param project The project the file is in.
   * @param _position The element's position.
   * @return The element.
   */
  protected IJavaElement getElement (String project, Position _position)
    throws Exception
  {
    ICompilationUnit src = JavaUtils.getCompilationUnit(project,
        _position.getFilename());
    IJavaElement[] elements = src.codeSelect(
        _position.getOffset(), _position.getLength());
    if(elements != null && elements.length > 0){
      return elements[0];
    }
    return null;
  }

  /**
   * Creates a SearchResult from the supplied SearchMatch.
   *
   * @param _match The SearchMatch.
   * @return The SearchResult.
   */
  protected Object createSearchResult (SearchMatch _match)
    throws Exception
  {
    IJavaElement element = (IJavaElement)_match.getElement();
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
        String srcFile = FileUtils.toUrl(
            srcPath.toOSString() + File.separator + classFile + ".java");
        // see if source file exists at source path.
        FileSystemManager fsManager = VFS.getManager();
        FileObject fileObject = fsManager.resolveFile(srcFile);
        if(fileObject.exists()){
          file = srcFile;
        }
      }
    }else{
      IPath location = _match.getResource().getLocation();
      file = location != null ? location.toOSString() : null;
    }

    elementName = JavaUtils.getFullyQualifiedName(element);
    return new SearchResult(
        archive, elementName, file, _match.getOffset(), _match.getLength());
  }

  /**
   * Gets the search scope to use.
   *
   * @param _scope The string name of the scope.
   * @param _project The current project.
   * @param _type The current type.
   *
   * @return The IJavaSearchScope equivalent.
   */
  protected IJavaSearchScope getScope (
      String _scope, IJavaProject _project, IType _type)
    throws Exception
  {
    if(SCOPE_PROJECT.equals(_scope)){
      return SearchEngine.createJavaSearchScope(new IJavaElement[]{_project});
    }else if(SCOPE_TYPE.equals(_scope)){
      return SearchEngine.createJavaSearchScope(new IJavaElement[]{_type});
    }
    return SearchEngine.createWorkspaceScope();
  }

  /**
   * Determins the appropriate context to used base on the elements context.
   *
   * @param _element The IJavaElement.
   * @return The int context
   */
  protected int getElementContextualContext (IJavaElement _element)
  {
    Class theClass = _element.getClass();

    // type declaration
    if(theClass.equals(org.eclipse.jdt.internal.core.SourceType.class)){
      return IJavaSearchConstants.IMPLEMENTORS;
    }

    // field / method declaration
    if (theClass.equals(org.eclipse.jdt.internal.core.SourceField.class) ||
        theClass.equals(org.eclipse.jdt.internal.core.SourceMethod.class))
    {
      return IJavaSearchConstants.ALL_OCCURRENCES;
    }

    return IJavaSearchConstants.DECLARATIONS;
  }

  /**
   * Translates the string context to the int equivalent.
   *
   * @param _context The String context.
   * @return The int context
   */
  protected int getContext (String _context)
  {
    if(CONTEXT_ALL.equals(_context)){
      return IJavaSearchConstants.ALL_OCCURRENCES;
    }else if(CONTEXT_IMPLEMENTORS.equals(_context)){
      return IJavaSearchConstants.IMPLEMENTORS;
    }else if(CONTEXT_REFERENCES.equals(_context)){
      return IJavaSearchConstants.REFERENCES;
    }
    return IJavaSearchConstants.DECLARATIONS;
  }

  /**
   * Translates the string type to the int equivalent.
   *
   * @param _type The String type.
   * @return The int type.
   */
  protected int getType (String _type)
  {
    if(TYPE_ANNOTATION.equals(_type)){
      return IJavaSearchConstants.ANNOTATION_TYPE;
    }else if(TYPE_CLASS.equals(_type)){
      return IJavaSearchConstants.CLASS;
    }else if(TYPE_CLASS_OR_ENUM.equals(_type)){
      return IJavaSearchConstants.CLASS_AND_ENUM;
    }else if(TYPE_CLASS_OR_INTERFACE.equals(_type)){
      return IJavaSearchConstants.CLASS_AND_INTERFACE;
    }else if(TYPE_CONSTRUCTOR.equals(_type)){
      return IJavaSearchConstants.CONSTRUCTOR;
    }else if(TYPE_ENUM.equals(_type)){
      return IJavaSearchConstants.ENUM;
    }else if(TYPE_FIELD.equals(_type)){
      return IJavaSearchConstants.FIELD;
    }else if(TYPE_INTERFACE.equals(_type)){
      return IJavaSearchConstants.INTERFACE;
    }else if(TYPE_METHOD.equals(_type)){
      return IJavaSearchConstants.METHOD;
    }else if(TYPE_PACKAGE.equals(_type)){
      return IJavaSearchConstants.PACKAGE;
    }
    return IJavaSearchConstants.TYPE;
  }
}
