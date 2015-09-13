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
package org.eclim.plugin.dltk.command.complete;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Arrays;
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
    ScriptCompletionProposalComparator comparator =
      new ScriptCompletionProposalComparator(document, offset);
    Arrays.sort(proposals, comparator);

    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();
    for (IScriptCompletionProposal proposal : proposals){
      CodeCompleteResult ccresult = new CodeCompleteResult(
          getCompletion(document, offset, proposal),
          getMenu(proposal),
          getInfo(proposal));

      if(!results.contains(ccresult)){
        results.add(ccresult);
      }
    }

    return results;
  }

  /**
   * Get the ISourceModule instance for the supplied file.
   *
   * @param file The IFile.
   * @return The ISourceModule.
   */
  protected ISourceModule getSourceModule(IFile file)
    throws Exception
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
      ISourceModule module)
    throws Exception;

  /**
   * Get the completion from the proposal.
   *
   * @param proposal The IScriptCompletionProposal.
   * @return The completion.
   */
  protected String getCompletion(
      IDocument doccument, int offset, IScriptCompletionProposal proposal)
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

  private class ScriptCompletionProposalComparator
    implements Comparator<IScriptCompletionProposal>
  {
    private Collator COLLATOR = Collator.getInstance(Locale.US);
    private IDocument document;
    private int offset;

    public ScriptCompletionProposalComparator(IDocument document, int offset){
      this.document = document;
      this.offset = offset;
    }

    public int compare(IScriptCompletionProposal p1, IScriptCompletionProposal p2)
    {
      int diff = p1.getRelevance() - p2.getRelevance();
      if (diff == 0){
        return COLLATOR.compare(
            getCompletion(document, offset, p1),
            getCompletion(document, offset, p2));
      }
      return 0 - diff;
    }
  }
}
