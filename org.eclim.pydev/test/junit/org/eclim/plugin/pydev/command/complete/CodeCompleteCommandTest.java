/**
 * Copyright (C) 2014 - 2015  Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.complete;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.pydev.Pydev;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE =
    "test/complete/test_complete.py";

  @Test
  @SuppressWarnings("unchecked")
  public void completionAllMembers()
  {
    assertTrue("Python project doesn't exist.",
        Eclim.projectExists(Pydev.TEST_PROJECT));

    List<Map<String,String>> results = (List<Map<String,String>>)
      Eclim.execute(new String[]{
        "python_complete", "-p", Pydev.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "32", "-e", "utf-8",
      });

    assertEquals(results.size(), 12);

    assertEquals(results.get(0).get("completion"), "__dict__");
    assertEquals(results.get(1).get("completion"), "__file__");
    assertEquals(results.get(2).get("completion"), "__name__");
    assertEquals(results.get(3).get("completion"), "__path__");
    assertEquals(results.get(4).get("completion"), "functions");
    assertEquals(results.get(5).get("completion"), "objects");

    Map<String,String> result = results.get(6);
    assertEquals(result.get("completion"), "Test1");
    assertTrue(result.get("info").startsWith("class Test1(object):"));

    result = results.get(7);
    assertEquals(result.get("completion"), "test1()");
    assertTrue(result.get("info").startsWith("def test1():"));

    result = results.get(9);
    assertEquals(result.get("completion"), "test2(");
    assertTrue(result.get("info").startsWith("def test2(foo, bar='baz'):"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completionByPrefix()
  {
    assertTrue("Python project doesn't exist.",
        Eclim.projectExists(Pydev.TEST_PROJECT));

    List<Map<String,String>> results = (List<Map<String,String>>)
      Eclim.execute(new String[]{
        "python_complete", "-p", Pydev.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "33", "-e", "utf-8",
      });

    assertEquals(results.size(), 3);

    Map<String,String> result = results.get(0);
    assertEquals(result.get("completion"), "test1()");
    assertTrue(result.get("info").startsWith("def test1():"));

    result = results.get(1);
    assertEquals(result.get("completion"), "test2(");
    assertTrue(result.get("info").startsWith("def test2(foo, bar='baz'):"));
  }
}
