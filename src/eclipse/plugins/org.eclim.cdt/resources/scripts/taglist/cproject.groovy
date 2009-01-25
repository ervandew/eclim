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
import org.eclim.command.taglist.RegexTaglist;
import org.eclim.command.taglist.TaglistScript;
import org.eclim.command.taglist.TagResult;

/**
 * Processes tags for cdt .cproject files.
 */
class CProjectTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def regex = null;
    try{
      regex = new RegexTaglist(file);
      regex.addPattern('c', ~/(s?)<configuration\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('e', ~/(s?)<entry\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('t', ~/(s?)<toolChain\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('l', ~/(s?)<tool\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('i', ~/(s?)<option\s+[^>]*?valueType=['"]includePath['"]/, "includes");
      regex.addPattern('s', ~/(s?)<option\s+[^>]*?valueType=['"]definedSymbols['"]/, "symbols");

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
