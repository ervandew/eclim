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
package org.eclim.command.filter;

import java.util.Arrays;
import java.util.Comparator;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.OutputFilter;

/**
 * Filter for generating vim output for an array of Error.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ErrorFilter
  implements OutputFilter
{
  private static final ErrorComparator COMPARTATOR = new ErrorComparator();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, Object _result)
  {
    if (_result != null &&
        _result.getClass().isArray() &&
        Error.class.isAssignableFrom(_result.getClass().getComponentType()))
    {
      StringBuffer buffer = new StringBuffer();
      Error[] errors = (Error[])_result;
      Arrays.sort(errors, COMPARTATOR);
      for(int ii = 0; ii < errors.length; ii++){
        if(ii > 0){
          buffer.append('\n');
        }
        buffer.append(errors[ii].getFilename())
          .append('|')
          .append(errors[ii].getLineNumber())
          .append(" col ")
          .append(errors[ii].getColumnNumber())
          .append('|')
          .append(errors[ii].getMessage())
          .append('|')
          .append(errors[ii].isWarning() ? 'w' : 'e');
      }

      return buffer.toString();
    }
    return _result.toString();
  }

  /**
   * Comparator for sorting Error arrays.
   */
  public static class ErrorComparator
    implements Comparator<Error>
  {
    /**
     * {@inheritDoc}
     */
    public int compare (Error _o1, Error _o2)
    {
      if(_o1 == null && _o2 == null){
        return 0;
      }else if(_o2 == null){
        return -1;
      }else if(_o1 == null){
        return 1;
      }

      // sort by line / col / error,warning
      if (_o1.getLineNumber() != _o2.getLineNumber()){
        return _o1.getLineNumber() - _o2.getLineNumber();
      }
      if (_o1.getColumnNumber() != _o2.getColumnNumber()){
        return _o1.getColumnNumber() - _o2.getColumnNumber();
      }
      if (_o1.isWarning() != _o2.isWarning()){
        return !_o1.isWarning() ? -1 : 1;
      }
      return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals (Object _obj)
    {
      if(_obj instanceof ErrorComparator){
        return true;
      }
      return false;
    }
  }
}
