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

import java.text.Collator;

import java.util.Comparator;
import java.util.Locale;

import org.eclipse.jdt.core.CompletionProposal;

/**
 * Comparator for sorting completion results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CompletionComparator
  implements Comparator
{
  /**
   * {@inheritDoc}
   */
  public int compare (Object _o1, Object _o2)
  {
    if(_o1 == null && _o2 == null){
      return 0;
    }else if(_o2 == null){
      return -1;
    }else if(_o1 == null){
      return 1;
    }

    CodeCompleteResult p1 = (CodeCompleteResult)_o1;
    CodeCompleteResult p2 = (CodeCompleteResult)_o2;

    // push keywords to the end.
    if (p1.getType() != CompletionProposal.KEYWORD &&
        p2.getType() != CompletionProposal.KEYWORD)
    {
      int kind = p1.getType() - p2.getType();
      if(kind != 0){
        return kind;
      }
    }else if(p1.getType() == CompletionProposal.KEYWORD &&
        p2.getType() != CompletionProposal.KEYWORD)
    {
      return 1;
    }else if(p2.getType() == CompletionProposal.KEYWORD &&
        p1.getType() != CompletionProposal.KEYWORD)
    {
      return -1;
    }

    return Collator.getInstance(Locale.US).compare(
        new String(p1.getCompletion()), new String(p2.getCompletion()));
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals (Object _obj)
  {
    if(_obj instanceof CompletionComparator){
      return true;
    }
    return false;
  }
}
