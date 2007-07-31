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

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

import org.eclipse.php.internal.core.project.IIncludePathEntry;

import org.eclipse.php.internal.core.project.options.PHPProjectOptions;

/**
 * Command to retrieve list of include paths for the given project.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class IncludePathsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String projectName = _commandLine.getValue(Options.PROJECT_OPTION);
      IProject project = ProjectUtils.getProject(projectName);
      PHPProjectOptions options = PHPProjectOptions.forProject(project);

      ArrayList<String> paths = new ArrayList<String>();
      if (options != null){
        IIncludePathEntry[] entries = options.readRawIncludePath();
        for(IIncludePathEntry entry : entries){
          if (entry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE){
            IPath path =
              PHPProjectOptions.getResolvedVariablePath(entry.getPath());
            paths.add(path.toOSString());
          }else if (entry.getEntryKind() == IIncludePathEntry.IPE_PROJECT){
            paths.add(entry.getResource().getLocation().toOSString());
          /*}else if (entry.getEntryKind() == IIncludePathEntry.IPE_CONTAINER){
            IIncludePathContainer container =
              PHPProjectOptions.getIncludePathContainer(entry.getPath(), project)
            // still need resolution... the following probably won't work.
            paths.add(container.getPath().toOSString());*/
          }else{
            paths.add(entry.getPath().toOSString());
          }
        }
      }

      return StringUtils.join(paths.iterator(), "\n");
    }catch(Exception e){
      return e;
    }
  }
}
