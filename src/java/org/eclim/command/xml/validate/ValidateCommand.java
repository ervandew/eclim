/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
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
