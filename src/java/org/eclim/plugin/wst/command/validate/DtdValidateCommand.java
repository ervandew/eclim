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

//import org.eclipse.wst.dtd.core.internal.validation.DTDValidator;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclipse.wst.dtd.core.internal.validation.eclipse.DTDValidator;

import org.eclipse.wst.xml.core.internal.validation.core.ValidationMessage;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationReport;

/**
 * Command to validate dtd files.
 *
 * @author Eric Van Dewoestine
 */
public class DtdValidateCommand
  extends WstValidateCommand
{
  //private DTDValidator validator = new DTDValidator();

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);

    ArrayList<Error> results = new ArrayList<Error>();
    DTDValidator validator = DTDValidator.getInstance();
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
