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
package org.eclim.plugin.maven.command.dependency;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.command.OutputFilter;

import org.eclim.util.IOUtils;
import org.eclim.util.ProjectUtils;
import org.eclim.util.XmlUtils;

import org.eclim.util.file.FileUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Filter for repository search results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
  public String filter (CommandLine _commandLine, List<Dependency> _result)
  {
    List<Dependency> dependencies = null;
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String type = _commandLine.getValue(Options.TYPE_OPTION);
      dependencies = getDependencies(project, file, type);
    }catch(Exception e){
      logger.warn("Unable to get dependencies.", e);
      dependencies = new ArrayList<Dependency>();
    }

    StringBuffer buffer = new StringBuffer();
    String groupId = null;
    for (Dependency dependency : _result){
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
   * @param _project The eclipse project name.
   * @param _file The project file.
   * @param _type The file type (ivy, maven, mvn).
   * @return List of dependencies.
   */
  private List<Dependency> getDependencies (
      String _project, String _file, String _type)
    throws Exception
  {
    ArrayList<Dependency> list = new ArrayList<Dependency>();
    InputStream in = null;
    try{
      String file = FileUtils.concat(ProjectUtils.getPath(_project), _file);
      Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(in = new FileInputStream(file)).getDocumentElement();
      NodeList nodes = ((Element)root.getElementsByTagName(DEPENDENCIES).item(0))
        .getElementsByTagName(DEPENDENCY);

      for (int ii = 0; ii < nodes.getLength(); ii++){
        Element element = (Element)nodes.item(ii);

        Dependency dependency = new Dependency();
        if(IVY.equals(_type)){
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
