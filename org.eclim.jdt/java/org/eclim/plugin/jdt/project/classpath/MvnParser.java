/**
 * Copyright (C) 2012 - 2017  Eric Van Dewoestine
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

import java.util.ArrayList;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of {@link Parser} for parsing a maven pom.xml file.
 *
 * @author Eric Van Dewoestine
 */
public class MvnParser
  extends AbstractXmlParser
{
  private static final String MVN_REPO = "M2_REPO";
  private static final String GROUP_ID = "groupId";
  private static final String ARTIFACT_ID = "artifactId";
  private static final String VERSION = "version";

  private static XPathExpression xpath;

  @Override
  public String getClasspathVar()
  {
    return MVN_REPO;
  }

  @Override
  public Dependency[] parse(Document document)
  {
    if(xpath == null){
      xpath = XmlUtils.createXPathExpression(
          "/project/dependencies/dependency");
    }

    if(JavaCore.getClasspathVariable(MVN_REPO) == null){
      throw new IllegalStateException(
          Services.getMessage("mvn.repo.not.set", MVN_REPO));
    }
    IPath path = new Path(MVN_REPO);

    NodeList results = null;
    try{
      results = (NodeList)xpath.evaluate(document, XPathConstants.NODESET);
    }catch(XPathExpressionException xpee){
      throw new RuntimeException(xpee);
    }
    ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
    for(int ii = 0; ii < results.getLength(); ii++){
      Element element = (Element)results.item(ii);

      NodeList group = element.getElementsByTagName(GROUP_ID);
      NodeList artifact = element.getElementsByTagName(ARTIFACT_ID);
      NodeList ver = element.getElementsByTagName(VERSION);
      if (group == null || group.getLength() < 1 ||
          artifact == null || artifact.getLength() < 1 ||
          ver == null || ver.getLength() < 1)
      {
        continue;
      }

      Node groupId = group.item(0).getFirstChild();
      Node artifactId = artifact.item(0).getFirstChild();
      Node version = ver.item(0).getFirstChild();
      if (groupId == null || artifactId == null || version == null){
        continue;
      }

      String groupIdValue = groupId.getNodeValue().trim();
      String artifactIdValue = artifactId.getNodeValue().trim();
      String versionValue = version.getNodeValue().trim();
      if (groupIdValue.length() == 0 ||
          artifactIdValue.length() == 0 ||
          versionValue.length() == 0)
      {
        continue;
      }

      Dependency dependency = new MvnDependency(
          groupIdValue, artifactIdValue, versionValue, path);
      dependency.setVariable(true);
      dependencies.add(dependency);
    }

    return (Dependency[])
      dependencies.toArray(new Dependency[dependencies.size()]);
  }
}
