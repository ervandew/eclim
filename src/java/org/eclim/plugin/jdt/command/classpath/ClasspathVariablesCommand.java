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
package org.eclim.plugin.jdt.command.classpath;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclipse.jdt.core.JavaCore;

/**
 * Command to work with classpath variables.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ClasspathVariablesCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    List results = new ArrayList();
    try{
      String[] names = JavaCore.getClasspathVariableNames();
      for(int ii = 0; ii < names.length; ii++){
        ClasspathVariable variable = new ClasspathVariable();
        variable.setName(names[ii]);
        variable.setPath(
            JavaCore.getClasspathVariable(names[ii]).toOSString());
        results.add(variable);
      }
      return filter(_commandLine, results);
    }catch(Exception e){
      return e;
    }
  }
}
