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
package org.eclim.plugin.jdt.project.classpath;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.eclim.Services;

import org.eclim.plugin.core.util.XmlUtils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of {@link Parser} for parsing an
 * <a href="http://ant.apache.org/ivy/">ivy.xml</a> file.
 *
 * @author Eric Van Dewoestine
 */
public class IvyParser
  extends AbstractXmlParser
{
  private static final String IVY_REPO = "IVY_REPO";
  private static final String ORG = "org";
  private static final String NAME = "name";
  private static final String REVISION = "rev";

  private static XPathExpression xpath;

  @Override
  public String getClasspathVar()
  {
    return IVY_REPO;
  }

  @Override
  public Dependency[] parse(Document document)
  {
    if(xpath == null){
      xpath = XmlUtils.createXPathExpression(
          "/ivy-module/dependencies/dependency");
    }

    if(JavaCore.getClasspathVariable(IVY_REPO) == null){
      throw new IllegalStateException(
          Services.getMessage("ivy.repo.not.set", IVY_REPO));
    }
    IPath path = new Path(IVY_REPO);

    NodeList results = null;
    try{
      results = (NodeList)xpath.evaluate(document, XPathConstants.NODESET);
    }catch(XPathExpressionException xpee){
      throw new RuntimeException(xpee);
    }
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
  }
}
