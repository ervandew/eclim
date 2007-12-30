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
package org.eclim.plugin.jdt.command.classpath;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

/**
 * Command to add a classpath variable.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ClasspathVariableCreateCommand
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

    JavaCore.setClasspathVariable(name, new Path(path), null);

    return Services.getMessage("classpath.variable.created", name);
  }
}
