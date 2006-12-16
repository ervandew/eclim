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

import org.eclipse.wst.dtd.core.internal.validation.DTDValidator;

import org.eclipse.wst.xml.core.internal.validation.core.ValidationMessage;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationReport;

/**
 * Command to validate dtd files.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class DtdValidateCommand
  extends WstValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      //String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);

      ArrayList results = new ArrayList();
      DTDValidator validator = new DTDValidator();
      ValidationReport result = validator.validate(toUri(file));
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

      return super.filter(_commandLine,
          (Error[])results.toArray(new Error[results.size()]));
    }catch(Throwable t){
      return t;
    }
  }
}
