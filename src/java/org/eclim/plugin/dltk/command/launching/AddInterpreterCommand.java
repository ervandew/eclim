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

import java.util.ArrayList;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.dltk.preference.DltkInterpreterTypeManager;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.core.environment.IFileHandle;

import org.eclipse.dltk.debug.ui.interpreters.InterpretersUpdater;

import org.eclipse.dltk.internal.debug.ui.interpreters.EnvironmentVariableContentProvider;

import org.eclipse.dltk.internal.launching.LazyFileHandle;

import org.eclipse.dltk.launching.EnvironmentVariable;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.InterpreterStandin;
import org.eclipse.dltk.launching.LibraryLocation;
import org.eclipse.dltk.launching.ScriptRuntime;

import org.eclipse.dltk.utils.PlatformFileUtils;

/**
 * Command to add a new interpreter.
 *
 * Majority of the code here was gleaned from
 * org.eclipse.dltk.internal.debug.ui.interpreters.AddScriptInterpreterDialog
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "dltk_add_interpreter",
  options =
    "REQUIRED n nature ARG," +
    "REQUIRED t type ARG," +
    "REQUIRED i interpreter ARG"
)
public class AddInterpreterCommand
  extends AbstractCommand
{
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

    IInterpreterInstallType type = getInterpreterInstallType(nature, commandLine);
    if (type == null){
      throw new RuntimeException(
          Services.getMessage("interpreter.type.not.found"));
    }

    IEnvironment env = EnvironmentManager.getLocalEnvironment();
    IFileHandle file = PlatformFileUtils
      .findAbsoluteOrEclipseRelativeFile(env, new Path(interpreterPath));
    if (!file.exists()){
      throw new RuntimeException(
          Services.getMessage("interpreter.path.not.found", interpreterPath));
    }

    IStatus status = type.validateInstallLocation(file);
    if (!status.isOK()){
      throw new RuntimeException(status.getMessage());
    }

    IInterpreterInstall deflt =
      ScriptRuntime.getDefaultInterpreterInstall(
          new ScriptRuntime.DefaultInterpreterEntry(nature, env.getId()));
    if (deflt != null && deflt.getInstallLocation().equals(file)){
      throw new RuntimeException(
          Services.getMessage("interpreter.already.exists", interpreterPath));
    }

    ArrayList<IInterpreterInstall> interpreters =
      new ArrayList<IInterpreterInstall>();
    IInterpreterInstallType[] types =
      ScriptRuntime.getInterpreterInstallTypes(nature);

    for (IInterpreterInstallType iit : types){
      IInterpreterInstall[] installs = iit.getInterpreterInstalls();
      for (IInterpreterInstall install : installs){
        if (install.getInstallLocation().equals(file)){
          throw new RuntimeException(
              Services.getMessage("interpreter.already.exists", interpreterPath));
        }
        interpreters.add(install);
      }
    }

    String id = null;
    do {
      id = String.valueOf(System.currentTimeMillis());
    } while (type.findInterpreterInstall(id) != null);

    String name = generateInterpreterName(file, nature);
    EnvironmentVariableContentProvider envVarsProvider =
      new EnvironmentVariableContentProvider();
    EnvironmentVariable[] envVars = envVarsProvider.getVariables();
    LibraryLocation[] libs = type.getDefaultLibraryLocations(
        file, envVars, new NullProgressMonitor());

    IInterpreterInstall install = new InterpreterStandin(type, id);
    install.setInstallLocation(
        new LazyFileHandle(env.getId(), new Path(interpreterPath)));
    install.setName(name);
    install.setLibraryLocations(libs);
    install.setInterpreterArgs(null); // future use
    install = ((InterpreterStandin)install).convertToRealInterpreter();
    interpreters.add(install);

    if(deflt == null){
      deflt = install;
      ScriptRuntime.setDefaultInterpreterInstall(install, null, false);
    }

    IInterpreterInstall[] defaults = {deflt};
    IInterpreterInstall[] installs =
      interpreters.toArray(new IInterpreterInstall[interpreters.size()]);
    InterpretersUpdater updater = new InterpretersUpdater();
    updater.updateInterpreterSettings(nature, installs, defaults);

    return Services.getMessage("interpreter.added");
  }

  protected IInterpreterInstallType getInterpreterInstallType(
      String nature, CommandLine commandLine)
    throws Exception
  {
    String interpreterType = commandLine.getValue(Options.TYPE_OPTION);
    return DltkInterpreterTypeManager
      .getInterpreterInstallType(interpreterType, nature);
  }

  // mostly ripped off from eclipse.
  protected String generateInterpreterName(IFileHandle file, String nature)
  {
    final String genName;
    final IPath path = new Path(file.getCanonicalPath());
    if (path.segmentCount() > 0) {
      genName = path.lastSegment();
    } else {
      genName = null;
    }
    // Add number if interpreter with such name already exists.
    String name = genName;
    if (name != null) {
      int index = 0;
      while (!validateGeneratedName(name, nature)) {
        name = genName + "(" + String.valueOf(++index) + ")";
      }
    }
    return name;
  }

  protected boolean validateGeneratedName(String name, String nature)
  {
    IInterpreterInstallType[] types =
      ScriptRuntime.getInterpreterInstallTypes(nature);
    for (IInterpreterInstallType type : types){
      IInterpreterInstall install = type.findInterpreterInstallByName(name);
      if (install != null) {
        return false;
      }
    }
    return true;
  }
}
