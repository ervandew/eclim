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
package org.eclim.plugin.dltk.command.complete;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.dltk.util.DltkUtils;

import org.eclipse.core.resources.IFile;

import org.eclipse.dltk.core.ISourceModule;

import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;

import org.eclipse.jface.text.IDocument;

//import org.eclipse.dltk.ui.DLTKUIPlugin;
//import org.eclipse.dltk.ui.PreferenceConstants;

/**
 * Abstract base class for dltk code completion commands.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractCodeCompleteCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    IFile ifile = ProjectUtils.getFile(project, file);
    IDocument document = ProjectUtils.getDocument(project, file);
    int offset = getOffset(commandLine);

    /*int timeout = DLTKUIPlugin.getDefault().getPreferenceStore()
      .getInt(PreferenceConstants.CODEASSIST_TIMEOUT);*/
    int timeout = 5000;
    ISourceModule module = getSourceModule(ifile);
    ScriptCompletionProposalCollector collector = getCompletionCollector(module);
    module.codeComplete(offset, collector, timeout);

    IScriptCompletionProposal[] proposals =
      collector.getScriptCompletionProposals();

    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();
    for (IScriptCompletionProposal proposal : proposals){
      CodeCompleteResult ccresult = new CodeCompleteResult(
          getCompletion(document, offset, proposal),
          getMenu(proposal),
          getInfo(proposal));
      ccresult.setRelevance(proposal.getRelevance());

      if(!results.contains(ccresult)){
        results.add(ccresult);
      }
    }

    Collections.sort(results, new CodeCompleteResultComparator());
    return results;
  }

  /**
   * Get the ISourceModule instance for the supplied file.
   *
   * @param file The IFile.
   * @return The ISourceModule.
   */
  protected ISourceModule getSourceModule(IFile file)
  {
    return DltkUtils.getSourceModule(file);
  }

  /**
   * Get the completion collector used to collect the completion proposals.
   *
   * @param module The source module.
   * @return The completion collector.
   */
  protected abstract ScriptCompletionProposalCollector getCompletionCollector(
      ISourceModule module);

  /**
   * Get the completion from the proposal.
   *
   * @param document The IDocument.
   * @param offset The offset in the document.
   * @param proposal The IScriptCompletionProposal.
   * @return The completion.
   */
  protected String getCompletion(
      IDocument document, int offset, IScriptCompletionProposal proposal)
  {
    return proposal.getDisplayString().trim();
  }

  /**
   * Get the menu text from the proposal.
   *
   * @param proposal The IScriptCompletionProposal.
   * @return The menu text.
   */
  protected String getMenu(IScriptCompletionProposal proposal)
  {
    return proposal.getDisplayString().trim();
  }

  /**
   * Get the info details from the proposal.
   *
   * @param proposal The IScriptCompletionProposal.
   * @return The description.
   */
  protected String getInfo(IScriptCompletionProposal proposal)
  {
    String info = proposal.getAdditionalProposalInfo();
    if(info != null){
      info = info.trim();
    }
    return info;
  }

  private class CodeCompleteResultComparator
    implements Comparator<CodeCompleteResult>
  {
    private Collator COLLATOR = Collator.getInstance(Locale.US);

    public int compare(CodeCompleteResult r1, CodeCompleteResult r2)
    {
      int diff = r1.getRelevance() - r2.getRelevance();
      if (diff == 0){
        return COLLATOR.compare(r1.getCompletion(), r2.getCompletion());
      }
      return 0 - diff;
    }
  }
}
