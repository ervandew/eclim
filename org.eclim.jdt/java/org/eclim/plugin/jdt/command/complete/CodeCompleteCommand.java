/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 * Command to handle java code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG," +
    "REQUIRED l layout ARG"
)
public class CodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  private static final Logger logger = Logger.getLogger(CodeCompleteCommand.class);

  protected static final Comparator<CodeCompleteResult> COMPLETION_COMPARATOR =
    new CompletionComparator();

  private ThreadLocal<CompletionProposalCollector> collector =
    new ThreadLocal<CompletionProposalCollector>();

  @Override
  protected Object getResponse(List<CodeCompleteResult> results)
  {
    CompletionProposalCollector collector = this.collector.get();
    return new CodeCompleteResponse(
        results, collector.getError(), collector.getImports());
  }

  @Override
  protected List<CodeCompleteResult> getCompletionResults(
      CommandLine commandLine, String project, String file, int offset)
    throws Exception
  {
    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    CompletionProposalCollector collector =
      new CompletionProposalCollector(src);
    src.codeComplete(offset, collector);

    IJavaCompletionProposal[] proposals =
      collector.getJavaCompletionProposals();
    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();
    for(IJavaCompletionProposal proposal : proposals){
      results.add(createCompletionResult(proposal));
    }
    Collections.sort(results, COMPLETION_COMPARATOR);

    this.collector.set(collector);

    return results;
  }

  /**
   * Create a CodeCompleteResult from the supplied CompletionProposal.
   *
   * @param proposal The proposal.
   *
   * @return The result.
   */
  protected CodeCompleteResult createCompletionResult(
      IJavaCompletionProposal proposal)
      throws Exception
  {
    return createCompletionResult(proposal, false);
  }

  /**
   * Create a CodeCompleteResult from the supplied CompletionProposal.
   *
   * @param proposal
   *          The proposal.
   * @param javaDocEnabled
   *          Flag if a javaDoc URI should be added to the CodeCompleteResult. If
   *          {@code javaDocEnabled} is true, the java doc URI corresponding to
   *          the proposal will be added to the result. If {code javaDocEnabled}
   *          is false the java doc URI is set to "".
   * @return The result.
   */
  protected CodeCompleteResult createCompletionResult(
      IJavaCompletionProposal proposal, boolean javaDocEnabled)
      throws Exception
  {
    String completion = null;
    String menu = proposal.getDisplayString();
    Integer offset = null;
    String javaDocURI = "";

    int kind = -1;
    if(proposal instanceof JavaCompletionProposal){
      JavaCompletionProposal lazy = (JavaCompletionProposal)proposal;
      completion = lazy.getReplacementString();
      offset = lazy.getReplacementOffset();
      if(javaDocEnabled){
        javaDocURI = getJavaDocLink(proposal);
      }
    }else if(proposal instanceof LazyJavaCompletionProposal){
      LazyJavaCompletionProposal lazy = (LazyJavaCompletionProposal)proposal;
      completion = lazy.getReplacementString();
      offset = lazy.getReplacementOffset();
      Method getProposal = LazyJavaCompletionProposal.class
        .getDeclaredMethod("getProposal");
      getProposal.setAccessible(true);
      CompletionProposal cproposal = (CompletionProposal)getProposal.invoke(lazy);
      if(javaDocEnabled){
        javaDocURI = getJavaDocLink(proposal);
      }
      if (cproposal != null){
        kind = cproposal.getKind();
      }
    }

    switch(kind){
      case CompletionProposal.METHOD_REF:
        int length = completion.length();
        if (length == 0){
          break;
        }
        if (completion.charAt(length - 1) == ';'){
          completion = completion.substring(0, length - 1);
          length--;
        }
        // trim off the trailing paren if the method takes any arguments.
        // Note: using indexOf instead of lastIndexOf to account for groovy
        // completion menu text.
        if (menu.indexOf(')') > menu.indexOf('(') + 1 &&
            completion.charAt(length - 1) == ')')
        {
          completion = completion.substring(0, completion.lastIndexOf('(') + 1);
        }
        break;
      case CompletionProposal.TYPE_REF:
        // trim off package info.
        int idx = completion.lastIndexOf('.');
        if(idx != -1){
          completion = completion.substring(idx + 1);
        }
        break;
    }

    if("class".equals(completion)){
      kind = CompletionProposal.KEYWORD;
    }

    String type = "";
    switch(kind){
      case CompletionProposal.TYPE_REF:
        type = CodeCompleteResult.TYPE;
        break;
      case CompletionProposal.FIELD_REF:
      case CompletionProposal.LOCAL_VARIABLE_REF:
        type = CodeCompleteResult.VARIABLE;
        type = CodeCompleteResult.VARIABLE;
        break;
      case CompletionProposal.METHOD_REF:
        type = CodeCompleteResult.FUNCTION;
        break;
      case CompletionProposal.KEYWORD:
        type = CodeCompleteResult.KEYWORD;
        break;
    }

    // TODO:
    // hopefully Bram will take my advice to add lazy retrieval of
    // completion 'info' so that I can provide this text without the
    // overhead involved with retrieving it for every completion regardless
    // of whether the user ever views it.
    /*return new CodeCompleteResult(
        kind, completion, menu, proposal.getAdditionalProposalInfo());*/
    return new CodeCompleteResult(completion, menu, menu, type, offset, javaDocURI);
  }

  private String getJavaDocLink(IJavaCompletionProposal proposal)
  {
    try {
      IJavaElement javaElement = getJavaElement(proposal);
      if (javaElement == null) {
        return "";
      } else {
        return JavaElementLinks.createURI(JavaElementLinks.JAVADOC_SCHEME,
            javaElement);
      }
    } catch (Exception e) {
      logger.error("Could not calculate the javaDoc link", e);
      return "";
    }
  }

  /**
   * Gets the {@code IJavaElement} which is behind the {@code proposal}. If the
   * {@code proposal} does not contain an {@code IJavaElement} null will be
   * returned.
   *
   * @param proposal
   *          The proposal from which we want the IJavaElement
   * @return IJavaElement The IJavaElement which is behind the {@code proposal}
   *         if there is one.
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws JavaModelException
   */
  private IJavaElement getJavaElement(IJavaCompletionProposal proposal)
      throws NoSuchMethodException, IllegalAccessException,
      InvocationTargetException, JavaModelException
  {
    if (!(AbstractJavaCompletionProposal.class
        .isAssignableFrom(proposal.getClass())))
    {
      // We do not throw an exception here since it may be that only some
      // elements cannot create a javaDocLink and not all of them.
      logger.error("The proposal " + proposal.toString() +
          " does not inherit from class AbstractJavaCompletionProposal." +
          " ==> We cannot create the javaDoc link.");
      return null;
    }
    Method getProposal = AbstractJavaCompletionProposal.class
        .getDeclaredMethod("getProposalInfo");

    getProposal.setAccessible(true);
    ProposalInfo proposalInfo = (ProposalInfo) getProposal
        .invoke((AbstractJavaCompletionProposal) proposal);
    if (proposalInfo != null) {
      return proposalInfo.getJavaElement();
    } else {
      return null;
    }
  }
}
