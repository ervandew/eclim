/**
 * Copyright (c) 2005 - 2006
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
        "(s?)create\\s+table\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('t', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)create\\s+view\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('v', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)create\\s+sequence\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('q', pattern, "\$2");

      pattern = Pattern.compile(
        "(s?)create(\\s+or\\s+replace)?\\s+function\\s+([a-zA-Z0-9_.]+)", Pattern.CASE_INSENSITIVE);
      regex.addPattern('f', pattern, "\$3");

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
