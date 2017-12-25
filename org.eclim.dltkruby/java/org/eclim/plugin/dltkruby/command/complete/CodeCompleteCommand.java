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
package org.eclim.plugin.dltkruby.command.complete;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.plugin.dltk.command.complete.AbstractCodeCompleteCommand;

import org.eclim.util.StringUtils;

import org.eclipse.dltk.core.ISourceModule;

import org.eclipse.dltk.ruby.internal.ui.text.completion.RubyCompletionProposalCollector;

import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;

import org.eclipse.jface.text.IDocument;

/**
 * Command to perform ruby code completion.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "ruby_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class CodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  private static final Pattern DISPALY_TO_COMPLETION =
    Pattern.compile("^(.*?)\\s+-\\s+.*");
  private static final Pattern METHOD_WITH_ARGS =
    Pattern.compile("^([a-zA-Z_?!=`<>~]+\\s*\\().+\\)\\s*$");

  @Override
  protected ScriptCompletionProposalCollector getCompletionCollector(
      ISourceModule module)
  {
    return new RubyCompletionProposalCollector(module);
  }

  @Override
  protected String getCompletion(
      IDocument document, int offset, IScriptCompletionProposal proposal)
  {
    String completion = proposal.getDisplayString().trim();
    completion = DISPALY_TO_COMPLETION.matcher(completion).replaceFirst("$1");

    Matcher matcher = METHOD_WITH_ARGS.matcher(completion);
    if (matcher.find()){
      completion = matcher.group(1);
    }else if(completion.endsWith("()")){
      completion = completion.substring(0, completion.length() - 2);
    }
    return completion;
  }

  @Override
  protected String getInfo(IScriptCompletionProposal proposal)
  {
    // too slow for a lot of results
    //String description = super.getInfo(proposal);
    return StringUtils.EMPTY;
  }
}
