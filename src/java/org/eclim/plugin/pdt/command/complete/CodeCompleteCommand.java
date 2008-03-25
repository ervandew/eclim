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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
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
