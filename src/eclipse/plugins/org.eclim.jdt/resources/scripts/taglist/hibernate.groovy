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
import org.eclim.plugin.core.command.taglist.RegexTaglist;
import org.eclim.plugin.core.command.taglist.TaglistScript;
import org.eclim.plugin.core.command.taglist.TagResult;

/**
 * Processes tags for hibernate mapping files.
 */
class HibernateTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def regex = null;
    try{
      regex = new RegexTaglist(file);
      regex.addPattern('c', ~/(s?)<class\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('j', ~/(s?)<joined-subclass\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('t', ~/(s?)<typedef\s+[^>]*?class=['"](.*?)['"]/, "\$2");
      regex.addPattern('f', ~/(s?)<filter-def\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('i', ~/(s?)<import\s+[^>]*?class=['"](.*?)['"]/, "\$2");
      regex.addPattern('q', ~/(s?)<query\s+[^>]*?name=['"](.*?)['"]/, "\$2");
      regex.addPattern('s', ~/(s?)<sql-query\s+[^>]*?name=['"](.*?)['"]/, "\$2");

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
