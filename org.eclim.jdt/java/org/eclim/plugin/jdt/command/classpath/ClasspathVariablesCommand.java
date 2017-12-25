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
package org.eclim.plugin.jdt.command.classpath;

import java.util.ArrayList;
import java.util.Collections;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.JavaCore;

/**
 * Command to work with classpath variables.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "java_classpath_variables")
public class ClasspathVariablesCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<ClasspathVariable> results = new ArrayList<ClasspathVariable>();
    String[] names = JavaCore.getClasspathVariableNames();
    for(int ii = 0; ii < names.length; ii++){
      IPath path = JavaCore.getClasspathVariable(names[ii]);
      if(path != null){
        ClasspathVariable variable = new ClasspathVariable();
        variable.setName(names[ii]);
        variable.setPath(path.toOSString());
        results.add(variable);
      }
    }
    Collections.sort(results);
    return results;
  }
}
