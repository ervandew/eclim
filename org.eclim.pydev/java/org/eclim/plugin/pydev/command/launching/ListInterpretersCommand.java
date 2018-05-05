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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;

import org.python.pydev.ast.interpreter_managers.InterpreterManagersAPI;

import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;

/**
 * Command to list available python interpreters
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "python_list_interpreters")
public class ListInterpretersCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<HashMap<String,String>> results =
      new ArrayList<HashMap<String,String>>();

    IInterpreterManager manager =
      InterpreterManagersAPI.getPythonInterpreterManager();
    IInterpreterInfo[] interpreters = manager.getInterpreterInfos();
    for (IInterpreterInfo interpreter : interpreters){
      HashMap<String,String> result = new HashMap<String,String>();
      results.add(result);
      result.put("name", interpreter.getName());
      result.put("version", interpreter.getVersion());
      result.put("path", interpreter.getExecutableOrJar());
    }

    Collections.sort(results, new Comparator<HashMap<String,String>>(){
      public int compare(HashMap<String,String> o1, HashMap<String,String> o2){
        return o1.get("name").compareTo(o2.get("name"));
      }
    });
    return results;
  }
}
