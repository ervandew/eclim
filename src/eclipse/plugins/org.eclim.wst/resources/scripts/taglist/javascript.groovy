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
      regex.addPattern('f', ~/(s?)function\s+([a-zA-Z0-9_.\$]*?)\s*\(/, "\$2");
      regex.addPattern('f', ~/(s?)([a-zA-Z0-9_.\$]*?)\s*=\s*function\s*\(/, "\$2");
      regex.addPattern('f', ~/(s?)([a-zA-Z0-9_.\$]*?)\s*:\s*function\s*\(/, "\$2");
      regex.addPattern('o', ~/(s?)(var\s\+)?\b([A-Z][A-Za-z0-9_.]*)\s*=\s*\{/, "\$3");
      //regex.addPattern('t', ~/(s?)<(xs[d]?:)?complexType\s+[^>]*?name=['"](.*?)['"]/, "\$3");

      return regex.execute();
    }finally{
      if (regex != null) regex.close();
    }
  }
}
