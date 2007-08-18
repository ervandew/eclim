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
package org.eclim.command.xml.validate;

import java.util.Iterator;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.util.XmlUtils;

import org.xml.sax.helpers.DefaultHandler;

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
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String project = _commandLine.getValue(Options.PROJECT_OPTION);
    String file = _commandLine.getValue(Options.FILE_OPTION);
    boolean schema = _commandLine.hasOption(Options.SCHEMA_OPTION);

    List<Error> list = validate(project, file, schema, null);

    return ErrorFilter.instance.filter(_commandLine, list);
  }

  /**
   * Validate the supplied file.
   *
   * @param _project The project name.
   * @param _file The file to validate.
   * @param _schema true to use declared schema, false otherwise.
   * @param _handler The DefaultHandler to use while parsing the xml file.
   * @return The list of errors.
   */
  protected List<Error> validate (
      String _project, String _file, boolean _schema, DefaultHandler _handler)
    throws Exception
  {
    List<Error> errors = XmlUtils.validateXml(_project, _file, _schema, _handler);
    for(Iterator<Error> ii = errors.iterator(); ii.hasNext();){
      Error error = ii.next();
      // FIXME: hack to ignore errors regarding no defined dtd.
      // When 1.4 no longer needs to be supported, this can be scrapped.
      if (error.getMessage().indexOf(NO_GRAMMER) != -1 ||
          error.getMessage().indexOf(DOCTYPE_ROOT_NULL) != -1)
      {
        ii.remove();
      }
    }
    return errors;
  }
}
