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
package org.eclim.plugin.dltk.command.buildpath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;

/**
 * Command to retrieve list of build paths for the given project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "dltk_buildpaths",
  options = "REQUIRED p project ARG"
)
public class BuildpathsCommand
  extends AbstractCommand
{
  private static final String LOCAL_ENV =
    "org.eclipse.dltk.core.environment.localEnvironment/:";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    IScriptProject scriptProject = DLTKCore.create(project);

    ArrayList<String> paths = new ArrayList<String>();
    HashSet<IScriptProject> visited = new HashSet<IScriptProject>();

    collect(scriptProject, paths, visited);
    return paths;
  }

  private void collect(
      IScriptProject scriptProject,
      List<String> paths,
      Set<IScriptProject> visited)
    throws Exception
  {
    if (visited.contains(scriptProject)){
      return;
    }
    visited.add(scriptProject);

    IBuildpathEntry[] entries = scriptProject.getResolvedBuildpath(true);
    for (IBuildpathEntry entry : entries){
      // NOTE: org.eclipse.dltk.internal.core.BuildpathEntry.elementDecode
      // currently doesn't support kind="var"
      switch (entry.getEntryKind()) {
        case IBuildpathEntry.BPE_CONTAINER:
        case IBuildpathEntry.BPE_LIBRARY:
        case IBuildpathEntry.BPE_VARIABLE:
          String path = entry.getPath().toOSString();
          if (path.startsWith(LOCAL_ENV)){
            path = path.replaceFirst(LOCAL_ENV, "");
          }
          if (!paths.contains(path)){
            paths.add(path);
          }
          break;
        case IBuildpathEntry.BPE_SOURCE:
          paths.add(ProjectUtils.getFilePath(
                scriptProject.getProject(), entry.getPath().toOSString()));
          break;
        case IBuildpathEntry.BPE_PROJECT:
          IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(entry.getPath().segment(0));
          if (project != null){
            collect(DLTKCore.create(project), paths, visited);
          }
          break;
      }
    }
  }
}
