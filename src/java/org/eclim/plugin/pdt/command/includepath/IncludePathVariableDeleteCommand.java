/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.plugin.pdt.command.includepath;

import java.util.ArrayList;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.CollectionUtils;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.php.internal.core.project.options.PHPProjectOptions;

/**
 * Command to delete an include path variable.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class IncludePathVariableDeleteCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String name = _commandLine.getValue(Options.NAME_OPTION);

    ArrayList<String> existing = new ArrayList<String>();
    CollectionUtils.addAll(
        existing, PHPProjectOptions.getIncludePathVariableNames());
    if(!existing.contains(name)){
      throw new RuntimeException(
          Services.getMessage("variable.not.found", name));
    }

    existing.remove(name);
    String[] names = (String[])existing.toArray(new String[existing.size()]);
    IPath[] paths = new IPath[names.length];
    for(int ii = 0; ii < names.length; ii++){
      paths[ii] = PHPProjectOptions.getIncludePathVariable(names[ii]);
    }

    PHPProjectOptions.setIncludePathVariables(names, paths, null);

    IScopeContext context = new InstanceScope();
    IEclipsePreferences preferences = context.getNode("org.eclipse.php.core");
    preferences.flush();

    return Services.getMessage("includepath.variable.deleted", name);
  }
}
