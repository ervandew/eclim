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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Output filter for import results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ImportFilter
  implements OutputFilter<List<ImportResult>>
{
  public static final ImportFilter instance = new ImportFilter();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<ImportResult> _result)
  {
    if(_result != null){
      ArrayList<String> elements = new ArrayList<String>(_result.size());
      for(ImportResult result : _result){
        elements.add(result.getElement());
      }
      Collections.sort(elements);
      return StringUtils.join(elements.toArray(), '\n');
    }
    return "";
  }
}
