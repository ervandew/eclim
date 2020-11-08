/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.ant.command.complete;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.ant.Ant;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "build.xml";

  @Test
  @SuppressWarnings("unchecked")
  public void completeProperty()
  {
    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "ant_complete", "-p", Ant.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "220", "-e", "utf-8",
      });

    Map<String, Object> result = results.get(0);
    assertEquals(result.get("completion"), "test.ant.property");
    assertEquals(result.get("menu"), "");
    assertEquals(result.get("info"), "Test Value");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completeTarget()
  {
    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "ant_complete", "-p", Ant.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "234", "-e", "utf-8",
      });

    assertEquals("Wrong number of results.", 8, results.size());

    assertEquals(results.get(0).get("completion"), "java");
    assertEquals(results.get(1).get("completion"), "javac");
    assertEquals(results.get(2).get("completion"), "javacc");
    assertEquals(results.get(3).get("completion"), "javaconstant");
    assertEquals(results.get(4).get("completion"), "javadoc");
    assertEquals(results.get(5).get("completion"), "javadoc2");
    assertEquals(results.get(6).get("completion"), "javah");
    assertEquals(results.get(7).get("completion"), "javaresource");
  }
}
