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
package org.eclim.plugin.maven.command.dependency;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.command.OutputFilter;

import org.eclim.logging.Logger;

import org.eclim.util.IOUtils;
import org.eclim.util.ProjectUtils;
import org.eclim.util.XmlUtils;

import org.eclim.util.file.FileUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Filter for repository search results.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SearchFilter
  implements OutputFilter<List<Dependency>>
{
  public static final SearchFilter instance = new SearchFilter();

  private static final Logger logger = Logger.getLogger(SearchFilter.class);

  private static final String IVY = "ivy";
  private static final String DEPENDENCIES = "dependencies";
  private static final String DEPENDENCY = "dependency";

  /**
   * {@inheritDoc}
   */
  public String filter(CommandLine commandLine, List<Dependency> results)
  {
    List<Dependency> dependencies = null;
    try{
      String project = commandLine.getValue(Options.PROJECT_OPTION);
      String file = commandLine.getValue(Options.FILE_OPTION);
      String type = commandLine.getValue(Options.TYPE_OPTION);
      dependencies = getDependencies(project, file, type);
    }catch(Exception e){
      logger.warn("Unable to get dependencies.", e);
      dependencies = new ArrayList<Dependency>();
    }

    StringBuffer buffer = new StringBuffer();
    String groupId = null;
    for (Dependency dependency : results){
      if(!dependency.getGroupId().equals(groupId)){
        if(buffer.length() != 0){
          buffer.append('\n');
        }
        buffer.append(groupId = dependency.getGroupId());
      }

      buffer.append("\n\t");
      if(dependencies.contains(dependency)){
        buffer.append("//");
      }

      buffer.append(dependency.getArtifactId())
        .append('.')
        .append(dependency.getType())
        .append(" (")
        .append(dependency.getVersion())
        .append(')');
    }
    return buffer.toString();
  }

  /**
   * Get the project file's current dependencies.
   *
   * @param project The eclipse project name.
   * @param filename The project file.
   * @param type The file type (ivy, maven, mvn).
   * @return List of dependencies.
   */
  private List<Dependency> getDependencies(
      String project, String filename, String type)
    throws Exception
  {
    ArrayList<Dependency> list = new ArrayList<Dependency>();
    InputStream in = null;
    try{
      String file = FileUtils.concat(ProjectUtils.getPath(project), filename);
      Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(in = new FileInputStream(file)).getDocumentElement();
      NodeList nodes = ((Element)root.getElementsByTagName(DEPENDENCIES).item(0))
        .getElementsByTagName(DEPENDENCY);

      for (int ii = 0; ii < nodes.getLength(); ii++){
        Element element = (Element)nodes.item(ii);

        Dependency dependency = new Dependency();
        if(IVY.equals(type)){
          dependency.setGroupId(element.getAttribute(Dependency.ORG));
          dependency.setArtifactId(element.getAttribute(Dependency.NAME));
          dependency.setVersion(element.getAttribute(Dependency.REV));
          dependency.setType(Dependency.JAR);
        }else{
          dependency.setGroupId(
              XmlUtils.getElementValue(element, Dependency.GROUP_ID));
          dependency.setArtifactId(
              XmlUtils.getElementValue(element, Dependency.ARTIFACT_ID));
          dependency.setVersion(
              XmlUtils.getElementValue(element, Dependency.VERSION));
          dependency.setType(Dependency.JAR);
        }

        list.add(dependency);
      }
    }finally{
      IOUtils.closeQuietly(in);
    }
    return list;
  }
}
