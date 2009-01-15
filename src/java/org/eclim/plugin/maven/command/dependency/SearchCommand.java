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

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;
import org.eclim.util.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Command for searching online maven repository.
 *
 * @author Eric Van Dewoestine
 */
public class SearchCommand
  extends AbstractCommand
{
  private static final String URL =
    "http://maven.ozacc.com/search?type=jar&format=xml&keyword=";

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String search = commandLine.getValue(Options.SEARCH_OPTION);
    return SearchFilter.instance.filter(commandLine, searchRepositories(search));
  }

  /**
   * Searches the repositories supported by maven.ozacc.com.
   *
   * @param query The search query.
   * @return Possibly empty List of results.
   */
  private List<Dependency> searchRepositories(String query)
    throws Exception
  {
    ArrayList<Dependency> dependencies = new ArrayList<Dependency>();

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
