/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.command.xml.validate;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.util.XmlUtils;

/**
 * Command to validate a xml file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ValidateCommand
  extends AbstractCommand
{
  private static final String NO_GRAMMER = "no grammar found";
  private static final String DOCTYPE_ROOT_NULL = "DOCTYPE root \"null\"";

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);

      List errors = validate(file);

      return filter(_commandLine, errors.toArray(new Error[errors.size()]));
    }catch(Throwable t){
      return t;
    }
  }

  /**
   * Validate the given file and return a list of any errors.
   *
   * @param _file The file to validate.
   * @return List of any errors.
   */
  protected List validate (String _file)
    throws Exception
  {
    Error[] errors = XmlUtils.validateXml(_file);
    ArrayList list = new ArrayList();
    for(int ii = 0; ii < errors.length; ii++){
      // FIXME: hack to ignore errors regarding no defined dtd.
      // When 1.4 no longer needs to be supported, this can be scrapped.
      if (errors[ii].getMessage().indexOf(NO_GRAMMER) == -1 &&
          errors[ii].getMessage().indexOf(DOCTYPE_ROOT_NULL) == -1)
      {
        list.add(errors[ii]);
      }
    }
    return list;
  }
}
