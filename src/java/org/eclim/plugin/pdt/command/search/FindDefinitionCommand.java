/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
          if(!results.contains(position)){
            results.add(position);
          }
        }
      }
    }
    return PositionFilter.instance.filter(_commandLine, results);
  }
}
