/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.command.admin;

import java.text.Collator;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;

import org.apache.commons.collections.comparators.ComparatorChain;

import org.eclim.command.OutputFilter;

import org.eclim.preference.Option;
import org.eclim.preference.OptionInstance;

/**
 * Output filter for global settings.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SettingsFilter
  implements OutputFilter
{
  private static ComparatorChain OPTION_COMPARATOR = new ComparatorChain();
  static{
    OPTION_COMPARATOR.addComparator(new PathComparator());
    OPTION_COMPARATOR.addComparator(new BeanComparator("name"));
  }

  /**
   * {@inheritDoc}
   */
  public String filter (Object _result)
  {
    List list = (List)_result;
    if(list.size() > 0){
      return printOptions(list);
    }
    return "";
  }

  /**
   * Print supplied list of options.
   *
   * @param _options The option list.
   * @return The result.
   */
  protected String printOptions (List _options)
  {
    StringBuffer buffer = new StringBuffer();

    // sort the list
    Collections.sort(_options, OPTION_COMPARATOR);
    String lastPath = ((Option)_options.get(0)).getPath();
    buffer.append("# ").append(lastPath);
    for(Iterator ii = _options.iterator(); ii.hasNext();){
      OptionInstance option = (OptionInstance)ii.next();
      if(!option.getPath().equals(lastPath)){
        lastPath = option.getPath();
        buffer.append("\n\n# ").append(lastPath);
      }

      buffer.append("\n\t# ").append(option.getDescription()).append("\n\t");
      buffer.append(option.getName()).append('=').append(option.getValue());
    }
    return buffer.toString();
  }

  /**
   * Comparator to sort options by path.
   */
  private static class PathComparator
    implements Comparator
  {
    private Collator collator = Collator.getInstance();

    /**
     * {@inheritDoc}
     */
    public int compare (Object _o1, Object _o2)
    {
      Option option1 = (Option)_o1;
      Option option2 = (Option)_o2;

      if(option1.getPath().equals(option2.getPath())){
        return 0;
      }

      if (option1.getPath().startsWith("General") &&
          !option2.getPath().startsWith("General"))
      {
        return -1;
      }

      if (option2.getPath().startsWith("General") &&
          !option1.getPath().startsWith("General"))
      {
        return 1;
      }

      return collator.compare(option1.getPath(), option2.getPath());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals (Object _obj)
    {
      return super.equals(_obj);
    }
  }
}
