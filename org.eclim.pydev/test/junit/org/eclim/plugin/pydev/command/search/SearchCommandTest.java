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
package org.eclim.plugin.pydev.command.search;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.pydev.Pydev;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SearchCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SearchCommandTest
{
  private static final String TEST_FILE =
    "test/search/test_search.py";

  @Test
  @SuppressWarnings("unchecked")
  public void searchDefinition()
  {
    assertTrue("Python project doesn't exist.",
        Eclim.projectExists(Pydev.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "python_search", "-n", Pydev.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "17", "-l", "6", "-e", "utf-8",
      });

    Map<String, Object> result = results.get(0);
    assertEquals(result.get("filename"),
        Eclim.resolveFile(Pydev.TEST_PROJECT, "test/common/__init__.py"));
    assertEquals(result.get("line"), 1);
    assertEquals(result.get("column"), 1);

    results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "python_search", "-n", Pydev.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "37", "-l", "5", "-e", "utf-8",
      });

    result = results.get(0);
    assertEquals(result.get("filename"),
        Eclim.resolveFile(Pydev.TEST_PROJECT, "test/common/objects.py"));
    assertEquals(result.get("line"), 3);
    assertEquals(result.get("column"), 7);

    results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "python_search", "-n", Pydev.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "53", "-l", "5", "-e", "utf-8",
      });

    result = results.get(0);
    assertEquals(result.get("filename"),
        Eclim.resolveFile(Pydev.TEST_PROJECT, "test/common/functions.py"));
    assertEquals(result.get("line"), 9);
    assertEquals(result.get("column"), 5);
  }
}
