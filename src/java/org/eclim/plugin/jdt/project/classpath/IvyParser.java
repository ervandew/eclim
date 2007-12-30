/**
 * Copyright (c) 2005 - 2008
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
package org.eclim.plugin.jdt.project.classpath;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.eclim.Services;

import org.eclim.util.XmlUtils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of {@link Parser} for parsing an
 * <a href="http://jayasoft.org/ivy/doc/ivyfile">ivy.xml</a> file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class IvyParser
  extends AbstractXmlParser
{
  private static final String IVY_REPO = "IVY_REPO";
  private static final String ORG = "org";
  private static final String NAME = "name";
  private static final String REVISION = "rev";

  private static XPathExpression xpath;

  /**
   * {@inheritDoc}
   */
  public Dependency[] parse (Document _document)
    throws Exception
  {
    try{
      if(xpath == null){
        xpath = XmlUtils.createXPathExpression(
            "/ivy-module/dependencies/dependency");
      }

      if(JavaCore.getClasspathVariable(IVY_REPO) == null){
        throw new IllegalStateException(
            Services.getMessage("ivy.repo.not.set", IVY_REPO));
      }
      IPath path = new Path(IVY_REPO);

      NodeList results = (NodeList)
        xpath.evaluate(_document, XPathConstants.NODESET);
      Dependency[] dependencies = new Dependency[results.getLength()];
      for(int ii = 0; ii < results.getLength(); ii++){
        Element element = (Element)results.item(ii);
        dependencies[ii] = new IvyDependency(
            element.getAttribute(ORG),
            element.getAttribute(NAME),
            element.getAttribute(REVISION),
            path);
        dependencies[ii].setVariable(true);
      }

      return dependencies;
    }catch(IllegalStateException iae){
      throw iae;
    }
  }
}
