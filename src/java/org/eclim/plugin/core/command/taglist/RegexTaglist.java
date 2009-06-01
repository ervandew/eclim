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
package org.eclim.plugin.core.command.taglist;

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
 * @author Eric Van Dewoestine
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
   * @param file The file to be processed.
   */
  public RegexTaglist (String file)
    throws Exception
  {
    FileInputStream is = null;
    try{
      is = new FileInputStream(file);

      this.file = file;
      contents = IOUtils.toString(is);
      offsets = FileOffsets.compile(file);
    }finally{
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Adds the supplied tag matching pattern.
   *
   * @param kind Character kind for the tag.
   * @param pattern Regex pattern for matching this tag.
   * @param replace The replacement that represents the tag value.
   */
  public void addPattern(String kind, String pattern, String replace)
    throws Exception
  {
    addPattern(kind, Pattern.compile(pattern), replace);
  }

  /**
   * Adds the supplied tag matching pattern.
   *
   * @param kind Character kind for the tag.
   * @param pattern Regex pattern for matching this tag.
   * @param replace The replacement that represents the tag value.
   */
  public void addPattern(String kind, Pattern pattern, String replace)
    throws Exception
  {
    if(kind.length() != 1){
      throw new IllegalArgumentException(
          Services.getMessage("taglist.kind.invalid", kind));
    }

    if(!pattern.pattern().endsWith("\\n")){
      pattern = Pattern.compile(pattern.pattern() + ".*?\\n");
    }

    Matcher matcher = pattern.matcher(contents);
    while(matcher.find()){
      int start = matcher.start();
      int end = matcher.end();
      String matched = contents.substring(start, end);

      int first = offsets.getLineStart(offsets.offsetToLineColumn(start)[0]);
      int last = offsets.getLineEnd(offsets.offsetToLineColumn(end)[0]);
      String lines = contents.substring(first, last);

      TagResult result = new TagResult();
      result.setFile(file);
      result.setName(pattern.matcher(matched).replaceFirst(replace));
      result.setKind(kind.toCharArray()[0]);
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
  public TagResult[] execute()
  {
    return (TagResult[])results.toArray(new TagResult[results.size()]);
  }

  /**
   * Cleans of this instance by releasing any held resources.
   */
  public void close()
  {
    contents = null;
  }
}
