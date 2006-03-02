/**
 * Copyright (c) 2004 - 2005
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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.JavaUtils;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.Signature;

/**
 * Command to handle java code completion requests.
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

      ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

      String filename = src.getResource().getRawLocation().toOSString();

      CompletionRequestor requestor = new CompletionRequestor();
      src.codeComplete(offset, requestor);

      List proposals = requestor.getProposals();
      for(Iterator ii = proposals.iterator(); ii.hasNext();){
        CompletionProposal proposal = (CompletionProposal)ii.next();
        // for classnames, we're removing the package, which may lead to
        // duplicate results in their unqualified form.
        CodeCompleteResult result = createCompletionResult(filename, proposal);
        if(!results.contains(result)){
          results.add(result);
        }
      }
    }catch(Exception e){
      return e;
    }
    return filter(_commandLine,
        results.toArray(new CodeCompleteResult[results.size()]));
  }

  /**
   * Create a CodeCompleteResult from the supplied CompletionProposal.
   *
   * @param _filename The filename.
   * @param _proposal The proposal.
   *
   * @return The result.
   */
  protected CodeCompleteResult createCompletionResult (
      String _filename, CompletionProposal _proposal)
  {
    String completion = new String(_proposal.getCompletion());
    String signature = createSignature(_proposal);

    switch(_proposal.getKind()){
      // trim off the package for type references.
      case CompletionProposal.TYPE_REF:
        String packge = new String(_proposal.getDeclarationSignature());
        if(completion.startsWith(packge)){
          completion = completion.substring(packge.length() + 1);
        }
        break;

      // trim off the trailing paren if the method takes any arguments.
      case CompletionProposal.METHOD_REF:
        if(signature.lastIndexOf(')') > signature.lastIndexOf('(') + 1){
          completion = completion.substring(0, completion.length() - 1);
        }
        break;
    }

    return new CodeCompleteResult(
        _proposal.getKind(), _filename, completion, signature,
        _proposal.getReplaceStart(), _proposal.getReplaceEnd());
  }

  /**
   * Creates a string representation of the proposals signature.
   *
   * @param _proposal The CompletionProposal.
   * @return The signature.
   */
  protected String createSignature (CompletionProposal _proposal)
  {
    StringBuffer signature = new StringBuffer();
    switch(_proposal.getKind()){
      case CompletionProposal.TYPE_REF:
        signature.append(_proposal.getCompletion());
        signature.append(" ");
        /*signature.append(Signature.getSignatureQualifier(
              _proposal.getSignature()));
        signature.append('.');*/
        signature.append(Signature.getSignatureSimpleName(
              _proposal.getSignature()));
        break;
      case CompletionProposal.FIELD_REF:
        signature.append(_proposal.getCompletion());
        signature.append(" ");
        /*char[] qualifier = Signature.getSignatureQualifier(
            _proposal.getSignature());
        if(qualifier.length > 0){
          signature.append(qualifier).append('.');
        }*/
        signature.append(Signature.getSignatureSimpleName(
              _proposal.getSignature()));
        signature.append(" - ");
        /*signature.append(Signature.getSignatureQualifier(
              _proposal.getDeclarationSignature()));
        signature.append('.');*/
        signature.append(Signature.getSignatureSimpleName(
              _proposal.getDeclarationSignature()));
        break;
      case CompletionProposal.LOCAL_VARIABLE_REF:
        signature.append(_proposal.getCompletion());
        signature.append(" ");
        /*qualifier = Signature.getSignatureQualifier(_proposal.getSignature());
        if(qualifier.length > 0){
          signature.append(qualifier).append('.');
        }*/
        signature.append(Signature.getSignatureSimpleName(
              _proposal.getSignature()));
        signature.append(" - ");
        /*signature.append(Signature.getSignatureQualifier(
              _proposal.getDeclarationSignature()));
        signature.append('.');*/
        signature.append(Signature.getSignatureSimpleName(
              _proposal.getDeclarationSignature()));
        break;
      case CompletionProposal.METHOD_REF:
        signature.append(_proposal.getName()).append('(');
        char[][] types = Signature.getParameterTypes(_proposal.getSignature());
        char[][] names = _proposal.findParameterNames(null);
        for(int ii = 0; ii < types.length; ii++){
          if(ii != 0){
            signature.append(", ");
          }
          /*qualifier = Signature.getSignatureQualifier(types[ii]);
          if(qualifier.length > 0){
            signature.append(qualifier).append('.');
          }*/
          signature.append(Signature.getSignatureSimpleName(types[ii]));
          if(names != null){
            signature.append(' ');
            signature.append(new String(names[ii]));
          }
        }
        signature.append(')');
        signature.append(' ');
        signature.append(Signature.getSignatureSimpleName(
              Signature.getReturnType(_proposal.getSignature())));
        signature.append(" - ");
        /*signature.append(Signature.getSignatureQualifier(
              _proposal.getDeclarationSignature()));
        signature.append('.');*/
        signature.append(Signature.getSignatureSimpleName(
              _proposal.getDeclarationSignature()));
        break;
    }

    return signature.toString();
  }
}
