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
package org.eclim.plugin.dltk.command.launching;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.project.ProjectNatureFactory;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.core.environment.IFileHandle;

import org.eclipse.dltk.internal.launching.LazyFileHandle;

import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.InterpreterStandin;
import org.eclipse.dltk.launching.LibraryLocation;
import org.eclipse.dltk.launching.ScriptRuntime;

import org.eclipse.dltk.utils.PlatformFileUtils;

/**
 * Command to add a new interpreter.
 *
 * Majority of the code hear was gleaned from
 * org.eclipse.dltk.internal.debug.ui.interpreters.AddScriptInterpreterDialog
 *
 * Note: this command is incomplete and probably uncessary, but keeping it
 * around since it took quite a bit of digging just to get it where it is now.
 *
 * @author Eric Van Dewoestine
 */
/*@Command(
  name = "dltk_add_interpreter",
  options =
    "REQUIRED n nature ARG," +
    "REQUIRED t type ARG," +
    "REQUIRED i interpreter ARG"
)*/
public class AddInterpreterCommand
  extends AbstractCommand
{
  private static final String RUBY =
    "org.eclipse.dltk.internal.debug.ui.launcher.GenericRubyInstallType";
  private static final String JRUBY =
    "org.eclipse.dltk.ruby.internal.launching.JRubyInstallType";

  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String nature = commandLine.getValue(Options.NATURE_OPTION);
    nature = ProjectNatureFactory.getNatureForAlias(nature);
    if (nature == null){
      throw new RuntimeException(
          Services.getMessage("nature.alias.not.found",
            commandLine.getValue(Options.NATURE_OPTION)));
    }

    String interpreterPath = commandLine.getValue("i");
    String interpreterType = commandLine.getValue(Options.TYPE_OPTION);
    if(interpreterType.equals("ruby")){
      interpreterType = RUBY;
    }else if(interpreterType.equals("jruby")){
      interpreterType = JRUBY;
    }

    IInterpreterInstallType[] types =
      ScriptRuntime.getInterpreterInstallTypes(nature);
    IEnvironment env = EnvironmentManager.getLocalEnvironment();
    IInterpreterInstallType type = null;
    for (IInterpreterInstallType iit : types){
      if (interpreterType.equals(iit.getId())){
        type = iit;
        break;
      }
    }

    if (type == null){
      throw new RuntimeException(
          "Interpreter type '" + interpreterType + "' not found.");
    }

    IFileHandle file = PlatformFileUtils
      .findAbsoluteOrEclipseRelativeFile(env, new Path(interpreterPath));
    if (!file.exists()){
      throw new RuntimeException(
          "Interpreter not found at '" + interpreterPath + "'.");
    }

    String id = null;
    do {
      id = String.valueOf(System.currentTimeMillis());
    } while (type.findInterpreterInstall(id) != null);

    IInterpreterInstall install = new InterpreterStandin(type, id);
    install.setInstallLocation(new LazyFileHandle(env.getId(),
        new Path(interpreterPath)));
    install.setName("rubyTest");

    LibraryLocation[] libs =
      type.getDefaultLibraryLocations(file, null, new NullProgressMonitor());
    install.setLibraryLocations(libs);

    //ScriptRuntime.fireInterpreterAdded(install);

    return "Interpreter added: " + interpreterPath;
  }
}
