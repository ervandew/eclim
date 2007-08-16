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
package org.eclim.command.admin;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.preference.Option;

/**
 * Command to obtain global settings.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SettingsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      ArrayList<Option> results = new ArrayList<Option>();

      String setting = _commandLine.getValue(Options.SETTING_OPTION);
      Option[] options = getPreferences().getOptions();

      // only retrieving the requested setting.
      if(setting != null){
        for(int ii = 0; ii < options.length; ii++){
          if(options[ii].getName().equals(setting)){
            results.add(options[ii]);
            break;
          }
        }

      // retrieve all settings.
      }else{
        results.addAll(Arrays.asList(options));
      }
     return filter(_commandLine, results);
    }catch(Throwable t){
      return t;
    }
  }
}
