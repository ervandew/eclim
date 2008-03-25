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

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.php.internal.core.project.options.PHPProjectOptions;

/**
 * Command to create an include path variable.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class IncludePathVariableCreateCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String name = _commandLine.getValue(Options.NAME_OPTION);
    String path = _commandLine.getValue(Options.PATH_OPTION);

    String[] existing = PHPProjectOptions.getIncludePathVariableNames();
    String[] names = new String[existing.length + 1];
    IPath[] paths = new IPath[existing.length + 1];

    for (int ii = 0; ii < existing.length; ii++){
      names[ii] = existing[ii];
      paths[ii] = PHPProjectOptions.getIncludePathVariable(names[ii]);
    }
    names[names.length - 1] = name;
    paths[paths.length - 1] = new Path(path);

    PHPProjectOptions.setIncludePathVariables(names, paths, null);

    IScopeContext context = new InstanceScope();
    IEclipsePreferences preferences = context.getNode("org.eclipse.php.core");
    preferences.flush();

    return Services.getMessage("includepath.variable.created", name);
  }
}
