/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.xml;

import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.XmlUtils;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Command to validate a xml file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "xml_validate",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL s schema NOARG"
)
public class ValidateCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    boolean schema = commandLine.hasOption(Options.SCHEMA_OPTION);

    return validate(project, file, schema, null);
  }

  /**
   * Validate the supplied file.
   *
   * @param project The project name.
   * @param file The file to validate.
   * @param schema true to use declared schema, false otherwise.
   * @param handler The DefaultHandler to use while parsing the xml file.
   * @return The list of errors.
   */
  protected List<Error> validate(
      String project, String file, boolean schema, DefaultHandler handler)
  {
    return XmlUtils.validateXml(project, file, schema, handler);
  }
}
