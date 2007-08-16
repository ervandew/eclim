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

import java.lang.reflect.Field;

import java.util.regex.Pattern;

import org.eclim.command.CommandLine;

import org.eclim.command.complete.AbstractCodeCompleteCommand;

import org.eclim.plugin.wst.command.complete.WstCodeCompleteCommand;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.php.internal.ui.editor.contentassist.CodeDataCompletionProposal;
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
  private static final Pattern METHOD_WITH_ARGS =
    Pattern.compile("^\\w+\\s*\\(.+\\).*$");
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

  /**
   * {@inheritDoc}
   * @see org.eclim.command.complete.AbstractCodeCompleteCommand#getCompletion(ICompletionProposal)
   */
  @Override
  protected String getCompletion (ICompletionProposal proposal)
  {
    CodeDataCompletionProposal phpProposal = (CodeDataCompletionProposal)proposal;
    String prefix = (String)phpProposal.getPrefixCompletionText(null, 0);
    try {
      Field suffixField = CodeDataCompletionProposal.class.getDeclaredField("suffix");
      suffixField.setAccessible(true);
      String suffix = (String)suffixField.get(phpProposal);
      if (suffix.endsWith(";")){
        suffix = suffix.substring(0, suffix.length() - 1);
      }
      if (suffix.endsWith(")") &&
          METHOD_WITH_ARGS.matcher(proposal.getDisplayString()).matches())
      {
        suffix = suffix.substring(0, suffix.length() - 1);
      }

      return prefix + phpProposal.getCodeData().getName() + suffix;
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   * @see org.eclim.command.complete.AbstractCodeCompleteCommand#getShortDescription(ICompletionProposal)
   */
  @Override
  protected String getShortDescription (ICompletionProposal proposal)
  {
    return proposal.getDisplayString();
  }
}
