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
package org.eclim.plugin.pdt.command.complete;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.plugin.dltk.command.complete.AbstractCodeCompleteCommand;

import org.eclim.util.StringUtils;

import org.eclipse.dltk.core.ISourceModule;

import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

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
  private static final Pattern PREFIX =
    Pattern.compile("^(.*?[^\\w$])[\\w$]*$");
  private static final Pattern METHOD_WITH_ARGS =
    Pattern.compile("^(\\w+\\s*\\().+\\)\\s*$");
  private static final Pattern REMOVE_HEAD =
    Pattern.compile("(s?)<head>.*</head>", Pattern.MULTILINE | Pattern.DOTALL);

  @Override
  protected ScriptCompletionProposalCollector getCompletionCollector(
      ISourceModule module)
  {
    // Using a regular document doesn't work... if at some point passing null
    // for the document stops working, then look at:
    // php.internal.core.codeassist.contexts.AbstractCompletionContext.determineDocument
    return new PHPCompletionProposalCollector(null /*document*/, module, true);
  }

  @Override
  protected String getCompletion(
      IDocument document, int offset, IScriptCompletionProposal proposal)
  {
    // php proposal display string isn't reliable enough to use as the
    // completion, so we have to resort to applying each completion and looking
    // at the change to determine the completion value to return.
    String content = document.get();
    try{
      int ln = document.getLineOfOffset(offset);
      IRegion region = document.getLineInformation(ln);
      String line = document.get(region.getOffset(), region.getLength());
      String prefix = line.substring(0, offset - region.getOffset());
      String suffix = line.replace(prefix, StringUtils.EMPTY);

      // here we are basically mimicing what we do on the vim side to determine
      // the start of where the completion will actually be inserted.
      Matcher matcher = PREFIX.matcher(prefix);
      if (matcher.find()){
        prefix = matcher.group(1);
      }

      proposal.apply(document);
      region = document.getLineInformation(ln);
      line = document.get(region.getOffset(), region.getLength());

      // remove everything from the line that isn't part of the completion, and
      // we are left with the proposal string.
      String completion = line;
      completion = completion.replace(prefix, StringUtils.EMPTY);
      completion = completion.replace(suffix, StringUtils.EMPTY);

      matcher = METHOD_WITH_ARGS.matcher(completion);
      if (matcher.find()){
        completion = matcher.group(1);
      }

      return completion;
    }catch(Exception e){
      throw new RuntimeException(e);
    }finally{
      document.set(content);
    }
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
