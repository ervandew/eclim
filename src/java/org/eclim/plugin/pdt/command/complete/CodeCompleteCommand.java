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
package org.eclim.plugin.pdt.command.complete;

import org.eclim.command.CommandLine;

import org.eclim.plugin.wst.command.complete.WstCodeCompleteCommand;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.php.internal.ui.editor.contentassist.PHPContentAssistProcessor;

/**
 * Command to perform php code completion.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCompleteCommand
  extends WstCodeCompleteCommand
{
  private static PHPContentAssistProcessor processor;

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor (
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    if (processor == null){
      processor = new PHPContentAssistProcessor();
    }
    return processor;
  }
}
