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
package org.eclim.plugin.pdt.command.includepath;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

import org.eclipse.php.internal.core.project.IIncludePathEntry;

import org.eclipse.php.internal.core.project.options.PHPProjectOptions;

/**
 * Command to retrieve list of include paths for the given project.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class IncludePathsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    PHPProjectOptions options = PHPProjectOptions.forProject(project);

    ArrayList<String> paths = new ArrayList<String>();
    if (options != null){
      IIncludePathEntry[] entries = options.readRawIncludePath();
      for(IIncludePathEntry entry : entries){
        if (entry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE){
          IPath path =
            PHPProjectOptions.getResolvedVariablePath(entry.getPath());
          paths.add(path.toOSString());
        }else if (entry.getEntryKind() == IIncludePathEntry.IPE_PROJECT){
          paths.add(entry.getResource().getLocation().toOSString());
        /*}else if (entry.getEntryKind() == IIncludePathEntry.IPE_CONTAINER){
          IIncludePathContainer container =
            PHPProjectOptions.getIncludePathContainer(entry.getPath(), project)
          // still need resolution... the following probably won't work.
          paths.add(container.getPath().toOSString());*/
        }else{
          paths.add(entry.getPath().toOSString());
        }
      }
    }

    return StringUtils.join(paths.iterator(), "\n");
  }
}
