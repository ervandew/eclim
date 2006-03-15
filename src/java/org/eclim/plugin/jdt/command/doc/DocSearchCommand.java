/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.plugin.jdt.command.doc;

import java.io.IOException;

import java.util.List;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.apache.log4j.Logger;

import org.eclim.command.CommandLine;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.plugin.jdt.command.search.SearchCommand;

import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.core.search.SearchMatch;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

/**
 * Command to search for javadocs.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class DocSearchCommand
  extends SearchCommand
{
  private static final Logger logger = Logger.getLogger(DocSearchCommand.class);

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    Object result = super.executeSearch(_commandLine);
    if(result instanceof Throwable){
      return result;
    }
    return filter(_commandLine, result);
  }

  /**
   * Creates a DocSearchResult from the supplied SearchMatch.
   *
   * @param _match The SearchMatch.
   * @return The DocSearchResult.
   */
  protected Object createSearchResult (SearchMatch _match)
    throws Exception
  {
    IJavaElement element = (IJavaElement)_match.getElement();

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
        String qualifiedName = JavaUtils.getFullyQualifiedName(element);

        // split up the class from the field/method
        String className = qualifiedName;
        String childElement = "";
        int index = className.indexOf('#');
        if(index != -1){
          childElement = className.substring(index);
          className = className.substring(0, index);
        }

        StringBuffer url = new StringBuffer(attributes[ii].getValue());
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
    return null;
  }
}
