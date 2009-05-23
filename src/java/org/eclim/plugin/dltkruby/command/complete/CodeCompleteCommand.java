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
package org.eclim.plugin.dltkruby.command.complete;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.command.complete.AbstractCodeCompleteCommand;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.eclipse.ui.EclimEditorSite;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.dltk.internal.ui.editor.ScriptSourceViewer;

import org.eclipse.dltk.ruby.core.RubyNature;

import org.eclipse.dltk.ruby.internal.ui.editor.RubyEditor;

import org.eclipse.dltk.ruby.internal.ui.text.RubySourceViewerConfiguration;
import org.eclipse.dltk.ruby.internal.ui.text.RubyTextTools;

import org.eclipse.dltk.ruby.internal.ui.text.completion.RubyCompletionProcessor;

import org.eclipse.dltk.ui.DLTKUILanguageManager;
import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.swt.SWT;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;

import org.eclipse.ui.part.FileEditorInput;

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
    Pattern.compile("^(.*)\\s+-\\s+.*");
  private static final Pattern METHOD_WITH_ARGS =
    Pattern.compile("^([a-zA-Z_?!=`<>]+\\s*\\().+\\)\\s*$");

  private static final RubyTextTools textTools = new RubyTextTools(true);
  private static ISourceViewer viewer;

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String projectName, String file)
    throws Exception
  {
    int offset = getOffset(commandLine);
    IProject project = ProjectUtils.getProject(projectName, true);
    IFile ifile = ProjectUtils.getFile(project, file);

    IEditorSite site = new EclimEditorSite();
    IEditorInput input = new FileEditorInput(ifile);
    RubyEditor editor = new RubyEditor();
    editor.init(site, input);
    editor.setInput(input);

    IDocument document = ProjectUtils.getDocument(project, file);
    textTools.setupDocumentPartitioner(document);
    IDLTKUILanguageToolkit toolkit =
      DLTKUILanguageManager.getLanguageToolkit(RubyNature.NATURE_ID);
    IPreferenceStore store = toolkit.getCombinedPreferenceStore();
    viewer = new ScriptSourceViewer(
        EclimPlugin.getShell(), null, null, false, SWT.NONE, store);

    String partioning = IDocument.DEFAULT_CONTENT_TYPE;
    //String partioning = IRubyPartitions.RUBY_PARTITIONING;
    RubySourceViewerConfiguration config = new RubySourceViewerConfiguration(
        textTools.getColorManager(), store, editor, partioning);
    viewer.configure(config);
    viewer.setEditable(true);
    viewer.setDocument(document);
    viewer.setSelectedRange(offset, 1);

    return new RubyCompletionProcessor(
      editor, (ContentAssistant)config.getContentAssistant(viewer), partioning);
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getTextViewer(CommandLine,String,String)
   */
  protected ITextViewer getTextViewer(
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    return viewer;
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getCompletion(ICompletionProposal)
   */
  @Override
  protected String getCompletion(ICompletionProposal proposal)
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

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getDescription(ICompletionProposal)
   */
  @Override
  protected String getDescription(ICompletionProposal proposal)
  {
    // too slow for a lot of results
    //String description = super.getDescription(proposal);
    return StringUtils.EMPTY;
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getShortDescription(ICompletionProposal)
   */
  @Override
  protected String getShortDescription(ICompletionProposal proposal)
  {
    return proposal.getDisplayString().trim();
  }
}
