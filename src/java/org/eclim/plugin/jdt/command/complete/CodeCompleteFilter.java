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

import org.eclim.command.OutputFilter;

import org.eclim.util.vim.VimUtils;

import org.eclipse.jdt.core.CompletionProposal;

/**
 * Output filter for code completion results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCompleteFilter
  implements OutputFilter
{
  /**
   * {@inheritDoc}
   */
  public String filter (Object _result)
  {
    StringBuffer buffer = new StringBuffer();
    CodeCompleteResult[] results = (CodeCompleteResult[])_result;
    if(results != null){
      //String offset = null;
      for(int ii = 0; ii < results.length; ii++){
        if(buffer.length() > 0){
          buffer.append('\n');
        }
        switch(results[ii].getType()){
          case CompletionProposal.TYPE_REF:
            buffer.append("c|");
            break;
          case CompletionProposal.FIELD_REF:
            buffer.append("v|");
            break;
          case CompletionProposal.LOCAL_VARIABLE_REF:
            buffer.append("v|");
            break;
          case CompletionProposal.METHOD_REF:
            buffer.append("f|");
            break;
          default:
            buffer.append("|");
        }
        buffer.append(results[ii].getCompletion())
        /*  .append('|');
        // the offset should be the same for all results, so calculate only once
        if(offset == null){
          offset = VimUtils.translateOffset(
            results[ii].getFilename(), results[ii].getReplaceStart());
        }
        buffer.append(offset)*/
          .append('|')
          .append(results[ii].getSignature());
      }
    }
    return buffer.toString();
  }
}
