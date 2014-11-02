/**
 * Copyright (C) 2011 - 2014  Eric Van Dewoestine
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
package org.eclim.plugin.sdt.command.complete;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.sdt.Sdt;

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
    "src/eclim/test/complete/TestComplete.scala";

  @Test
  @SuppressWarnings("unchecked")
  public void completionScala()
  {
    assertTrue("Scala project doesn't exist.",
        Eclim.projectExists(Sdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "scala_complete", "-p", Sdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "151", "-e", "utf-8", "-l", "standard"
      });

    assertEquals("Wrong number of results.", results.size(), 3);

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "scalaMethod1");
    assertEquals(result.get("menu"), "scalaMethod1(): Unit");
    assertEquals(result.get("info"), "scalaMethod1(): Unit");
    assertEquals(result.get("type"), "");

    result = results.get(1);
    assertEquals(result.get("completion"), "scalaMethod2(");
    assertEquals(result.get("menu"), "scalaMethod2(name: String): Unit");
    assertEquals(result.get("info"), "scalaMethod2(name: String): Unit");
    assertEquals(result.get("type"), "");

    result = results.get(2);
    assertEquals(result.get("completion"), "scalaMethod3(");
    assertEquals(result.get("menu"),
        "scalaMethod3(name: String, value: Comparator[String]): Unit");
    assertEquals(result.get("info"),
        "scalaMethod3(name: String, value: Comparator[String]): Unit");
    assertEquals(result.get("type"), "");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completionJava()
  {
    assertTrue("Scala project doesn't exist.",
        Eclim.projectExists(Sdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "scala_complete", "-p", Sdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "214", "-e", "utf-8", "-l", "standard"
      });

    assertEquals("Wrong number of results.", 3, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "javaMethod1");
    assertEquals(result.get("menu"), "javaMethod1(): Unit");
    assertEquals(result.get("info"), "javaMethod1(): Unit");
    assertEquals(result.get("type"), "");

    result = results.get(1);
    assertEquals(result.get("completion"), "javaMethod2(");
    assertEquals(result.get("menu"), "javaMethod2(name: String): String");
    assertEquals(result.get("info"),
        "javaMethod2(name: String): String");
    assertEquals(result.get("type"), "");

    result = results.get(2);
    assertEquals(result.get("completion"), "javaMethod3(");
    assertEquals(result.get("menu"), "javaMethod3(name: String, value: Comparator[_]): String");
    assertEquals(result.get("info"),
        "javaMethod3(name: String, value: Comparator[_]): String");
    assertEquals(result.get("type"), "");
  }
}
