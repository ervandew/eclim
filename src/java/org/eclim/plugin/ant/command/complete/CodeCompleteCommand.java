/**
 * Copyright (c) 2004 - 2006
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

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.eclipse.jface.text.DummyTextViewer;

import org.eclim.plugin.ant.util.AntUtils;

import org.eclipse.ant.internal.ui.AntUtil;

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

      AntModel model = (AntModel)AntUtil.getAntModel(file, true, true, true);
System.out.println("### ant model = " + model);
      AntEditorCompletionProcessor processor =
        new AntEditorCompletionProcessor(model);
System.out.println("### ant processor = " + processor);

      ITextViewer viewer =
        new DummyTextViewer(AntUtils.getDocument(file), offset, 1);
      ICompletionProposal[] proposals =
        processor.computeCompletionProposals(viewer, offset);

System.out.println("### ant proposals = " + proposals);
System.out.println("### ant proposals.length = " + proposals.length);
      for (int ii = 0; ii < proposals.length; ii++){
System.out.println("  ### proposal = " + proposals[ii].getDisplayString());
System.out.println("  ### proposal = " + proposals[ii].getAdditionalProposalInfo());
      }

    }catch(Exception e){
      return e;
    }
    return filter(_commandLine, results);
  }
}
