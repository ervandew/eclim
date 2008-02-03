/**
 * Copyright (c) 2005 - 2008
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
package org.eclim.plugin.pdt.command.search;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.command.filter.PositionFilter;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;

import org.eclipse.php.internal.core.phpModel.phpElementData.CodeData;

import org.eclipse.php.internal.core.util.CodeDataResolver;

/**
 * Command to find the definition of a php element.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class FindDefinitionCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    int offset = Integer.parseInt(_commandLine.getValue(Options.OFFSET_OPTION));

    IFile ifile = ProjectUtils.getFile(project, file);

    CodeData[] codeDatas = CodeDataResolver.getInstance().resolve(ifile, offset);

    IWorkspaceRoot root = project.getWorkspace().getRoot();

    List<Position> results = new ArrayList<Position>();
    for (CodeData data : codeDatas) {
      if (data.isUserCode()) {
        String filename = data.getUserData().getFileName();
        IResource resource = root.findMember(filename);
        if(resource != null){
          filename = resource.getRawLocation().toOSString();
          Position position = new Position(
              filename, data.getUserData().getStartPosition(), 0);
          position.setMessage(data.getName());
          results.add(position);
        }
      }
    }
    return PositionFilter.instance.filter(_commandLine, results);
  }
}
