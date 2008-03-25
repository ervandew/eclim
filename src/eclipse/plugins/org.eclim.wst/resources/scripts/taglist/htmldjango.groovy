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
import java.util.regex.Pattern;

import org.eclim.command.taglist.RegexTaglist;
import org.eclim.command.taglist.TaglistScript;
import org.eclim.command.taglist.TagResult;

/**
 * Processes tags for django html files.
 */
class HtmlDjangoTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def regex = null;
    try{
      regex = new RegexTaglist(file);

      regex.addPattern('a', ~/(s?)<a\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('i', ~/(s?)<([a-z]*?)\s+[^>]*?id=['"](.*?)['"]/, "\$2 \$3");
      // TODO: javascript function
      //regex.addPattern('f', ~/(s?), "\$2");

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
