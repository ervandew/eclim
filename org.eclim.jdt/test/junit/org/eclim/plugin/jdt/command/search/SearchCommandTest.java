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
package org.eclim.plugin.jdt.command.search;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

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
    "src/org/eclim/test/search/TestSearch.java";
  private static final String TEST_FILE_IMPLEMENTORS =
    "src/org/eclim/test/search/implementors/TestSearchImplementors.java";

  @Test
  @SuppressWarnings("unchecked")
  public void searchCamelCase()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-p", "NPE", "-s", "project",
      });

    assertEquals("Wrong number of results.", 2, results.size());

    Map<String, Object> result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("/java/lang/NullPointerException.java"));
    assertEquals(result.get("message"), "java.lang.NullPointerException");
    assertEquals(result.get("line"), 53);
    assertEquals(result.get("column"), 7);

    result = results.get(1);
    assertTrue(((String)result.get("filename"))
        .endsWith("/javax/naming/NoPermissionException.java"));
    assertEquals(result.get("message"), "javax.naming.NoPermissionException");
    assertEquals(result.get("line"), 42);
    assertEquals(result.get("column"), 14);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchElement()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "180", "-e", "utf-8", "-l", "4",
      });

    Map<String, Object> result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("/org/eclim/test/search/TestSearch.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearch#list");
    assertEquals(result.get("line"), 8);
    assertEquals(result.get("column"), 16);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchPattern()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-p", "org.eclim.test.search.TestSearch%2A",
      });

    assertEquals("Wrong number of results.", 3, results.size());

    Map<String, Object> result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("src/org/eclim/test/search/TestSearch.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearch");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 14);

    result = results.get(1);
    assertTrue(((String)result.get("filename"))
        .endsWith("src/org/eclim/test/search/TestSearchVUnit.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearchVUnit");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 14);

    result = results.get(2);
    assertTrue(((String)result.get("filename"))
        .endsWith("test/org/eclim/test/search/TestSearchTest.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearchTest");
    assertEquals(result.get("line"), 3);
    assertEquals(result.get("column"), 14);

    // test changing the sort
    Eclim.execute(new String[]{
      "project_setting",
      "-p", Jdt.TEST_PROJECT,
      "-s", "org.eclim.java.search.sort",
      "-v", "[\"test\", \"src\"]",
    });

    results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-p", "org.eclim.test.search.TestSearch%2A",
      });

    assertEquals("Wrong number of results.", 3, results.size());

    result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("test/org/eclim/test/search/TestSearchTest.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearchTest");
    assertEquals(result.get("line"), 3);
    assertEquals(result.get("column"), 14);

    result = results.get(1);
    assertTrue(((String)result.get("filename"))
        .endsWith("src/org/eclim/test/search/TestSearch.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearch");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 14);

    result = results.get(2);
    assertTrue(((String)result.get("filename"))
        .endsWith("src/org/eclim/test/search/TestSearchVUnit.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearchVUnit");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 14);

    // change the sort again
    Eclim.execute(new String[]{
      "project_setting",
      "-p", Jdt.TEST_PROJECT,
      "-s", "org.eclim.java.search.sort",
      "-v", "[\"src\", \"test\"]",
    });

    results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-p", "org.eclim.test.search.TestSearch%2A",
      });

    assertEquals("Wrong number of results.", 3, results.size());

    result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("src/org/eclim/test/search/TestSearch.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearch");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 14);

    result = results.get(1);
    assertTrue(((String)result.get("filename"))
        .endsWith("src/org/eclim/test/search/TestSearchVUnit.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearchVUnit");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 14);

    result = results.get(2);
    assertTrue(((String)result.get("filename"))
        .endsWith("test/org/eclim/test/search/TestSearchTest.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearchTest");
    assertEquals(result.get("line"), 3);
    assertEquals(result.get("column"), 14);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchField()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-p", "TestSearch.list", "-t", "field",
      });

    Map<String, Object> result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("/org/eclim/test/search/TestSearch.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearch#list");
    assertEquals(result.get("line"), 8);
    assertEquals(result.get("column"), 16);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchImplementors()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE_IMPLEMENTORS,
        "-x", "implementors",
        "-o", "173", "-e", "utf-8", "-l", "4",
      });
    assertEquals(results.size(), 2);

    Map<String, Object> result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("/org/eclim/test/search/implementors/TestImplementor1.java"));
    assertEquals(result.get("message"),
        "org.eclim.test.search.implementors.TestImplementor1#test()");
    assertEquals(result.get("line"), 7);
    assertEquals(result.get("column"), 15);

    result = results.get(1);
    assertTrue(((String)result.get("filename"))
        .endsWith("/org/eclim/test/search/implementors/TestImplementor2.java"));
    assertEquals(result.get("message"),
        "org.eclim.test.search.implementors.TestImplementor2#test()");
    assertEquals(result.get("line"), 7);
    assertEquals(result.get("column"), 15);
  }
}
