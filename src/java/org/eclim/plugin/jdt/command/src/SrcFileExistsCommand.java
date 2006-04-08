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
package org.eclim.plugin.jdt.command.src;

import java.io.IOException;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Command to determines if the specified src file exists.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SrcFileExistsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

      if(projectName != null){
        ICompilationUnit src = JavaUtils.findCompilationUnit(projectName, file);
        return src != null && src.exists() ? Boolean.TRUE : Boolean.FALSE;
      }
      ICompilationUnit src = JavaUtils.findCompilationUnit(file);
      return src != null && src.exists() ? Boolean.TRUE : Boolean.FALSE;
    }catch(IllegalArgumentException iae){
      return Boolean.FALSE;
    }catch(Exception e){
      return e;
    }
  }
}
