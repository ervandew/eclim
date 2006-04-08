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
package org.eclim.plugin.jdt.command.search;

import java.util.Iterator;
import java.util.List;

import org.eclim.command.OutputFilter;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Position;

import org.eclim.util.vim.VimUtils;

/**
 * Output filter for search results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SearchFilter
  implements OutputFilter
{
  /**
   * {@inheritDoc}
   */
  public String filter (Object _result)
  {
    try{
      StringBuffer buffer = new StringBuffer();
      List results = (List)_result;
      if(results != null){
        for(Iterator ii = results.iterator(); ii.hasNext();){
          SearchResult result = (SearchResult)ii.next();
          String url = FileUtils.toUrl(result.getFilename());
          Position position = new Position(url, result.getOffset(), 0);
          String lineColumn = VimUtils.translateLineColumn(position);

          if(lineColumn != null){
            if(buffer.length() > 0){
              buffer.append('\n');
            }
            buffer.append(url)
              .append('|')
              .append(lineColumn)
              .append('|')
              .append(result.getElement());
          }
        }
      }
      return buffer.toString();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
}
