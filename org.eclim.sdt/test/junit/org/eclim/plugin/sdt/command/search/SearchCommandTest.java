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
package org.eclim.plugin.sdt.command.search;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.sdt.Sdt;

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
    "src/eclim/test/search/TestSearch.scala";

  @Test
  @SuppressWarnings("unchecked")
  public void searchScala()
  {
    assertTrue("Scala project doesn't exist.",
        Eclim.projectExists(Sdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "scala_search", "-n", Sdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "128", "-l", "9", "-e", "utf-8"
      });

    assertEquals("Wrong number of results.", 1, results.size());
    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"),
        Eclim.resolveFile(Sdt.TEST_PROJECT, "src/eclim/test/TestScala.scala"));
    assertEquals(result.get("line"), 5);
    assertEquals(result.get("column"), 7);
    assertEquals(result.get("message"), "class eclim.test.TestScala");

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "scala_search", "-n", Sdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "147", "-l", "12", "-e", "utf-8"
      });

    assertEquals("Wrong number of results.", 1, results.size());
    result = results.get(0);
    assertEquals(result.get("filename"),
        Eclim.resolveFile(Sdt.TEST_PROJECT, "src/eclim/test/TestScala.scala"));
    assertEquals(result.get("line"), 7);
    assertEquals(result.get("column"), 7);
    assertEquals(result.get("message"), "method eclim.test.TestScala.scalaMethod1");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchJava()
  {
    assertTrue("Scala project doesn't exist.",
        Eclim.projectExists(Sdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "scala_search", "-n", Sdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "202", "-l", "8", "-e", "utf-8"
      });

    assertEquals("Wrong number of results.", 1, results.size());
    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"),
        Eclim.resolveFile(Sdt.TEST_PROJECT, "src/eclim/test/TestJava.java"));
    assertEquals(result.get("line"), 5);
    assertEquals(result.get("column"), 1);
    assertEquals(result.get("message"), "eclim.test.TestJava");

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "scala_search", "-n", Sdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "220", "-l", "11", "-e", "utf-8"
      });

    assertEquals("Wrong number of results.", 1, results.size());
    result = results.get(0);
    assertEquals(result.get("filename"),
        Eclim.resolveFile(Sdt.TEST_PROJECT, "src/eclim/test/TestJava.java"));
    assertEquals(result.get("line"), 7);
    assertEquals(result.get("column"), 3);
    assertEquals(result.get("message"), "eclim.test.TestJava#javaMethod1()");
  }
}
