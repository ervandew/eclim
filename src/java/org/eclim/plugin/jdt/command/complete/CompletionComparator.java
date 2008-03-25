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
