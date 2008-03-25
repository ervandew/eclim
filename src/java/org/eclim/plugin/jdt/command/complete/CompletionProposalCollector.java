/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
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
