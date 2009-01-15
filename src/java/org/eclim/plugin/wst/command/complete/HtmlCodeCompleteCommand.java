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
package org.eclim.plugin.wst.command.complete;

import org.eclim.command.CommandLine;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.wst.html.ui.internal.contentassist.HTMLContentAssistProcessor;

/**
 * Command to handle html code completion requests.
 *
 * @author Eric Van Dewoestine
 */
public class HtmlCodeCompleteCommand
  extends WstCodeCompleteCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.complete.AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    return new HTMLContentAssistProcessor();
  }

  /**
   * {@inheritDoc}
   * @see org.eclim.command.complete.AbstractCodeCompleteCommand#acceptProposal(ICompletionProposal)
   */
  protected boolean acceptProposal(ICompletionProposal proposal)
  {
    String display = proposal.getDisplayString();
    return !display.toLowerCase().startsWith("close with");
  }
}
