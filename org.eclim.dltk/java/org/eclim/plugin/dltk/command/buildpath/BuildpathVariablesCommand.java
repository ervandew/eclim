/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.dltk.command.buildpath;

import java.util.ArrayList;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.core.runtime.IPath;

import org.eclipse.dltk.core.DLTKCore;

/**
 * Command to list defined build path variables.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "dltk_buildpath_variables")
public class BuildpathVariablesCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<BuildpathVariable> results = new ArrayList<BuildpathVariable>();
    String[] names = DLTKCore.getBuildpathVariableNames();
    for(int ii = 0; ii < names.length; ii++){
      IPath path = DLTKCore.getBuildpathVariable(names[ii]);
      if(path != null){
        BuildpathVariable variable = new BuildpathVariable();
        variable.setName(names[ii]);
        variable.setPath(path.toOSString());
        results.add(variable);
      }
    }
    return results;
  }
}
