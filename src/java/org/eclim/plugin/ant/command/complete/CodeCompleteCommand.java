/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.ant.command.complete;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.eclipse.jface.text.DummyTextViewer;

import org.eclim.plugin.ant.util.AntUtils;

import org.eclipse.ant.internal.ui.editor.AntEditorCompletionProcessor;

import org.eclipse.ant.internal.ui.model.AntModel;

import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Command to handle ant file code completion requests.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCompleteCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    List results = new ArrayList();
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);
      int offset = Integer.parseInt(_commandLine.getValue(Options.OFFSET_OPTION));

      AntModel model = (AntModel)AntUtils.getAntModel(file);
      AntEditorCompletionProcessor processor =
        new AntEditorCompletionProcessor(model);

      ITextViewer viewer =
        new DummyTextViewer(AntUtils.getDocument(file), offset, 1);
      ICompletionProposal[] proposals =
        processor.computeCompletionProposals(viewer, offset);

      for (int ii = 0; ii < proposals.length; ii++){
        String description = null;
        if(proposals[ii].getAdditionalProposalInfo() != null){
          description = StringUtils.replace(
            proposals[ii].getAdditionalProposalInfo().trim(), "\n", "<br/>");
        }

        String completion = proposals[ii].getDisplayString();
        int index = completion.indexOf(" - ");
        if(index != -1){
          completion = completion.substring(0, index);
        }

        CodeCompletionResult result =
          new CodeCompletionResult(completion, description);
        if(!results.contains(result)){
          results.add(result);
        }
      }

      return filter(_commandLine, (CodeCompletionResult[])
        results.toArray(new CodeCompletionResult[results.size()]));

    }catch(Exception e){
      return e;
    }
  }
}
