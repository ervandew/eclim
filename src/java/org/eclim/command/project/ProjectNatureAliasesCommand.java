/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.command.project;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.apache.commons.lang.StringUtils;

import org.eclim.project.ProjectNatureFactory;

/**
 * Command to get available project nature aliases.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectNatureAliasesCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String natures = StringUtils.join(ProjectNatureFactory.getNatureAliases(), '\n');
      return natures + '\n' + ProjectNatureFactory.NONE;
    }catch(Throwable t){
      return t;
    }
  }
}
