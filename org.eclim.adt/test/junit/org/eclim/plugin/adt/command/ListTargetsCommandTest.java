/**
 * Copyright (C) 2012  Eric Van Dewoestine
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
package org.eclim.plugin.adt.command;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ListTargetsCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ListTargetsCommandTest
{
  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    List<Map<String,String>> results = (List<Map<String,String>>)
      Eclim.execute(new String[]{"android_list_targets"});
    assertTrue("No targets returned", results.size() > 0);

    for(Map<String,String> result : results){
      assertTrue("Missing key 'name'", result.containsKey("name"));
      assertTrue("Missing key 'hash'", result.containsKey("hash"));
      assertTrue("Missing key 'api'", result.containsKey("api"));
      assertTrue("Invalid api version", StringUtils.isNumeric(result.get("api")));
    }
  }
}
