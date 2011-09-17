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
package org.eclim.plugin.jdt.command.search;

import java.util.HashMap;
import java.util.List;

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

  @Test
  @SuppressWarnings("unchecked")
  public void searchCamelCase()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<HashMap<String,Object>> results = (List<HashMap<String,Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-p", "NPE", "-s", "project"
      });

    assertEquals("Wrong number of results.", 2, results.size());

    HashMap<String,Object> result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("/java/lang/NullPointerException.java"));
    assertEquals(result.get("message"), "java.lang.NullPointerException");
    assertEquals(result.get("line"), 31);
    assertEquals(result.get("column"), 7);

    result = results.get(1);
    assertTrue(((String)result.get("filename"))
        .endsWith("/javax/naming/NoPermissionException.java"));
    assertEquals(result.get("message"), "javax.naming.NoPermissionException");
    assertEquals(result.get("line"), 25);
    assertEquals(result.get("column"), 14);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchElement()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<HashMap<String,Object>> results = (List<HashMap<String,Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "180", "-e", "utf-8", "-l", "4"
      });

    HashMap<String,Object> result = results.get(0);
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

    List<HashMap<String,Object>> results = (List<HashMap<String,Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-p", "org.eclim.test.search.TestSearch%2A"
      });

    assertEquals("Wrong number of results.", 2, results.size());

    HashMap<String,Object> result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("/org/eclim/test/search/TestSearch.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearch");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 14);

    result = results.get(1);
    assertTrue(((String)result.get("filename"))
        .endsWith("/org/eclim/test/search/TestSearchVUnit.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearchVUnit");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 14);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchField()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<HashMap<String,Object>> results = (List<HashMap<String,Object>>)
      Eclim.execute(new String[]{
        "java_search", "-n", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-p", "TestSearch.list", "-t", "field"
      });

    HashMap<String,Object> result = results.get(0);
    assertTrue(((String)result.get("filename"))
        .endsWith("/org/eclim/test/search/TestSearch.java"));
    assertEquals(result.get("message"), "org.eclim.test.search.TestSearch#list");
    assertEquals(result.get("line"), 8);
    assertEquals(result.get("column"), 16);
  }
}
