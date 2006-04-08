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
package org.eclim.plugin.jdt.command.regex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.file.FileOffsets;
import org.eclim.util.file.FileUtils;

/**
 * Command to evaluate the specified regex test file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class RegexCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      return filter(_commandLine, evaluate(file));
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Evaluates the supplied test regex file.
   *
   * @param _file The file name.
   * @return The results.
   */
  private List evaluate (String _file)
    throws Exception
  {
    List results = new ArrayList();

    FileInputStream fis = null;
    try{
      fis = new FileInputStream(_file);
      InputStreamReader reader = new InputStreamReader(fis);

      // read the pattern from the first line of the file.
      String line = new BufferedReader(reader).readLine();
      Pattern pattern = Pattern.compile(line.trim(), Pattern.MULTILINE);

      // advance the input

      FileOffsets offsets = FileOffsets.compile(_file);
      Matcher matcher = FileUtils.matcher(pattern, fis, reader.getEncoding());

      // force matching to start past the first line.
      if(matcher.find(line.length() + 1)){
        processFinding(offsets, matcher, results);
      }
      while(matcher.find()){
        processFinding(offsets, matcher, results);
      }
    }finally{
      IOUtils.closeQuietly(fis);
    }

    return results;
  }

  /**
   * Process the current regex finding.
   *
   * @param _offsets The FileOffsets.
   * @param _matcher The Matcher.
   * @param _results The list of results to add to.
   */
  private void processFinding (
      FileOffsets _offsets, Matcher _matcher, List _results)
  {
    MatcherResult result = new MatcherResult();

    int[] lineColumn = _offsets.offsetToLineColumn(_matcher.start());
    result.setStartLine(lineColumn[0]);
    result.setStartColumn(lineColumn[1]);

    lineColumn = _offsets.offsetToLineColumn(_matcher.end());
    result.setEndLine(lineColumn[0]);
    result.setEndColumn(lineColumn[1]);

    for (int ii = 1; ii <= _matcher.groupCount(); ii++){
      MatcherResult group = new MatcherResult();

      lineColumn = _offsets.offsetToLineColumn(_matcher.start(ii));
      group.setStartLine(lineColumn[0]);
      group.setStartColumn(lineColumn[1]);

      lineColumn = _offsets.offsetToLineColumn(_matcher.end(ii));
      group.setEndLine(lineColumn[0]);
      group.setEndColumn(lineColumn[1]);

      result.addGroupMatch(group);
    }
    _results.add(result);
  }
}
