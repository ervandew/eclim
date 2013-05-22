/**
 * Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
import java.util.HashMap;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.dltk.util.DltkUtils;

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
    "OPTIONAL l nature ARG"
)
public class InterpretersCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String alias = commandLine.getValue(Options.LANG_OPTION);

    if (projectName == null && alias == null){
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
      String nature = ProjectNatureFactory.getNatureForAlias(alias);
      if (nature == null){
        throw new RuntimeException(
            Services.getMessage("nature.alias.not.found", alias));
      }
      natures.add(nature);
      env = EnvironmentManager.getLocalEnvironment();
    }

    ArrayList<HashMap<String,Object>> interpreters =
      new ArrayList<HashMap<String,Object>>();
    for (String natureId : natures){
      IInterpreterInstall deflt =
        ScriptRuntime.getDefaultInterpreterInstall(
            new ScriptRuntime.DefaultInterpreterEntry(natureId, env.getId()));
      if (deflt != null){
        HashMap<String,Object> defaultInt = new HashMap<String,Object>();
        defaultInt.put("nature", ProjectNatureFactory.getAliasForNature(natureId));
        defaultInt.put("name", deflt.getName());
        defaultInt.put("path", deflt.getInstallLocation().getPath().toOSString());
        defaultInt.put("default", true);
        interpreters.add(defaultInt);
      }

      IInterpreterInstallType[] types =
        ScriptRuntime.getInterpreterInstallTypes(natureId);

      for (IInterpreterInstallType type : types){
        IInterpreterInstall[] installs = type.getInterpreterInstalls();
        for (IInterpreterInstall install : installs){
          if (!install.equals(deflt)){
            HashMap<String,Object> inter = new HashMap<String,Object>();
            inter.put("nature", ProjectNatureFactory.getAliasForNature(natureId));
            inter.put("name", install.getName());
            inter.put("path", install.getInstallLocation().getPath().toOSString());
            inter.put("default", false);
            interpreters.add(inter);
          }
        }
      }
    }
    return interpreters;
  }
}
