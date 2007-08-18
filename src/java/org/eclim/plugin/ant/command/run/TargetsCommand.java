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
package org.eclim.plugin.ant.command.run;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.Project;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.ant.util.AntUtils;

import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.AntProjectNode;

/**
 * Command to handle request for available tasks in an ant build file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class TargetsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    ArrayList<String> results = new ArrayList<String>();
    String project = _commandLine.getValue(Options.PROJECT_OPTION);
    String file = _commandLine.getValue(Options.FILE_OPTION);

    AntModel model = (AntModel)AntUtils.getAntModel(project, file);
    AntProjectNode projectNode = model.getProjectNode(true);
    Project antProject = projectNode.getProject();

    Map targets = antProject.getTargets();
    for (Iterator ii = targets.keySet().iterator(); ii.hasNext();){
      String target = (String)ii.next();
      if(target.trim().length() > 0){
        results.add(target);
      }
    }

    Collections.sort(results, Collator.getInstance());
    return TargetsFilter.instance.filter(_commandLine, results);
  }
}
