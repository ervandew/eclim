/**
 * Copyright (C) 2012 - 2018 Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.launching;

import java.util.HashSet;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.python.pydev.ast.interpreter_managers.InterpreterManagersAPI;

import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;

/**
 * Command to add a python interpreter.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "python_add_interpreter",
  options =
    "REQUIRED p path ARG," +
    "OPTIONAL n name ARG"
)
public class AddInterpreterCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    IInterpreterManager manager =
      InterpreterManagersAPI.getPythonInterpreterManager();
    IInterpreterInfo[] existing = manager.getInterpreterInfos();
    HashSet<String> skip = new HashSet<String>();
    for (IInterpreterInfo info : existing){
      skip.add(info.getExecutableOrJar());
    }

    String path = commandLine.getValue(Options.PATH_OPTION);
    if (!skip.contains(path)){
      IInterpreterInfo info = manager.createInterpreterInfo(
          path, new NullProgressMonitor(), false);
      if (commandLine.hasOption(Options.NAME_OPTION)){
        info.setName(commandLine.getValue(Options.NAME_OPTION));
      }else{
        info.setName(FileUtils.getBaseName(path).replace(".exe", ""));
      }

      IInterpreterInfo[] updated = new IInterpreterInfo[existing.length + 1];
      System.arraycopy(existing, 0, updated, 0, existing.length);
      updated[updated.length - 1] = info;

      manager.setInfos(updated, skip, new NullProgressMonitor());
      return Services.getMessage("python.interpreter.added", path);
    }
    return Services.getMessage("python.interpreter.exists", path);
  }
}
