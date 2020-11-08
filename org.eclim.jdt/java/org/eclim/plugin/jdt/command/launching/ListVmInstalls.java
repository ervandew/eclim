/**
 * Copyright (C) 2011 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.launching;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Command which lists the available jvm installs grouped by their install type.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "java_list_installs")
public class ListVmInstalls
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<HashMap<String, Object>> results =
      new ArrayList<HashMap<String, Object>>();

    AbstractVMInstall defaultInstall =
      (AbstractVMInstall)JavaRuntime.getDefaultVMInstall();

    IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
    for (IVMInstallType type : types){
      IVMInstall[] installs = type.getVMInstalls();
      if (installs.length > 0){
        for (IVMInstall iinstall : installs){
          AbstractVMInstall install = (AbstractVMInstall)iinstall;
          HashMap<String, Object> result = new HashMap<String, Object>();
          results.add(result);
          result.put("type", type.getName());
          result.put("name", install.getName());
          result.put("dir", install.getInstallLocation().getPath());
          result.put("version", install.getJavaVersion());
          result.put("args", install.getVMArgs());
          result.put("default", install.equals(defaultInstall));
        }
      }
    }
    return results;
  }
}
