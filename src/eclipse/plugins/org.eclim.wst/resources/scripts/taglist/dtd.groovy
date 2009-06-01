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
import java.io.File;

import org.eclim.plugin.core.command.taglist.TaglistScript;
import org.eclim.plugin.core.command.taglist.TagResult;

/**
 * Processes tags for dtd files.
 */
class DtdTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def results = [];
    def lineNumber = 0;
    new File(file).eachLine {
      line -> processTag(line, ++lineNumber, file, results)
    };

    return (TagResult[])results.toArray(new TagResult[results.size()]);
  }

  void processTag (line, lineNumber, file, results)
  {
    def matcher = line =~ /^\s*<!ELEMENT\s+(.*?)(\s|\s*$)/;
    if(matcher.matches()){
      def name = matcher[0][1];
      def tag = new TagResult(
        file:file, pattern:line, line:lineNumber, kind:'e', name:name);

      results.add(tag);
    }
  }
}
