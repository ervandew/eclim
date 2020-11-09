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
package org.eclim.plugin.ant.command.run;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.ant.util.AntUtils;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.AntProjectNode;

/**
 * Command to handle request for available tasks in an ant build file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "ant_targets",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class TargetsCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<String> results = new ArrayList<String>();
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);

    AntModel model = (AntModel)AntUtils.getAntModel(project, file);
    AntProjectNode projectNode = model.getProjectNode(true);
    Project antProject = projectNode.getProject();

    Map<String, Target> targets = antProject.getTargets();
    for (String target : targets.keySet()){
      if(target.trim().length() > 0){
        results.add(target);
      }
    }

    Collections.sort(results, Collator.getInstance());
    return results;
  }
}
