/**
 * Copyright (c) 2005 - 2008
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

import org.eclim.command.taglist.RegexTaglist;
import org.eclim.command.taglist.TaglistScript;
import org.eclim.command.taglist.TagResult;

/**
 * Processes tags for web.xml files.
 */
class WebxmlTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def regex = null;
    try{
      regex = new RegexTaglist(file);
      regex.addPattern('p',
        ~/(s?)<context-param\s*>\s*<param-name\s*>\s*(.*?)\s*<\/param-name\s*>/, "\$2");
      regex.addPattern('f',
        ~/(s?)<filter\s*>\s*<filter-name\s*>\s*(.*?)\s*<\/filter-name\s*>/, "\$2");
      regex.addPattern('i',
        ~/(s?)<filter-mapping\s*>\s*<filter-name\s*>\s*(.*?)\s*<\/filter-name\s*>/, "\$2");
      regex.addPattern('l',
        ~/(s?)<listener\s*>\s*<listener-class\s*>\s*(.*?)\s*<\/listener-class\s*>/, "\$2");
      regex.addPattern('s',
        ~/(s?)<servlet\s*>\s*<servlet-name\s*>\s*(.*?)\s*<\/servlet-name\s*>/, "\$2");
      regex.addPattern('v',
        ~/(s?)<servlet-mapping\s*>\s*<servlet-name\s*>\s*(.*?)\s*<\/servlet-name\s*>/, "\$2");

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
