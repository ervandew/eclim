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

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Command for searching online maven repository.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SearchCommand
  extends AbstractCommand
{
  private static final String URL =
    "http://maven.ozacc.com/search?type=jar&format=xml&keyword=";

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String search = _commandLine.getValue(Options.SEARCH_OPTION);
      return filter(_commandLine, searchRepositories(search));
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Searches the repositories supported by maven.ozacc.com.
   *
   * @param query The search query.
   * @return Possibly empty List of results.
   */
  private List searchRepositories (String query)
    throws Exception
  {
    ArrayList dependencies = new ArrayList();

    URL url = new URL(URL + query);
    InputStream in = null;
    try{
      in = url.openConnection().getInputStream();

      Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(in).getDocumentElement();
      NodeList nodes = root.getChildNodes();
      for (int ii = 0; ii < nodes.getLength(); ii++){
        Element element = (Element)nodes.item(ii);

        Dependency dependency = new Dependency();
        dependency.setGroupId(
            XmlUtils.getElementValue(element, Dependency.GROUP_ID));
        dependency.setArtifactId(
            XmlUtils.getElementValue(element, Dependency.ARTIFACT_ID));
        dependency.setVersion(
            XmlUtils.getElementValue(element, Dependency.VERSION));
        dependency.setType(
            XmlUtils.getElementValue(element, Dependency.TYPE));
        dependency.setRepository(
            XmlUtils.getElementValue(element, Dependency.REPOSITORY));

        dependencies.add(dependency);
      }
    }finally{
      IOUtils.closeQuietly(in);
    }

    return dependencies;
  }
}
