/**
 * Copyright (C) 2005 - 2015  Eric Van Dewoestine
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.plugin.dltk.command.complete.AbstractCodeCompleteCommand;

import org.eclipse.dltk.core.ISourceModule;

import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;

import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposalCollector;

/**
 * Command to perform php code completion.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "php_complete",
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
    Pattern.compile("^(.*)(\\s+-\\s+.*|:\\s+\\S+)");
  private static final Pattern METHOD_WITH_ARGS =
    Pattern.compile("^(\\w+\\s*\\().+\\)\\s*$");
  private static final Pattern REMOVE_HEAD =
    Pattern.compile("(s?)<head>.*</head>", Pattern.MULTILINE | Pattern.DOTALL);

  @Override
  protected ScriptCompletionProposalCollector getCompletionCollector(
      ISourceModule module)
    throws Exception
  {
    // Using a regular document doesn't work... if at some point passing null
    // for the document stops working, then look at:
    // php.internal.core.codeassist.contexts.AbstractCompletionContext.determineDocument
    return new PHPCompletionProposalCollector(null /*document*/, module, true);
  }

  @Override
  protected String getCompletion(IScriptCompletionProposal proposal)
  {
    String completion = proposal.getDisplayString().trim();
    completion = DISPALY_TO_COMPLETION
      .matcher(completion).replaceFirst("$1").trim();

    Matcher matcher = METHOD_WITH_ARGS.matcher(completion);
    if (matcher.find()){
      completion = matcher.group(1);
    }else if(completion.startsWith("$")){
      completion = completion.substring(1);
    }

    // handle inconsistency w/ completion of php namespaced results where the
    // namespace will complete fully, but a function, etc in that namespace will
    // complete only that member:
    //   App\Lib being a full completion for 'App\' but,
    //   MyFunction() begin the completion for 'App\Lib\MyF'
    int index = completion.lastIndexOf('\\');
    if (index != -1){
      completion = completion.substring(index + 1);
    }

    return completion;
  }

  @Override
  protected String getInfo(IScriptCompletionProposal proposal)
  {
    String info = super.getInfo(proposal);
    if (info != null){
      info = REMOVE_HEAD.matcher(info).replaceFirst("");
      info = info.replaceAll("</dt>", ": ");
      info = info.replaceAll("</dd>", " ");
      info = info.replaceAll("\n", "");
      info = info.replaceAll("<dl>", "\n");
      info = info.replaceAll("</?[^>]+>", "");
      info = info.replaceAll("-->", "");
      info = info.replaceAll("&nbsp;?", " ");
      info = info.trim();
    }
    return info;
  }
}
