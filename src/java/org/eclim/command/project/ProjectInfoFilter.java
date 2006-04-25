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

import java.util.Iterator;
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

import org.eclim.command.admin.SettingsFilter;

import org.eclim.preference.OptionInstance;

/**
 * Output filter for project info.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectInfoFilter
  extends SettingsFilter
{
  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, Object _result)
  {
    List list = (List)_result;
    if(list.size() > 0){
      // list of project's current settings.
      if(list.get(0) instanceof OptionInstance){
        return printOptions(list);

      // list of all projects.
      }else{
        return printProjects(list);
      }
    }

    return "";
  }

  /**
   * Print supplied list of projects.
   *
   * @param _projects The project list.
   * @return The result.
   */
  protected String printProjects (List _projects)
  {
    StringBuffer buffer = new StringBuffer();
    for(Iterator ii = _projects.iterator(); ii.hasNext();){
      if(buffer.length() > 0){
        buffer.append('\n');
      }
      buffer.append(ii.next());
    }
    return buffer.toString();
  }
}
