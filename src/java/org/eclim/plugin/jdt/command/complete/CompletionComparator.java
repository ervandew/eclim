/**
 * Copyright (c) 2005 - 2008
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CompletionComparator
  implements Comparator<CodeCompleteResult>
{
  /**
   * {@inheritDoc}
   */
  public int compare (CodeCompleteResult _o1, CodeCompleteResult _o2)
  {
    if(_o1 == null && _o2 == null){
      return 0;
    }else if(_o2 == null){
      return -1;
    }else if(_o1 == null){
      return 1;
    }

    // push keywords to the end.
    if (_o1.getType() != CompletionProposal.KEYWORD &&
        _o2.getType() != CompletionProposal.KEYWORD)
    {
      int kind = _o1.getType() - _o2.getType();
      if(kind != 0){
        return kind;
      }
    }else if(_o1.getType() == CompletionProposal.KEYWORD &&
        _o2.getType() != CompletionProposal.KEYWORD)
    {
      return 1;
    }else if(_o2.getType() == CompletionProposal.KEYWORD &&
        _o1.getType() != CompletionProposal.KEYWORD)
    {
      return -1;
    }

    return Collator.getInstance(Locale.US).compare(
        new String(_o1.getCompletion()), new String(_o2.getCompletion()));
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
