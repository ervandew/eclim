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
package org.eclim.plugin.jdt.command.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.regex.Pattern;

import org.eclim.command.CommandLine;

import org.eclim.plugin.jdt.command.search.SearchCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

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

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    List<SearchMatch> matches = executeSearch(commandLine);

    ArrayList<String> results = new ArrayList<String>();
    for(SearchMatch match : matches){
      if (match.getElement() != null){
        int elementType = ((IJavaElement)match.getElement()).getElementType();
        if (elementType != IJavaElement.PACKAGE_FRAGMENT &&
            elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT){
          String result = createDocSearchResult(match);
          if(result != null){
            results.add(result);
          }
        }
      }
    }
    return DocSearchFilter.instance.filter(commandLine, results);
  }

  /**
   * Creates a javadoc url from the supplied SearchMatch.
   *
   * @param match The SearchMatch.
   * @return The javadoc url.
   */
  private String createDocSearchResult(SearchMatch match)
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
    IClasspathAttribute[] attributes = classpath.getExtraAttributes();
    for(int ii = 0; ii < attributes.length; ii++){
      String name = attributes[ii].getName();
      if(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME.equals(name)){
        return buildUrl(attributes[ii].getValue(), element);
      }
    }

    // somewhere in the eclipse 3.2 release canidate stage they stopped
    // providing default locations for jre javadocs, but the final version of
    // 3.2 seems to have contain it.  The following will lookup location from
    // our own settings should this occur again.
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
          return buildUrl(url, element);
        }
      }
    }

    return null;
  }

  private String buildUrl(String baseUrl, IJavaElement element)
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
    return url.toString();
  }
}
