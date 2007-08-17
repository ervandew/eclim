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
package org.eclim.plugin.jdt.command.complete;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * Extension to eclipse CompletionProposalCollector that saves reference to
 * original CompletionProposals.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CompletionProposalCollector
  extends org.eclipse.jdt.ui.text.java.CompletionProposalCollector
{
  private ArrayList<CompletionProposal> proposals =
    new ArrayList<CompletionProposal>();

  public CompletionProposalCollector (ICompilationUnit cu)
  {
    super(cu);
  }

  public void accept (CompletionProposal proposal) {
    try {
      if (isFiltered(proposal)){
        return;
      }

      if (proposal.getKind() != CompletionProposal.POTENTIAL_METHOD_DECLARATION) {
        switch (proposal.getKind()) {
          case CompletionProposal.KEYWORD:
          case CompletionProposal.PACKAGE_REF:
          case CompletionProposal.TYPE_REF:
          case CompletionProposal.FIELD_REF:
          case CompletionProposal.METHOD_REF:
          case CompletionProposal.METHOD_NAME_REFERENCE:
          case CompletionProposal.METHOD_DECLARATION:
          case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
          case CompletionProposal.LABEL_REF:
          case CompletionProposal.LOCAL_VARIABLE_REF:
          case CompletionProposal.VARIABLE_DECLARATION:
          case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
          case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
            proposals.add(proposal);
            super.accept(proposal);
            break;
          default:
            // do nothing
        }
      }
    } catch (IllegalArgumentException e) {
      // all signature processing method may throw IAEs
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84657
      // don't abort, but log and show all the valid proposals
      JavaPlugin.log(
          new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IStatus.OK,
            "Exception when processing proposal for: " +
            String.valueOf(proposal.getCompletion()), e));
    }
  }

  public CompletionProposal getProposal (int index)
  {
    return (CompletionProposal)proposals.get(index);
  }
}
