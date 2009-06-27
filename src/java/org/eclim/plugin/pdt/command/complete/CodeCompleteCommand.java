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
package org.eclim.plugin.pdt.command.complete;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.eclipse.ui.EclimEditorSite;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.pdt.util.PhpUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.php.internal.core.documentModel.partitioner.PHPPartitionTypes;

import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.php.internal.ui.editor.PHPStructuredTextViewer;

import org.eclipse.php.internal.ui.editor.configuration.PHPStructuredTextViewerConfiguration;

import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProcessor;

import org.eclipse.php.internal.ui.editor.templates.PhpTemplateProposal;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;

import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

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
    Pattern.compile("^(.*)\\s+-\\s+.*");
  private static final Pattern METHOD_WITH_ARGS =
    Pattern.compile("^(\\w+\\s*\\().+\\)\\s*$");
  private static final Pattern REMOVE_HEAD =
    Pattern.compile("(s?)<head>.*</head>", Pattern.MULTILINE | Pattern.DOTALL);

  private static PHPStructuredTextViewer viewer;

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String projectName, String file)
    throws Exception
  {
    PHPStructuredTextViewerConfiguration config =
      new PHPStructuredTextViewerConfiguration();

    IProject project = ProjectUtils.getProject(projectName, true);
    IFile ifile = ProjectUtils.getFile(project, file);

    // I really hate this hack
    PhpUtils.waitOnBuild();

    IStructuredModel model =
      StructuredModelManager.getModelManager().getModelForRead(ifile);
    IStructuredDocument document = model.getStructuredDocument();

    IEditorSite site = new EclimEditorSite();
    IEditorInput input = new FileEditorInput(ifile);
    PHPStructuredEditor editor = new PHPStructuredEditor(){
      public void update()
      {
        // no-op to prevent StructuredTextEditor from running it.
      }

      protected void installOverrideIndicator(boolean provideAST)
      {
        // no-op to prevent PHPStructuredEditor from running it.
      }
    };
    editor.init(site, input);
    editor.setInput(input);

    viewer = new PHPStructuredTextViewer(
        (ITextEditor)editor, EclimPlugin.getShell(), null, null, false, 0){
      protected void createControl(Composite parent, int styles)
      {
        // no-op to prevent possible deadlock in native method on windows.
      }
    };
    viewer.setDocument(document);

    return new PHPCompletionProcessor(
      viewer.getTextEditor(),
      (ContentAssistant)config.getPHPContentAssistant(viewer),
      PHPPartitionTypes.PHP_DEFAULT
    );
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
   * @see AbstractCodeCompleteCommand#acceptProposal(ICompletionProposal)
   */
  @Override
  protected boolean acceptProposal(ICompletionProposal proposal)
  {
    // filter out template proposals for now
    return !(proposal instanceof PhpTemplateProposal);
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
    }else if(completion.startsWith("$")){
      completion = completion.substring(1);
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
    String description = super.getDescription(proposal);
    description = REMOVE_HEAD.matcher(description).replaceFirst("");
    description = description.replaceAll("</dt>", ": ");
    description = description.replaceAll("</dd>", " ");
    description = description.replaceAll("</?[^>]+>", "");
    return description.trim();
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
