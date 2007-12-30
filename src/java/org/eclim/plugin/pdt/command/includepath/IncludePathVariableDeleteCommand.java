/**
 * Copyright (c) 2005 - 2008
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
