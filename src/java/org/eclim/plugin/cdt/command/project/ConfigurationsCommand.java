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
package org.eclim.plugin.cdt.command.project;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

/**
 * Command to obtain the current configuration (source entries, include
 * locations, etc.) for the specified project.
 *
 * @author Eric Van Dewoestine
 */
public class ConfigurationsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    ICProjectDescription desc =
      CCorePlugin.getDefault().getProjectDescription(project, false);
    ICConfigurationDescription[] configs = desc.getConfigurations();

    StringBuffer out = new StringBuffer();
    for(ICConfigurationDescription config : configs){
      out.append("Config: ").append(config.getName()).append('\n');

      ICSourceEntry[] sources = config.getSourceEntries();
      if (sources.length > 0){
        out.append("\tSources:\n");
        for(ICSourceEntry entry : sources){
          out.append("\t\tdir:      ")
            .append(entry.getFullPath().removeFirstSegments(1));
          IPath[] excludes = entry.getExclusionPatterns();
          if (excludes.length > 0){
            out.append('\n');
            String[] patterns = new String[excludes.length];
            for (int ii = 0; ii < excludes.length; ii++){
              patterns[ii] = excludes[ii].toString();
            }
            out.append("\t\texcludes: ").append(StringUtils.join(patterns, ','));
          }
        }
      }
    }

    return out.toString();
  }
}
