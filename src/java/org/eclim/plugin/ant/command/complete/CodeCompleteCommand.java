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
package org.eclim.plugin.ant.command.complete;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclim.command.CommandLine;

import org.eclim.command.complete.AbstractCodeCompleteCommand;

import org.eclim.plugin.ant.util.AntUtils;

import org.eclipse.ant.internal.ui.editor.TaskDescriptionProvider;

import org.eclipse.ant.internal.ui.model.AntModel;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

/**
 * Command to handle ant file code completion requests.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor (
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    AntModel model = (AntModel)AntUtils.getAntModel(project, file);
    AntEditorCompletionProcessor processor =
      new AntEditorCompletionProcessor(model);
    return processor;
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getCompletion(ICompletionProposal)
   */
  protected String getCompletion (ICompletionProposal proposal)
  {
    String completion = super.getCompletion(proposal);
    int index = completion.indexOf(" - ");
    if(index != -1){
      completion = completion.substring(0, index);
    }
    return completion;
  }
}
