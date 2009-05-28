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

import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.eclim.annotation.Command;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

/**
 * Command for searching online maven repository.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "maven_dependency_search",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED t type ARG," +
    "REQUIRED s search ARG"
)
public class SearchCommand
  extends AbstractCommand
{
  private static final String URL =
    "http://www.jarvana.com/jarvana/search?search_type=project&project=";

  private static final String GROUP_ID = "Group Id";
  private static final String ARTIFACT_ID = "Artifact Id";
  private static final String VERSION = "Version";

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
    Source source = new Source(new URL(URL + query));
    Element table = source.getElementById("resulttable");

    // get header column indexes
    int groupIndex = -1;
    int artifactIndex = -1;
    int versionIndex = -1;

    List<Element> ths = table.getAllElements("th");
    for (int ii = 0; ii < ths.size(); ii++){
      Element th = ths.get(ii);
      String text = th.getTextExtractor().toString().trim();
      if(groupIndex == -1 && GROUP_ID.equals(text)){
        groupIndex = ii;
      }else if(artifactIndex == -1 && ARTIFACT_ID.equals(text)){
        artifactIndex = ii;
      }else if(versionIndex == -1 && VERSION.equals(text)){
        versionIndex = ii;
      }

      if(groupIndex >= 0 && artifactIndex >= 0 && versionIndex >= 0){
        break;
      }
    }

    Iterator<Element> rows = table.getAllElements("tr").iterator();
    // skip header row
    rows.next();

    ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
    while (rows.hasNext()){
      Element row = rows.next();
      List<Element> cells = row.getAllElements("td");

      Dependency dependency = new Dependency();
      dependency.setGroupId(
          cells.get(groupIndex).getTextExtractor().toString().trim());
      dependency.setArtifactId(
          cells.get(artifactIndex).getTextExtractor().toString().trim());
      dependency.setVersion(
          cells.get(versionIndex).getTextExtractor().toString().trim());
      dependencies.add(dependency);
    }

    return dependencies;
  }
}
