package eclim.plugin.groovy.command.complete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.core.dom.GroovyCompilationUnit;
import org.eclim.annotation.Command;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.eclipse.jface.text.DummyTextViewer;
import org.eclim.plugin.core.command.AbstractCommand;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;
import org.eclim.plugin.core.util.ProjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.ide.IDE;

@Command(name = "groovy_complete", options = "REQUIRED p project ARG,REQUIRED f file ARG,REQUIRED o offset ARG,REQUIRED e encoding ARG")
public final class CodeCompleteCommand extends AbstractCommand
{

  @Override
  public Object execute(CommandLine commandLine) throws Exception
  {
    int offset = getOffset(commandLine);
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    IFile ifile = ProjectUtils.getFile(ProjectUtils.getProject(project, true),
        file);
    ICompilationUnit unit = JavaCore.createCompilationUnitFrom(ifile);

    List<ICompletionProposal> proposals = performContentAssist(unit, offset,
        GroovyCompletionProposalComputer.class);
    List<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>(
        proposals.size());

    for(ICompletionProposal proposal : proposals){
      if(acceptProposal(proposal)){
        CodeCompleteResult ccresult = createCodeCompletionResult(proposal);

        if(!results.contains(ccresult)){
          results.add(ccresult);
        }
      }
    }

    return results;
  }

  /**
   * Get the menu text from the proposal.
   * 
   * @param proposal
   *          The ICompletionProposal.
   * @return The menu text.
   */
  protected String getMenu(ICompletionProposal proposal)
  {
    return StringUtils.EMPTY;
  }

  /**
   * Get the info details from the proposal.
   * 
   * @param proposal
   *          The ICompletionProposal.
   * @return The info.
   */
  protected String getInfo(ICompletionProposal proposal)
  {
    String info = proposal.getAdditionalProposalInfo();

    if(info != null){
      return info.trim();
    }

    return StringUtils.EMPTY;
  }

  protected CodeCompleteResult createCodeCompletionResult(
      ICompletionProposal proposal)
  {
    return new CodeCompleteResult(getCompletion(proposal), getMenu(proposal),
        getInfo(proposal));
  }

  protected List<ICompletionProposal> performContentAssist(
      ICompilationUnit unit, int offset,
      Class<? extends IJavaCompletionProposalComputer> computerClass)
      throws Exception
  {
    // ensure opens with Groovy editor
    if(unit instanceof GroovyCompilationUnit){
      unit.getResource().setPersistentProperty(IDE.EDITOR_KEY,
          GroovyEditor.EDITOR_ID);
    }

    JavaEditor editor = (JavaEditor) EditorUtility.openInEditor(unit);
    JavaSourceViewer viewer = (JavaSourceViewer) editor.getViewer();
    JavaContentAssistInvocationContext context = new JavaContentAssistInvocationContext(
        viewer, offset, editor);

    IJavaCompletionProposalComputer computer = computerClass.newInstance();
    List<ICompletionProposal> proposals = computer.computeCompletionProposals(
        context, null);

    if(proposals == null){
      proposals = Collections.emptyList();
    }

    editor.close(false);

    return proposals;
  }

  /**
   * Gets the text viewer passed to the content assist processor.
   * 
   * @param commandLine
   *          The current command line.
   * @param project
   *          The project the file is in.
   * @param file
   *          The file.
   * @return The ITextViewer.
   */
  protected ITextViewer getTextViewer(CommandLine commandLine, String project,
      String file) throws Exception
  {
    int offset = getOffset(commandLine);
    return new DummyTextViewer(ProjectUtils.getDocument(project, file), offset,
        1);
  }

  /**
   * Determines if the supplied proposal will be accepted as a result.
   * 
   * @param proposal
   *          The ICompletionProposal.
   * @return true if the proposal is accepted, false otherwise.
   */
  protected boolean acceptProposal(ICompletionProposal proposal)
  {
    return true;
  }

  /**
   * Get the completion from the proposal.
   * 
   * @param proposal
   *          The ICompletionProposal.
   * @return The completion.
   */
  protected String getCompletion(ICompletionProposal proposal)
  {
    return proposal.getDisplayString();
  }

}
