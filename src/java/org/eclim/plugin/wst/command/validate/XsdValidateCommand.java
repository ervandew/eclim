/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.wst.command.validate;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.filter.ErrorFilter;

import org.eclipse.wst.xml.core.internal.validation.core.ValidationMessage;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationReport;

import org.eclipse.wst.xsd.core.internal.validation.eclipse.XSDValidator;

/**
 * Command to validate dtd files.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "xsd_validate",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class XsdValidateCommand
  extends WstValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);

    ArrayList<Error> results = new ArrayList<Error>();
    XSDValidator validator = XSDValidator.getInstance();
    ValidationReport result = validator.validate(toUri(project, file));
    ValidationMessage[] messages = result.getValidationMessages();
    for (int ii = 0; ii < messages.length; ii++){
      StringBuffer message = new StringBuffer(messages[ii].getMessage());
      for (Iterator jj = messages[ii].getNestedMessages().iterator(); jj.hasNext();){
        ValidationMessage nested = (ValidationMessage)jj.next();
        message.append(' ').append(nested.getMessage());
      }

      results.add(new Error(
            message.toString(),
            toFile(messages[ii].getUri()),
            messages[ii].getLineNumber(),
            messages[ii].getColumnNumber(),
            false//messages[ii].getSeverity() != ValidationMessage.SEV_HIGH
      ));
    }

    return ErrorFilter.instance.filter(commandLine, results);
  }
}
