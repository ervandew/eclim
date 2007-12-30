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
package org.eclim.plugin.jdt.command.src;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Attempts to find the source file for the specified class name in one of the
 * user's projects.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SrcFindCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String classname = _commandLine.getValue(Options.CLASSNAME_OPTION);
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

    ICompilationUnit src = null;
    String file = classname.replace('.', '/') + ".java";
    if(projectName != null){
      src = JavaUtils.findCompilationUnit(projectName, file);
    }else{
      src = JavaUtils.findCompilationUnit(file);
    }

    if (src != null && src.exists()){
      return src.getResource().getLocation().toOSString();
    }

    return "";
  }
}
