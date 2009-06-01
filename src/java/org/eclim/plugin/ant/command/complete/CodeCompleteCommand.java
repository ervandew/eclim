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
package org.eclim.plugin.ant.command.complete;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.ant.util.AntUtils;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;

import org.eclipse.ant.internal.ui.model.AntModel;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

/**
 * Command to handle ant file code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "ant_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class CodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor(
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
  protected String getCompletion(ICompletionProposal proposal)
  {
    String completion = super.getCompletion(proposal);
    int index = completion.indexOf(" - ");
    if(index != -1){
      completion = completion.substring(0, index);
    }
    return completion;
  }
}
