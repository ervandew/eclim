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
package org.eclim.plugin.wst.command.validate;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclipse.wst.wsdl.validation.internal.IValidationMessage;
import org.eclipse.wst.wsdl.validation.internal.IValidationReport;
import org.eclipse.wst.wsdl.validation.internal.WSDLValidator;

/**
 * Command for wsdl validation requests.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class WsdlValidateCommand
  extends WstValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);

      ArrayList results = new ArrayList();
      WSDLValidator validator = new WSDLValidator();
      IValidationReport result = validator.validate(toUri(file));
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

      return super.filter(_commandLine,
          (Error[])results.toArray(new Error[results.size()]));
    }catch(Throwable t){
      return t;
    }
  }
}
