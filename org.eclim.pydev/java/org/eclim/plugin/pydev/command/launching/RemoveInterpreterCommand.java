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
  name = "python_remove_interpreter",
  options = "REQUIRED p path ARG"
)
public class RemoveInterpreterCommand
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
    if (skip.contains(path)){
      IInterpreterInfo[] updated = new IInterpreterInfo[existing.length - 1];
      int index = 0;
      for (IInterpreterInfo info : existing){
        if (info.getExecutableOrJar().equals(path)){
          continue;
        }
        updated[index++] = info;
      }
      manager.setInfos(updated, skip, new NullProgressMonitor());
      return Services.getMessage("python.interpreter.removed", path);
    }
    return Services.getMessage("python.interpreter.not.found", path);
  }
}
