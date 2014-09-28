/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
import java.util.Iterator;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.apache.tools.ant.taskdefs.condition.Os;

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
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;

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

  private static final Pattern INNER_CLASS =
    Pattern.compile("(.*?)(\\w+\\$)(\\w.*)");

  protected static final String ANDROID_NATURE =
    "com.android.ide.eclipse.adt.AndroidNature";
  private static final Pattern ANDROID_JDK_URL =
    Pattern.compile(".*android\\.jar!java.*");

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    List<SearchMatch> matches = executeSearch(commandLine);
    String projectName = commandLine.getValue(Options.NAME_OPTION);
    IProject project = projectName != null ?
      ProjectUtils.getProject(projectName) : null;

    ArrayList<Position> results = new ArrayList<Position>();
    for(SearchMatch match : matches){
      IJavaElement element = (IJavaElement)match.getElement();
      if (element != null){
        int elementType = element.getElementType();
        if (elementType != IJavaElement.PACKAGE_FRAGMENT &&
            elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT)
        {
          Position result = createPosition(project, match);
          if(result != null){
            results.add(result);
          }
        }
      }
    }
    return results;
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
    IJavaProject javaProject = project != null ?
      JavaUtils.getJavaProject(project) : null;

    // element search
    IJavaElement implementedTarget = null;
    if(file != null && offset != null && length != null){
      int charOffset = getOffset(commandLine);
      IJavaElement element = getElement(
          javaProject, file, charOffset, Integer.parseInt(length));
      if(element != null){
        // user requested a contextual search.
        if(context == -1){
          context = getElementContextualContext(element);

        // jdt search doesn't support implementors for method searches, so
        // switch to declarations.
        }else if (context == IJavaSearchConstants.IMPLEMENTORS &&
            element.getElementType() == IJavaElement.METHOD)
        {
          if (element instanceof IMethod) {
            // it *should* be instanceof since that's the type,
            //  but let's be overly cautious
            implementedTarget = element;
            element = element.getAncestor(IJavaElement.TYPE);
          } else {
            context = IJavaSearchConstants.DECLARATIONS;
          }
        } else if (context == IJavaSearchConstants.IMPLEMENTORS &&
            (element.getElementType() == IJavaElement.FIELD ||
             element.getElementType() == IJavaElement.LOCAL_VARIABLE)) {
          // it doesn't make sense to search for implementors
          //  of a variable, so switch to declarations
          context = IJavaSearchConstants.DECLARATIONS;
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

      // jdt search doesn't support implementors for method searches, so switch
      // to declarations.
      if (type == IJavaSearchConstants.METHOD &&
          context == IJavaSearchConstants.IMPLEMENTORS)
      {
        context = IJavaSearchConstants.DECLARATIONS;
      }

      // hack for inner classes
      Matcher matcher = INNER_CLASS.matcher(pat);
      if (matcher.matches()){
        // pattern search doesn't support org.test.Type$Inner or
        // org.test.Type.Inner, so convert it to org.test.*Inner, then filter
        // the results.
        pattern = SearchPattern.createPattern(
            matcher.replaceFirst("$1*$3"), type, context, matchType);
        Pattern toMatch = Pattern.compile(pat
          .replace(".", "\\.")
          .replace("$", "\\$")
          .replace("(", "\\(")
          .replace(")", "\\)")
          .replace("*", ".*")
          .replace("?", "."));
        List<SearchMatch> matches = search(pattern,
                getScope(scope, javaProject),
                null);
        Iterator<SearchMatch> iterator = matches.iterator();
        while (iterator.hasNext()){
          SearchMatch match = iterator.next();
          String name = JavaUtils.getFullyQualifiedName(
              (IJavaElement)match.getElement()).replace("#", ".");
          if (!toMatch.matcher(name).matches()){
            iterator.remove();
          }
        }
        return matches;
      }

      pattern = SearchPattern.createPattern(pat, type, context, matchType);

    // bad search request
    }else{
      throw new IllegalArgumentException(
          Services.getMessage("java_search.indeterminate"));
    }

    List<SearchMatch> matches = search(pattern,
            getScope(scope, javaProject),
            implementedTarget);
    return matches;
  }

  /**
   * Executes the search.
   *
   * @param pattern The search pattern.
   * @param scope The scope of the search (file, project, all, etc).
   * @param implementedTarget If provided, an IJavaElement representing
   *    a method that must be declared in the resulting types
   *
   * @return List of matches.
   */
  protected List<SearchMatch> search(
      SearchPattern pattern,
      IJavaSearchScope scope,
      IJavaElement implementedTarget)
    throws CoreException
  {
    SearchRequestor requestor = implementedTarget instanceof IMethod
        ? new ImplementorSearchRequestor((IMethod) implementedTarget)
        : new SearchRequestor();
    if(pattern != null){
      SearchEngine engine = new SearchEngine();
      SearchParticipant[] participants =
        new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()};
      engine.search(pattern, participants, scope, requestor, null);
    }
    return requestor.getMatches();
  }

  /**
   * Gets a IJavaElement by its position.
   *
   * @param javaProject The IJavaProject the file is in.
   * @param filename The file containing the element.
   * @param offset The offset of the element in the file.
   * @param length The lenght of the element.
   * @return The element.
   */
  protected IJavaElement getElement(
      IJavaProject javaProject, String filename, int offset, int length)
    throws Exception
  {
    ICodeAssist code = null;
    try{
      code = JavaUtils.getCompilationUnit(javaProject, filename);
    }catch(IllegalArgumentException iae){
      // source not found, try location the class file.
      code = JavaUtils.findClassFile(javaProject, filename);
    }

    if (code != null){
      IJavaElement[] elements = code.codeSelect(offset, length);
      if(elements != null && elements.length > 0){
        return elements[0];
      }
    }
    return null;
  }

  /**
   * Determines if the supplied path is a jar compatible path that can be
   * converted to a jar: url.
   *
   * @param path The IPath.
   * @return True if a jar or zip, false otherwise.
   */
  protected boolean isJarArchive(IPath path)
  {
    String ext = path.getFileExtension();
    return ext != null && ext.toLowerCase().matches("^(jar|zip)$");
  }

  /**
   * Creates a Position from the supplied SearchMatch.
   *
   * @param project The project searching from.
   * @param match The SearchMatch.
   * @return The Position.
   */
  protected Position createPosition(IProject project, SearchMatch match)
    throws Exception
  {
    IJavaElement element = (IJavaElement)match.getElement();
    IJavaElement parent = JavaUtils.getPrimaryElement(element);

    String file = null;
    String elementName = JavaUtils.getFullyQualifiedName(parent);
    if(parent.getElementType() == IJavaElement.CLASS_FILE){
      IResource resource = parent.getResource();
      // occurs with a referenced project as a lib with no source and class
      // files that are not archived in that project
      if (resource != null &&
          resource.getType() == IResource.FILE &&
          !isJarArchive(resource.getLocation()))
      {
        file = resource.getLocation().toOSString();

      }else{
        IPath path = null;
        IPackageFragmentRoot root = (IPackageFragmentRoot)
          parent.getParent().getParent();
        resource = root.getResource();
        if (resource != null){
          if (resource.getType() == IResource.PROJECT){
            path = ProjectUtils.getIPath((IProject)resource);
          }else{
            path = resource.getLocation();
          }
        }else{
          path = root.getPath();
        }

        String classFile = elementName.replace('.', File.separatorChar);
        if (isJarArchive(path)){
          file = "jar:file://" + path.toOSString() + '!' + classFile + ".class";
        }else{
          file = path.toOSString() + '/' + classFile + ".class";
        }

        // android injects its jdk classes, so filter those out if the project
        // doesn't have the android nature.
        if (ANDROID_JDK_URL.matcher(file).matches() &&
            project != null && !project.hasNature(ANDROID_NATURE))
        {
          return null;
        }

        // if a source path attachment exists, use it.
        IPath srcPath = root.getSourceAttachmentPath();
        if(srcPath != null){
          String rootPath;
          IProject elementProject = root.getJavaProject().getProject();

          // determine if src path is project relative or file system absolute.
          if(srcPath.isAbsolute() &&
             elementProject.getName().equals(srcPath.segment(0)))
          {
            rootPath = ProjectUtils.getFilePath(elementProject,
                srcPath.toString());
          }else{
            rootPath = srcPath.toOSString();
          }
          String srcFile = FileUtils.toUrl(
              rootPath + File.separator + classFile + ".java");

          // see if source file exists at source path.
          FileSystemManager fsManager = VFS.getManager();
          FileObject fileObject = fsManager.resolveFile(srcFile.replace("%", "%25"));
          if(fileObject.exists()){
            file = srcFile;

          // jdk sources on osx are under a "src/" dir in the jar
          }else if (Os.isFamily(Os.FAMILY_MAC)){
            srcFile = FileUtils.toUrl(
                rootPath + File.separator + "src" +
                File.separator + classFile + ".java");
            fileObject = fsManager.resolveFile(srcFile.replace("%", "%25"));
            if(fileObject.exists()){
              file = srcFile;
            }
          }
        }
      }
    }else{
      IPath location = match.getResource().getLocation();
      file = location != null ? location.toOSString() : null;
    }

    elementName = JavaUtils.getFullyQualifiedName(element);
    return Position.fromOffset(
        file.replace('\\', '/'), elementName,
        match.getOffset(), match.getLength());
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

    // type / field / method declaration
    if (theClass.equals(org.eclipse.jdt.internal.core.SourceType.class) ||
        theClass.equals(org.eclipse.jdt.internal.core.SourceField.class) ||
        theClass.equals(org.eclipse.jdt.internal.core.SourceMethod.class))
    {
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
