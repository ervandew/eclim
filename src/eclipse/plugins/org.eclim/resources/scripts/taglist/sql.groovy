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
 * Processes tags for sql files.
 */
class SqlTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def regex = null;
    try{
      regex = new RegexTaglist(file);

      def pattern = Pattern.compile(
        "(s?)create\\s+(group|role)\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('g', pattern, "\$3");

      pattern = Pattern.compile(
        "(s?)create\\s+user\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('u', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)create\\s+(tablespace|dbspace)\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('p', pattern, "\$3");

      pattern = Pattern.compile(
        "(s?)create\\s+schema\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('s', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)create\\s+table\\s+[`]?([a-zA-Z0-9_.]+)[`]?", Pattern.CASE_INSENSITIVE);
      regex.addPattern('t', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)create\\s+view\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('v', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)create\\s+sequence\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('q', pattern, "\$2");

      pattern = Pattern.compile(
        "(x?)create\\s+trigger\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('x', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)create(\\s+or\\s+replace)?\\s+function\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('f', pattern, "\$3");

      pattern = Pattern.compile(
        "(s?)create\\s+procedure\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('c', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)exec\\s+sp_addrole\\s+['\"]([a-zA-Z0-9_.]+)['\"]", Pattern.CASE_INSENSITIVE);
      regex.addPattern('r', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)exec\\s+sp_addlogin\\s+@loginname=['\"](.*?)['\"]", Pattern.CASE_INSENSITIVE);
      regex.addPattern('m', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)alter\\s+database.*add\\s+filegroup\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('z', pattern, "\$2");

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
