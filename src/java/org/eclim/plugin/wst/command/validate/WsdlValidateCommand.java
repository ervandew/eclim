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

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclipse.wst.wsdl.validation.internal.IValidationMessage;
import org.eclipse.wst.wsdl.validation.internal.IValidationReport;

import org.eclipse.wst.wsdl.validation.internal.eclipse.WSDLValidator;

/**
 * Command for wsdl validation requests.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class WsdlValidateCommand
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
    WSDLValidator validator = WSDLValidator.getInstance();
    IValidationReport result = validator.validate(toUri(project, file));
    IValidationMessage[] messages = result.getValidationMessages();
    for (int ii = 0; ii < messages.length; ii++){
      StringBuffer message = new StringBuffer(messages[ii].getMessage());
      for (Iterator jj = messages[ii].getNestedMessages().iterator(); jj.hasNext();){
        IValidationMessage nested = (IValidationMessage)jj.next();
        message.append(' ').append(nested.getMessage());
      }

      results.add(new Error(
            message.toString(),
            toFile(messages[ii].getURI()),
            messages[ii].getLine(),
            messages[ii].getColumn(),
            messages[ii].getSeverity() == IValidationMessage.SEV_WARNING
      ));
    }

    return ErrorFilter.instance.filter(commandLine, results);
  }
}
