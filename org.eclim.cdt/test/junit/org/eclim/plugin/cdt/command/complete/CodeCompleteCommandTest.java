/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.cdt.command.complete;

import java.util.List;
import java.util.Map;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.eclim.Eclim;

import org.eclim.plugin.cdt.Cdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "src/test_complete.c";

  @Test
  @SuppressWarnings("unchecked")
  public void completeAll()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_complete", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "140", "-e", "utf-8", "-l", "standard"
      });

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "test_a");
    assertEquals(result.get("menu"), "test_a : int");
    assertEquals(result.get("info"), "test_a : int");

    result = results.get(1);
    assertEquals(result.get("completion"), "test_b");
    assertEquals(result.get("menu"), "test_b : int");
    assertEquals(result.get("info"), "test_b : int");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completePrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_complete", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "156", "-e", "utf-8", "-l", "standard"
      });

    assertEquals("Wrong number of results", 2, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "EXIT_FAILURE");
    assertEquals(result.get("menu"), "EXIT_FAILURE");
    assertEquals(result.get("info"), "");

    result = results.get(1);
    assertEquals(result.get("completion"), "EXIT_SUCCESS");
    assertEquals(result.get("menu"), "EXIT_SUCCESS");
    assertEquals(result.get("info"), "");
  }
}
