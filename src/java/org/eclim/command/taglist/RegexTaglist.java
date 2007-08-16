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
package org.eclim.command.taglist;

import java.io.FileInputStream;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.Services;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileOffsets;

/**
 * Handles processing tags from a file using a series of regex patterns.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class RegexTaglist
{
  private String file;
  private String contents;
  private FileOffsets offsets;
  private ArrayList<TagResult> results = new ArrayList<TagResult>();

  /**
   * Constructs a new instance.
   *
   * @param _file The file to be processed.
   */
  public RegexTaglist (String _file)
    throws Exception
  {
    FileInputStream is = null;
    try{
      is = new FileInputStream(_file);

      file = _file;
      contents = IOUtils.toString(is);
      offsets = FileOffsets.compile(_file);
    }finally{
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Adds the supplied tag matching pattern.
   *
   * @param _kind Character kind for the tag.
   * @param _pattern Regex pattern for matching this tag.
   * @param _replace The replacement that represents the tag value.
   */
  public void addPattern (String _kind, String _pattern, String _replace)
    throws Exception
  {
    addPattern(_kind, _pattern, _replace);
  }

  /**
   * Adds the supplied tag matching pattern.
   *
   * @param _kind Character kind for the tag.
   * @param _pattern Regex pattern for matching this tag.
   * @param _replace The replacement that represents the tag value.
   */
  public void addPattern (String _kind, Pattern _pattern, String _replace)
    throws Exception
  {
    if(_kind.length() != 1){
      throw new IllegalArgumentException(
          Services.getMessage("taglist.kind.invalid", _kind));
    }

    Matcher matcher = _pattern.matcher(contents);
    while(matcher.find()){
      int start = matcher.start();
      int end = matcher.end();
      String matched = contents.substring(start, end);

      int first = offsets.getLineStart(offsets.offsetToLineColumn(start)[0]);
      int last = offsets.getLineEnd(offsets.offsetToLineColumn(end)[0]);
      String lines = contents.substring(first, last);

      TagResult result = new TagResult();
      result.setFile(file);
      result.setName(_pattern.matcher(matched).replaceFirst(_replace));
      result.setKind(_kind.toCharArray()[0]);
      result.setLine(offsets.offsetToLineColumn(start)[0]);
      result.setPattern(lines);

      results.add(result);
    }
  }

  /**
   * Executes the processing of the specified tags.
   *
   * @return Array of TagResult.
   */
  public TagResult[] execute ()
  {
    return (TagResult[])results.toArray(new TagResult[results.size()]);
  }

  /**
   * Cleans of this instance by releasing any held resources.
   */
  public void close ()
  {
    contents = null;
  }
}
