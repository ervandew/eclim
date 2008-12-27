/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.regex;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileOffsets;
import org.eclim.util.file.FileUtils;

/**
 * Command to evaluate the specified regex test file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class RegexCommand
  extends AbstractCommand
{
  private static final String FILE = "file";
  private static final String LINE = "line";

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String type = commandLine.getValue(Options.TYPE_OPTION);
    return RegexFilter.instance.filter(commandLine, evaluate(file, type));
  }

  /**
   * Evaluates the supplied test regex file.
   *
   * @param file The file name.
   * @param type The regex evaluation type to use.
   * @return The results.
   */
  private List<MatcherResult> evaluate(String file, String type)
    throws Exception
  {
    ArrayList<MatcherResult> results = new ArrayList<MatcherResult>();

    String regex = null;
    FileInputStream fis = null;
    try{
      fis = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

      // read the pattern from the first line of the file.
      regex = reader.readLine();
      Pattern pattern = Pattern.compile(regex.trim());

      if (type == null || FILE.equals(type)){
        FileOffsets offsets = FileOffsets.compile(file);
        Matcher matcher = FileUtils.matcher(pattern, file);

        // force matching to start past the first line.
        if(matcher.find(regex.length() + 1)){
          processFinding(offsets, matcher, results);
        }
        while(matcher.find()){
          processFinding(offsets, matcher, results);
        }
      }else{
        String line = null;
        for(int ii = 2; (line = reader.readLine()) != null; ii++){
          Matcher matcher = pattern.matcher(line);

          if(matcher.find()){
            processFinding(ii, matcher, results);
          }
          while(matcher.find()){
            processFinding(ii, matcher, results);
          }
        }
      }
    }finally{
      IOUtils.closeQuietly(fis);
    }

    return results;
  }

  /**
   * Process the current regex finding.
   *
   * @param offsets The FileOffsets.
   * @param matcher The Matcher.
   * @param results The list of results to add to.
   */
  private void processFinding(
      FileOffsets offsets, Matcher matcher, List<MatcherResult> results)
  {
    MatcherResult result = new MatcherResult();

    int[] lineColumn = offsets.offsetToLineColumn(matcher.start());
    result.setStartLine(lineColumn[0]);
    result.setStartColumn(lineColumn[1]);

    lineColumn = offsets.offsetToLineColumn(matcher.end() - 1);
    result.setEndLine(lineColumn[0]);
    result.setEndColumn(lineColumn[1]);

    for (int ii = 1; ii <= matcher.groupCount(); ii++){
      MatcherResult group = new MatcherResult();

      lineColumn = offsets.offsetToLineColumn(matcher.start(ii));
      group.setStartLine(lineColumn[0]);
      group.setStartColumn(lineColumn[1]);

      lineColumn = offsets.offsetToLineColumn(matcher.end(ii) - 1);
      group.setEndLine(lineColumn[0]);
      group.setEndColumn(lineColumn[1]);

      result.addGroupMatch(group);
    }
    results.add(result);
  }

  /**
   * Process the current regex finding.
   *
   * @param line The current line number being processed.
   * @param matcher The Matcher.
   * @param results The list of results to add to.
   */
  private void processFinding(
      int line, Matcher matcher, List<MatcherResult> results)
  {
    MatcherResult result = new MatcherResult();

    result.setStartLine(line);
    result.setStartColumn(matcher.start() + 1);

    result.setEndLine(line);
    result.setEndColumn(matcher.end());

    for (int ii = 1; ii <= matcher.groupCount(); ii++){
      MatcherResult group = new MatcherResult();

      group.setStartLine(line);
      group.setStartColumn(matcher.start(ii) + 1);

      group.setEndLine(line);
      group.setEndColumn(matcher.end(ii));

      result.addGroupMatch(group);
    }
    results.add(result);
  }
}
