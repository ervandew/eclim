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
import org.eclim.plugin.core.command.taglist.RegexTaglist;
import org.eclim.plugin.core.command.taglist.TaglistScript;
import org.eclim.plugin.core.command.taglist.TagResult;

/**
 * Processes tags for gant files.
 */
class GantTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def regex = null;
    try{
      regex = new RegexTaglist(file);
      regex.addPattern('t', ~/(s?)(?:^|\n)\s*target\s*\(\s*(?:name\s*:)?\s*['"]?([^'",: ]+)/, '$2');
      regex.addPattern('f', ~/(s?)(?:^|\n)\s*def\s+(\w+)\s*\(/, '$2');
      regex.addPattern('f', ~/(s?)(?:^|\n)\s*(?:final|def)?\s*(\w+)\s*=\s*\{/, '$2');

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
