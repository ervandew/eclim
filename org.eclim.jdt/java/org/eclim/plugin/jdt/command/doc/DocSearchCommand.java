/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.command.search.SearchCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.core.search.SearchMatch;

import org.eclipse.jdt.internal.core.JavaModelManager;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.internal.launching.JREContainer;

/**
 * Command to search for javadocs.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_docsearch",
  options =
    "REQUIRED n project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL l length ARG," +
    "OPTIONAL p pattern ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL x context ARG," +
    "OPTIONAL s scope ARG"
)
public class DocSearchCommand
  extends SearchCommand
{
  private static final Pattern LOCAL_URL =
    Pattern.compile("^file://(/|[A-Z]).*");
  private static final HashMap<String, String> JRE_DOCS =
    new HashMap<String, String>();
  static{
    JRE_DOCS.put(JavaCore.VERSION_1_3,
        "http://java.sun.com/j2se/1.3/docs/api/");
    JRE_DOCS.put(JavaCore.VERSION_1_4,
        "http://java.sun.com/j2se/1.4.2/docs/api/");
    JRE_DOCS.put(JavaCore.VERSION_1_5,
        "http://java.sun.com/j2se/1.5.0/docs/api/");
    JRE_DOCS.put(JavaCore.VERSION_1_6,
        "http://java.sun.com/javase/6/docs/api/");
  }

  private static final Pattern ANDROID_JDK_URL = Pattern.compile(
      ".*android.*?/docs/reference/java.*", Pattern.CASE_INSENSITIVE);

  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    List<SearchMatch> matches = executeSearch(commandLine);
    String projectName = commandLine.getValue(Options.NAME_OPTION);
    IProject project = ProjectUtils.getProject(projectName);

    ArrayList<String> results = new ArrayList<String>();
    for(SearchMatch match : matches){
      if (match.getElement() != null){
        int elementType = ((IJavaElement)match.getElement()).getElementType();
        if (elementType != IJavaElement.PACKAGE_FRAGMENT &&
            elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT)
        {
          String result = createDocSearchResult(project, match);
          if(result != null){
            results.add(result);
          }
        }
      }
    }
    return results;
  }

  /**
   * Creates a javadoc url from the supplied SearchMatch.
   *
   * @param project The project we are searching from.
   * @param match The SearchMatch.
   * @return The javadoc url.
   */
  private String createDocSearchResult(IProject project, SearchMatch match)
    throws Exception
  {
    IJavaElement element = (IJavaElement)match.getElement();

    IJavaElement parent = element;
    while(parent.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT){
      parent = parent.getParent();
    }

    IPackageFragmentRoot root = (IPackageFragmentRoot)parent;
    IClasspathEntry classpath = root.getRawClasspathEntry();
    if(classpath.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
      classpath = JavaModelUtil.getClasspathEntryToEdit(
          element.getJavaProject(), classpath.getPath(), root.getPath());
    }

    // may be null from JavaModelUtil.getClasspathEntryToEdit
    if (classpath != null){
      IClasspathAttribute[] attributes = classpath.getExtraAttributes();
      for(int ii = 0; ii < attributes.length; ii++){
        String name = attributes[ii].getName();
        if(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME.equals(name)){
          return buildUrl(project, attributes[ii].getValue(), element);
        }
      }
    }

    // somewhere in the eclipse 3.2 release canidate stage they stopped
    // providing default locations for jre javadocs, but the final version of
    // 3.2 seems to have it.  The following will lookup the location from our
    // own settings should this occur again.
    classpath = root.getRawClasspathEntry();
    if(classpath.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
      IClasspathContainer container =
        JavaModelManager.getJavaModelManager().getClasspathContainer(
            classpath.getPath(), element.getJavaProject());
      if(container instanceof JREContainer){
        String version = JavaUtils.getCompilerSourceCompliance(
            element.getJavaProject());
        String url = (String)JRE_DOCS.get(version);
        if(url != null){
          return buildUrl(project, url, element);
        }
      }
    }

    return null;
  }

  private String buildUrl(IProject project, String baseUrl, IJavaElement element)
    throws Exception
  {
    String qualifiedName = JavaUtils.getFullyQualifiedName(element);

    // split up the class from the field/method
    String className = qualifiedName;
    String childElement = "";
    int index = className.indexOf('#');
    if(index != -1){
      childElement = className.substring(index);
      className = className.substring(0, index);
    }

    String base = baseUrl.trim();
    if(base.startsWith("file:") && !LOCAL_URL.matcher(base).matches()){
      base = base.replaceFirst("file:/*(/|[A-Z])", "file://$1");
    }
    StringBuffer url = new StringBuffer(base);
    if(url.charAt(url.length() - 1) != '/'){
      url.append('/');
    }
    url.append(className.replace('.', '/').replace('$', '.'))
      .append(".html");

    /*FileSystemManager fsManager = VFS.getManager();
    if(!fsManager.resolveFile(url.toString()).exists()){
      logger.debug("'{}' does not exist.", url);
      return null;
    }*/

    url.append(childElement);
    String result = url.toString();

    // android injects its own docs, so filter those out if the project doesn't
    // have the android nature.
    if (ANDROID_JDK_URL.matcher(result).matches() &&
        !project.hasNature(ANDROID_NATURE))
    {
      return null;
    }

    return result;
  }
}
