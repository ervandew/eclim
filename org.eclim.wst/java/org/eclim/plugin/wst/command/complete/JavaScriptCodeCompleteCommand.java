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

import java.util.ArrayList;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.wst.util.JavaScriptUtils;

import org.eclipse.wst.jsdt.core.CompletionProposal;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.Signature;

/**
 * Command to handle javascript code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "javascript_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class JavaScriptCodeCompleteCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int offset = getOffset(commandLine);

    IJavaScriptUnit src = JavaScriptUtils.getJavaScriptUnit(project, file);

    CompletionRequestor collector = new CompletionRequestor();
    src.codeComplete(offset, collector);

    return collector.getResults();
  }

  public static class CompletionRequestor
    extends org.eclipse.wst.jsdt.core.CompletionRequestor
  {
    private ArrayList<CodeCompleteResult> results =
      new ArrayList<CodeCompleteResult>();

    @Override
    public void accept(CompletionProposal proposal)
    {
      String completion = String.valueOf(proposal.getCompletion());
      String type = String.valueOf(proposal.getDeclarationTypeName());

      StringBuffer desc = new StringBuffer();
      desc.append(type).append('.');
      if (proposal.getKind() == CompletionProposal.METHOD_REF){
        String sig = String.valueOf(
            Signature.getSignatureSimpleName(proposal.getSignature()));
        String name = completion.replaceFirst("\\(\\)", "");

        int index = sig.indexOf(" (");
        String ret = sig.substring(0, index);
        String params = sig.substring(index).trim();

        desc.append(name).append(params).append(" : ").append(ret);

        // trim off the trailing paren if the method takes any arguments.
        if (params.lastIndexOf(')') > params.lastIndexOf('(') + 1 &&
            (completion.length() > 0 &&
             completion.charAt(completion.length() - 1) == ')'))
        {
          completion = completion.substring(0, completion.length() - 1);
        }
      }else{
        desc.append(completion);
      }

      String info = desc.toString();
      results.add(new CodeCompleteResult(completion, null, info));
    }

    public List<CodeCompleteResult> getResults()
    {
      return results;
    }
  }
}
