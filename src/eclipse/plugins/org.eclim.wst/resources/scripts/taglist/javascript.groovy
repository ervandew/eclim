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
import org.eclim.command.taglist.RegexTaglist;
import org.eclim.command.taglist.TaglistScript;
import org.eclim.command.taglist.TagResult;

/**
 * Processes tags for javascript files.
 */
class JavascriptTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def regex = null;
    try{
      regex = new RegexTaglist(file);
      regex.addPattern('f', ~/(s?)\bfunction\s+([a-zA-Z0-9_.\$]*?)\s*\(/, "\$2");
      //regex.addPattern('f', ~/(s?)(?<!var)\s+([a-zA-Z0-9_.\$]*?)\s*=\s*function\s*\(/, "\$2");
      regex.addPattern('m', ~/(s?)\b([a-zA-Z0-9_.\$]*?)\s*:\s*function\s*\(/, "\$2");
      regex.addPattern('o', ~/(s?)(var\s\+)?\b([A-Z][A-Za-z0-9_.]*)\s*=\s*\{/, "\$3");

      // prototype.js has Object.extend to extend existing objects.
      regex.addPattern('o', ~/(s?)(var\s\+)?\b([A-Z][A-Za-z0-9_.]*)\s*=\s*Object\.extend\s*\(/, "\$3");
      regex.addPattern('o', ~/(s?)\bObject\.extend\s*\(\b([A-Z][A-Za-z0-9_.]*)\s*,\s*\{/, "\$2");

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
