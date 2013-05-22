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

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclipse.core.runtime.Path;

import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.core.environment.IFileHandle;

import org.eclipse.dltk.debug.ui.interpreters.InterpretersUpdater;

import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.ScriptRuntime;

import org.eclipse.dltk.utils.PlatformFileUtils;

/**
 * Command to remove an interpreter.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "dltk_remove_interpreter",
  options =
    "REQUIRED p path ARG," +
    "REQUIRED l nature ARG"
)
public class RemoveInterpreterCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String alias = getNatureAlias(commandLine);
    String nature = ProjectNatureFactory.getNatureForAlias(alias);
    if (nature == null){
      throw new RuntimeException(
          Services.getMessage("nature.alias.not.found", alias));
    }

    String interpreterPath = commandLine.getValue(Options.PATH_OPTION);

    IEnvironment env = EnvironmentManager.getLocalEnvironment();
    IFileHandle file = PlatformFileUtils
      .findAbsoluteOrEclipseRelativeFile(env, new Path(interpreterPath));

    IInterpreterInstall deflt =
      ScriptRuntime.getDefaultInterpreterInstall(
          new ScriptRuntime.DefaultInterpreterEntry(nature, env.getId()));
    if (deflt != null && deflt.getInstallLocation().equals(file)){
      deflt = null;
    }

    IInterpreterInstallType[] types =
      ScriptRuntime.getInterpreterInstallTypes(nature);

    boolean removed = false;
    ArrayList<IInterpreterInstall> interpreters =
      new ArrayList<IInterpreterInstall>();
    for (IInterpreterInstallType iit : types){
      IInterpreterInstall[] installs = iit.getInterpreterInstalls();
      for (IInterpreterInstall install : installs){
        if (!install.getInstallLocation().toOSString().equals(file.toOSString())){
          interpreters.add(install);
        }else{
          removed = true;
        }
      }
    }

    IInterpreterInstall[] defaults = deflt != null ?
      new IInterpreterInstall[]{deflt} : null;
    IInterpreterInstall[] installs =
      interpreters.toArray(new IInterpreterInstall[interpreters.size()]);
    InterpretersUpdater updater = new InterpretersUpdater();
    updater.updateInterpreterSettings(nature, installs, defaults);

    if (removed){
      return Services.getMessage("interpreter.removed");
    }
    return Services.getMessage("interpreter.not.found");
  }

  protected String getNatureAlias(CommandLine commandLine)
    throws Exception
  {
    return commandLine.getValue(Options.LANG_OPTION);
  }
}
