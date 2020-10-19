/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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

import java.net.URL;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.command.include.ImportUtils;

import org.eclim.plugin.jdt.command.search.SearchCommand;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.core.search.SearchMatch;

import org.eclipse.jdt.ui.JavaUI;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.help.IWorkbenchHelpSystem;

/**
 * Command to search for javadocs.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_docsearch",
  options =
    "REQUIRED n project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL l length ARG," +
    "OPTIONAL p pattern ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL x context ARG," +
    "OPTIONAL s scope ARG"
)
public class DocSearchCommand
  extends SearchCommand
{
  private static final Pattern JAR_URL = Pattern.compile(
      "^jar:(file|platform):.*\\.jar!/.*", Pattern.CASE_INSENSITIVE);

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    IProject project = ProjectUtils.getProject(
        commandLine.getValue(Options.NAME_OPTION));
    String pattern = commandLine.getValue(Options.PATTERN_OPTION);

    IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();

    ArrayList<String> results = new ArrayList<String>();
    for(SearchMatch match : executeSearch(commandLine)){
      IJavaElement element = (IJavaElement)match.getElement();

      if (element == null){
        continue;
      }

      // for pattern searches, honor import excludes
      if (pattern != null){
        String name = null;
        switch(element.getElementType()){
          case IJavaElement.TYPE:
            name = ((IType)element).getFullyQualifiedName();
            break;
          case IJavaElement.METHOD:
          case IJavaElement.FIELD:
            name = ((IType)element.getParent()).getFullyQualifiedName();
            break;
        }
        if (name != null){
          name = name.replace('$', '.');
          if (ImportUtils.isImportExcluded(project, name)){
            continue;
          }
        }
      }

      URL url = JavaUI.getJavadocLocation(element, true);
      if(url == null){
        continue;
      }

      // convert any jar urls to a usable eclipse url
      Matcher jarMatcher = JAR_URL.matcher(url.toExternalForm());
      if (jarMatcher.matches()){
        url = helpSystem.resolve(url.toExternalForm(), true);
      }

      results.add(url.toString());
    }
    return results;
  }
}
