package org.eclim.plugin.adt.command.complete;

import java.util.HashMap;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.android.ide.eclipse.adt.internal.editors.AndroidXmlEditor;

@Command(
  name = "android_xml_complete",
  options =
  "REQUIRED p project ARG," +
  "REQUIRED f file ARG," +
  "REQUIRED o offset ARG," +
  "REQUIRED e encoding ARG," +
  "REQUIRED l layout ARG"
)
public class AdtXmlCodeCompleteCommand 
  extends AbstractCodeCompleteCommand
{

  @Override
  protected Object getResponse(List<CodeCompleteResult> results)
  {
    // Anything else wanted here?
    HashMap<String, Object> response = new HashMap<String, Object>();
    response.put("completions", results);
    return response;
  }

  @Override
  protected ICompletionProposal[] getCompletionProposals(
      CommandLine commandLine, String projectName, String file, int offset)
    throws Exception
  {
    final IProject project = ProjectUtils.getProject(projectName);
    final IFile ifile = ProjectUtils.getFile(project, file);

    AndroidXmlEditor editor = AdtAssistUtil.newXmlEditor(ifile, offset);

    // step 1: only slightly hacky completion based on default mechanism
    ICompletionProposal[] props = AdtAssistUtil
      .computeCompletionProposals(project, editor, ifile, offset);

    // step 2: release after, to ensure the model still exists
    //  (it's not needed for step 3)
    AdtAssistUtil.release(editor);

    // step 3: no results? twiddle the input 
    //  and pass to the ValuesContentAssist
    if (props.length == 0) {
      return AdtAssistUtil.attemptValuesCompletion(editor, offset);
    }

    return props;
  }


}
