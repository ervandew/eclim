/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.cdt.command.project;

import java.util.ArrayList;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;

import org.eclipse.core.resources.IProject;

/**
 * Command to retrieve a list of source paths for a project.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "c_sourcepaths", options = "REQUIRED p project ARG")
public class SourcePathsCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    ICProject cproject = CoreModel.getDefault().create(project);
    ArrayList<String> results = new ArrayList<String>();
    for (ISourceRoot root : cproject.getSourceRoots()){
      results.add(ProjectUtils
        .getFilePath(project, root.getPath().toOSString()));
    }
    return results;
  }
}
