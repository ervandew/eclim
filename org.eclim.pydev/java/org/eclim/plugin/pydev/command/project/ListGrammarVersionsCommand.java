/**
 * Copyright (C) 2013 - 2017 Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.project;

import java.util.ArrayList;
import java.util.Collections;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IPythonNature;

import org.python.pydev.plugin.nature.PythonNature;

/**
 * Command to list available grammar versions for a project based on its current
 * interpreter.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "python_list_grammars",
  options = "REQUIRED p project ARG"
)
public class ListGrammarVersionsCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    PythonNature nature = PythonNature.getPythonNature(project);
    ArrayList<String> grammars = new ArrayList<String>();
    if (nature != null){
      IInterpreterInfo interpreter = nature.getProjectInterpreter();
      if (interpreter != null){
        String version = interpreter.getVersion();
        String[] parts = StringUtils.split(version, ".");
        double iversion = Double.parseDouble(parts[0] + '.' + parts[1]);
        for (String grammar : IPythonNature.Versions.ALL_PYTHON_VERSIONS){
          grammar = grammar.replace("python ", "");
          if (grammar.equals("interpreter")){
            continue;
          }
          double gversion = Double.parseDouble(grammar);
          if (gversion <= iversion && grammar.charAt(0) == version.charAt(0)){
            grammars.add(grammar);
          }
        }
        Collections.sort(grammars);
      }
    }

    return grammars;
  }
}
