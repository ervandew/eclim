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
import org.eclim.plugin.core.command.taglist.RegexTaglist;
import org.eclim.plugin.core.command.taglist.TaglistScript;
import org.eclim.plugin.core.command.taglist.TagResult;

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

      /* Match Objects/Classes */
      //regex.addPattern('o', ~/(s?)(?<!var)\s*([A-Za-z0-9_.]+)\s*=\s*\{/, "\$2");
      regex.addPattern('o', ~/(s?)([A-Za-z0-9_.]+)\s*=\s*\{(\s*[^}]|$)/, "\$2");

      // prototype.js has Object.extend to extend existing objects.
      regex.addPattern('o', ~/(s?)(var\s+)?\b([A-Z][A-Za-z0-9_.]+)\s*=\s*Object\.extend\s*\(/, "\$3");
      regex.addPattern('o', ~/(s?)\bObject\.extend\s*\(\b([A-Z][A-Za-z0-9_.]+)\s*,\s*\{/, "\$2");

      // mootools uses 'new Class'
      regex.addPattern('o', ~/(s?)(var\s+)?\b([A-Z][A-Za-z0-9_.]+)\s*=\s*new\s+Class\s*\(/, "\$3");

      // firebug uses extend
      regex.addPattern('o', ~/(s?)(var\s+)?\b([A-Z][A-Za-z0-9_.]+)\s*=\s*extend\s*\(/, "\$3");

      // vimperator uses function MyClass ()
      regex.addPattern('o', ~/(s?)function\s+\b([A-Z][A-Za-z0-9_.]+)\s*\(/, "\$2");
      // vimperator uses var = (function()
      regex.addPattern('o', ~/(s?)([A-Za-z0-9_.]+)\s*=\s*\(function\s*\(/, "\$2");

      /* Match Functions */
      regex.addPattern('f', ~/(s?)\bfunction\s+([a-zA-Z0-9_.\$]+?)\s*\(/, "\$2");
      regex.addPattern('f', ~/(s?)([a-zA-Z0-9_.\$]+?)\s*=\s*function\s*\(/, "\$2");

      /* Match Members */
      regex.addPattern('m', ~/(s?)\b([a-zA-Z0-9_.\$]+?)\s*:\s*function\s*\(/, "\$2");

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
