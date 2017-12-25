/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.jface.text.templates.TemplateProposal;

import org.eclipse.wst.html.ui.internal.contentassist.HTMLContentAssistProcessor;

import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;

/**
 * Command to handle html code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "html_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class HtmlCodeCompleteCommand
  extends WstCodeCompleteCommand
{
  @Override
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String project, String file)
  {
    return new HTMLContentAssistProcessor();
  }

  @Override
  protected boolean acceptProposal(ICompletionProposal proposal)
  {
    if (proposal instanceof TemplateProposal){
      return false;
    }
    String display = proposal.getDisplayString();
    return
      !display.toLowerCase().startsWith("close with") &&
      !display.toLowerCase().startsWith("end with");
  }

  @Override
  protected String getCompletion(ICompletionProposal proposal)
  {
    if (proposal instanceof CustomCompletionProposal){
      CustomCompletionProposal completion = (CustomCompletionProposal)proposal;
      String replacement = completion.getReplacementString();
      // strip off trailing " on attribute completions
      if (replacement.endsWith("\"")){
        return replacement.substring(0, replacement.length() - 1);
      }
      // strip off closing tag so the user doesn't have to then move back to the
      // end of the start tag to keep typing.
      if (replacement.indexOf("</") != -1){
        return replacement.substring(0, replacement.indexOf("</"));
      }
      return replacement;
    }
    return proposal.getDisplayString();
  }
}
