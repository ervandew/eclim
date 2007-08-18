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

import java.util.ArrayList;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclipse.core.runtime.IPath;

import org.eclipse.php.internal.core.project.options.PHPProjectOptions;

/**
 * Command to list defined include path variables.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class IncludePathVariablesCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    ArrayList<IncludePathVariable> results =
      new ArrayList<IncludePathVariable>();
    String[] names = PHPProjectOptions.getIncludePathVariableNames();
    for(String name : names){
      IPath path = PHPProjectOptions.getIncludePathVariable(name);
      if(path != null){
        IncludePathVariable variable = new IncludePathVariable();
        variable.setName(name);
        variable.setPath(path.toOSString());
        results.add(variable);
      }
    }
    return IncludePathVariablesFilter.instance.filter(_commandLine, results);
  }
}
