/**
 * Copyright (c) 2005 - 2007
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  public Object execute (CommandLine _commandLine)
  {
    try{
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
    }catch(Exception e){
      return e;
    }
  }
}
