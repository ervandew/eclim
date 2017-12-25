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

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;

/**
 * Command to handle xml code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "xml_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class XmlCodeCompleteCommand
  extends WstCodeCompleteCommand
{
  private static final ArrayList<String> IGNORE = new ArrayList<String>();
  static{
    IGNORE.add("comment - xml comment");
    IGNORE.add("XSL processing instruction - XSL processing instruction");
  }

  @Override
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String project, String file)
  {
    return new XMLContentAssistProcessor();
  }

  @Override
  protected boolean acceptProposal(ICompletionProposal proposal)
  {
    String display = proposal.getDisplayString();
    return !display.toLowerCase().startsWith("close with") &&
      !display.toLowerCase().startsWith("end with") &&
      !IGNORE.contains(display);
  }

  @Override
  protected String getMenu(ICompletionProposal proposal)
  {
    String menu = proposal.getAdditionalProposalInfo();
    if(menu != null){
      int index = menu.indexOf("</p>");
      if(index != -1){
        menu = menu.substring(index + 4);
        menu = CodeCompleteResult.menuFromInfo(menu);
      }
    }
    return menu;
  }
}
