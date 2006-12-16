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
import java.util.List;

import org.apache.log4j.Logger;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;
import org.eclim.command.Options;

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
  private static final Logger logger =
    Logger.getLogger(CodeCompleteFilter.class);

  private static String COMPACT = "compact";
  //private static String STANDARD = "standard";

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, Object _result)
  {
    List results = (List)_result;
    if(results != null){
      try{
        String layout = _commandLine.getValue(Options.LAYOUT_OPTION);
        if(COMPACT.equals(layout)){
          return compactFormat(results);
        }
      }catch(Exception e){
        logger.warn("Failed to get layout option.", e);
      }
      return standardFormat(results);
    }
    return "";
  }

  /**
   * Print the results in standard format.
   *
   * @param _results The results in standard format.
   * @return The formatted results.
   */
  private String standardFormat (List _results)
  {
    StringBuffer buffer = new StringBuffer();
    for(int ii = 0; ii < _results.size(); ii++){
      CodeCompleteResult result = (CodeCompleteResult)_results.get(ii);
      if(buffer.length() > 0){
        buffer.append('\n');
      }

      buffer.append(getTypeString(result));
      buffer.append(result.getCompletion()).append('|');

      if(result.getShortDescription() != null){
        buffer.append(result.getShortDescription());
      }

      buffer.append('|');

      if(result.getDescription() != null){
        buffer.append(result.getDescription());
      }
    }
    return buffer.toString();
  }

  /**
   * Print the results in a compact format.
   *
   * @param _results The results in compact format.
   * @return The formatted results.
   */
  private String compactFormat (List _results)
  {
    StringBuffer buffer = new StringBuffer();
    if(_results.size() > 0){
      CodeCompleteResult result = (CodeCompleteResult)_results.get(0);
      String lastWord = result.getCompletion();
      Overload overload = new Overload();
      overload.add(result);

      for(int ii = 1; ii < _results.size(); ii++){
        result = (CodeCompleteResult)_results.get(ii);
        if(result.getCompletion().equals(lastWord)){
          overload.add(result);
        }else{
          if(buffer.length() > 0){
            buffer.append('\n');
          }
          buffer.append(overload);
          overload.clear();
          overload.add(result);
        }
        lastWord = result.getCompletion();
      }
      if(buffer.length() > 0){
        buffer.append('\n');
      }
      buffer.append(overload);
    }
    return buffer.toString();
  }

  /**
   * Converts the result type into the vim 'kind' equivalent string.
   *
   * @param _result The completion result.
   * @return The type string.
   */
  private String getTypeString (CodeCompleteResult _result)
  {
    switch(_result.getType()){
      case CompletionProposal.TYPE_REF:
        return "c|";
      case CompletionProposal.FIELD_REF:
        return "v|";
      case CompletionProposal.LOCAL_VARIABLE_REF:
        return "v|";
      case CompletionProposal.METHOD_REF:
        return "f|";
      default:
        return "|";
    }
  }

  private class Overload
  {
    private String word;
    private String type;
    private String menu;
    private List list = new ArrayList();

    public void add (CodeCompleteResult _result)
    {
      if(list.size() == 0){
        word = _result.getCompletion();
        type = getTypeString(_result);
        menu = _result.getShortDescription();
      }
      list.add(_result);
    }

    public void clear ()
    {
      list.clear();
    }

    public String toString ()
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append(type).append(word).append('|');
      if(list.size() > 1){
        buffer.append("Overloaded, see preview...");

        StringBuffer info = new StringBuffer();
        for (int ii = 0; ii < list.size(); ii++){
          CodeCompleteResult result = (CodeCompleteResult)list.get(ii);
          if(info.length() > 0){
            info.append("<br/>");
          }
          info.append(result.getShortDescription());
        }
        buffer.append('|').append(info);
      }else if(list.size() > 0){
        buffer.append(menu).append('|').append(menu);
      }

      return buffer.toString();
    }
  }
}
