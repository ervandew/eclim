/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.complete;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

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
    "src/org/eclim/test/complete/TestCompletion.java";

  @Test
  @SuppressWarnings("unchecked")
  public void completion1()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "java_complete", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "184", "-e", "utf-8", "-l", "standard"
      });

    assertTrue("Wrong number of results.", results.size() > 30);

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "add(");
    assertEquals(result.get("menu"), "add(int index, Object element) : void - List");
    assertEquals(result.get("info"), "add(int index, Object element) : void - List");
    assertEquals(result.get("type"), "f");

    result = results.get(results.size() - 1);
    assertEquals(result.get("completion"), "wait()");
    assertEquals(result.get("menu"), "wait() : void - Object");
    assertEquals(result.get("info"), "wait() : void - Object");
    assertEquals(result.get("type"), "f");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completion2()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "java_complete", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "266", "-e", "utf-8", "-l", "standard"
      });

    assertEquals("Wrong number of results.", 4, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "add(");
    assertEquals(result.get("menu"), "add(int index, Object element) : void - List");
    assertEquals(result.get("info"), "add(int index, Object element) : void - List");
    assertEquals(result.get("type"), "f");

    result = results.get(results.size() - 1);
    assertEquals(result.get("completion"), "addAll(");
    assertEquals(result.get("menu"), "addAll(Collection c) : boolean - List");
    assertEquals(result.get("info"), "addAll(Collection c) : boolean - List");
    assertEquals(result.get("type"), "f");
  }
}
