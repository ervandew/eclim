/**
 * Copyright (c) 2004 - 2006
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
import java.io.InputStreamReader;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.channels.FileChannel;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.eclim.util.file.FileOffsets;
import org.eclim.util.file.FileUtils;

/**
 * Handles processing tags from a file using a series of regex patterns.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class RegexTaglist
{
  private String file;
  private FileInputStream fileStream;;
  private CharBuffer fileBuffer;
  private FileOffsets offsets;
  private Matcher matcher;
  private List results = new ArrayList();

  /**
   * Constructs a new instance.
   *
   * @param _file The file to be processed.
   */
  public RegexTaglist (String _file)
    throws Exception
  {
    file = _file;
    fileStream = new FileInputStream(_file);

    FileChannel fc = fileStream.getChannel();
    ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
    Charset cs = Charset.forName(new InputStreamReader(fileStream).getEncoding());
    CharsetDecoder cd = cs.newDecoder();
    fileBuffer = cd.decode(bb);

    offsets = FileOffsets.compile(_file);
  }

  /**
   * Adds the supplied tag matching pattern.
   *
   * @param _kind Character kind for the tag.
   * @param _pattern Regex pattern for matching this tag.
   * @param _replace The replacement that represents the tag value.
   */
  public void addPattern (char _kind, String _pattern, String _replace)
    throws Exception
  {
    Pattern pattern = Pattern.compile(_pattern);
    addPattern(_kind, _pattern, _replace);
  }

  /**
   * Adds the supplied tag matching pattern.
   *
   * @param _kind Character kind for the tag.
   * @param _pattern Regex pattern for matching this tag.
   * @param _replace The replacement that represents the tag value.
   */
  public void addPattern (char _kind, Pattern _pattern, String _replace)
    throws Exception
  {
    if(matcher == null){
      matcher = _pattern.matcher(fileBuffer);
    }else{
      matcher.reset();
      matcher.usePattern(_pattern);
    }

    while(matcher.find()){
      int start = matcher.start();
      int end = matcher.end();
      String matched = fileBuffer.subSequence(start, end).toString();

      int first = offsets.getLineStart(offsets.offsetToLineColumn(start)[0]);
      int last = offsets.getLineEnd(offsets.offsetToLineColumn(end)[0]);
      String lines = fileBuffer.subSequence(first, last).toString();
      // escape newlines and '/'
      lines = StringUtils.replace(lines, "/", "\\/");
      lines = StringUtils.replace(lines, "\n", "\\n");
      // remove ctrl-Ms
      lines = StringUtils.replace(lines, "\r", "");

      TagResult result = new TagResult();
      result.setFile(file);
      result.setName(_pattern.matcher(matched).replaceFirst(_replace));
      result.setKind(_kind);
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
    IOUtils.closeQuietly(fileStream);
  }
}
