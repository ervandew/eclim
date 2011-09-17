/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.dltk.command.launching;

import java.util.ArrayList;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.dltk.util.DltkUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.IEnvironment;

import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.ScriptRuntime;

/**
 * Command to list the currently available interpreters.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "dltk_interpreters",
  options =
    "OPTIONAL p project ARG," +
    "OPTIONAL n nature ARG"
)
public class InterpretersCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String nature = commandLine.getValue(Options.NATURE_OPTION);

    if (projectName == null && nature == null){
      throw new RuntimeException(
          Services.getMessage("interpreters.arg.required"));
    }

    IEnvironment env = null;
    ArrayList<String> natures = new ArrayList<String>();
    if (projectName != null){
      IProject project = ProjectUtils.getProject(projectName, true);
      env = EnvironmentManager.getEnvironment(project);
      for (String n : DltkUtils.getDltkNatures()){
        if (project.hasNature(n)){
          natures.add(n);
        }
      }
    }else{
      nature = ProjectNatureFactory.getNatureForAlias(nature);
      if (nature == null){
        throw new RuntimeException(
            Services.getMessage("nature.alias.not.found",
              commandLine.getValue(Options.NATURE_OPTION)));
      }
      natures.add(nature);
      env = EnvironmentManager.getLocalEnvironment();
    }

    StringBuffer result = new StringBuffer();

    for (String natureId : natures){
      ArrayList<IInterpreterInstall> interpreters =
        new ArrayList<IInterpreterInstall>();

      IInterpreterInstall deflt =
        ScriptRuntime.getDefaultInterpreterInstall(
            new ScriptRuntime.DefaultInterpreterEntry(natureId, env.getId()));

      int length = 0;

      if (deflt != null){
        length = deflt.getName().length() + 10;
      }

      IInterpreterInstallType[] types =
        ScriptRuntime.getInterpreterInstallTypes(natureId);

      for (IInterpreterInstallType type : types){
        IInterpreterInstall[] installs = type.getInterpreterInstalls();
        for (IInterpreterInstall install : installs){
          if (!install.equals(deflt)){
            interpreters.add(install);
            if (install.getName().length() > length){
              length = install.getName().length();
            }
          }
        }
      }

      if (result.length() > 0){
        result.append('\n');
      }

      if (deflt != null || interpreters.size() > 0){
        result.append("Nature: ")
          .append(ProjectNatureFactory.getAliasForNature(natureId));
        if (deflt != null){
          result
            .append("\n\t")
            .append(StringUtils.rightPad(deflt.getName() + " (default)", length))
            .append(" - ")
            .append(deflt.getInstallLocation().getPath().toOSString());
        }

        for (IInterpreterInstall interpreter : interpreters){
          result.append("\n\t");
          result
            .append(StringUtils.rightPad(interpreter.getName(), length))
            .append(" - ")
            .append(interpreter.getInstallLocation().getPath().toOSString());
        }
      }
    }
    return result.toString();
  }
}
