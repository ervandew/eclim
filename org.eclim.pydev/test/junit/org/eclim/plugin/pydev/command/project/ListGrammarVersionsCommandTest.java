/**
 * Copyright (C) 2014 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.project;

import java.util.List;

import org.eclim.Eclim;

import org.eclim.plugin.pydev.Pydev;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ListGrammarVersionsCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ListGrammarVersionsCommandTest
{
  @Test
  @SuppressWarnings("unchecked")
  public void searchDefinition()
  {
    assertTrue("Python project doesn't exist.",
        Eclim.projectExists(Pydev.TEST_PROJECT));

    List<String> results = (List<String>)
      Eclim.execute(new String[]{
        "python_list_grammars", "-p", Pydev.TEST_PROJECT,
      });

    assertTrue(results.size() > 2);
    for(String result : results){
      assertTrue(result.startsWith("2.") || result.startsWith("3."));
    }
  }
}
