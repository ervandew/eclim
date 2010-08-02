/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.complete;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.command.OutputFilter;

/**
 * Filter for code completion results.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteFilter
  implements OutputFilter<List<CodeCompleteResult>>
{
  public static final CodeCompleteFilter instance = new CodeCompleteFilter();

  private static final String DELIMETER = "|";

  private static String COMPACT = "compact";
  private static String STANDARD = "standard";

  /**
   * {@inheritDoc}
   */
  public String filter(CommandLine commandLine, List<CodeCompleteResult> results)
  {
    String delimiter = null;
    String layout = null;
    try{
      delimiter = commandLine.hasOption(Options.DELIMETER_OPTION) ?
        commandLine.getValue(Options.DELIMETER_OPTION) : DELIMETER;
      layout = commandLine.hasOption(Options.LAYOUT_OPTION) ?
        commandLine.getValue(Options.LAYOUT_OPTION) : STANDARD;
    }catch(Exception e){
      throw new RuntimeException(e);
    }

    if(COMPACT.equals(layout)){
      return compactFormat(results, delimiter);
    }
    return standardFormat(results, delimiter);
  }


  /**
   * Print the results in standard format.
   *
   * @param results The results in standard format.
   * @param delimiter The delimiter to use.
   * @return The formatted results.
   */
  private String standardFormat(List<CodeCompleteResult> results, String delimiter)
  {
    StringBuffer buffer = new StringBuffer();
    if(results != null){
      for(CodeCompleteResult result : results){
        if(buffer.length() > 0){
          buffer.append('\n');
        }

        buffer.append(result.getCompletion()).append(delimiter);

        if(result.getShortDescription() != null){
          buffer.append(result.getShortDescription());
        }

        buffer.append(delimiter);

        if(result.getDescription() != null){
          buffer.append(result.getDescription());
        }
      }
    }
    return buffer.toString();
  }

  /**
   * Print the results in a compact format.
   *
   * @param results The results in compact format.
   * @param delimiter The delimiter to use.
   * @return The formatted results.
   */
  private String compactFormat(List<CodeCompleteResult> results, String delimiter)
  {
    StringBuffer buffer = new StringBuffer();
    if(results.size() > 0){
      CodeCompleteResult result = (CodeCompleteResult)results.get(0);
      String lastWord = result.getCompletion();
      Overload overload = new Overload(delimiter);
      overload.add(result);

      for(int ii = 1; ii < results.size(); ii++){
        result = (CodeCompleteResult)results.get(ii);
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

  private class Overload
  {
    private String delimiter;
    private String word;
    private String menu;
    private ArrayList<CodeCompleteResult> list =
      new ArrayList<CodeCompleteResult>();

    public Overload(String delimiter)
    {
      this.delimiter = delimiter;
    }

    public void add(CodeCompleteResult result)
    {
      if(list.size() == 0){
        word = result.getCompletion();
        menu = result.getShortDescription();
      }
      list.add(result);
    }

    public void clear()
    {
      list.clear();
    }

    public String toString()
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append(word).append(delimiter);
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
        buffer.append(delimiter).append(info);
      }else if(list.size() > 0){
        buffer.append(menu).append(delimiter).append(menu);
      }

      return buffer.toString();
    }
  }
}
