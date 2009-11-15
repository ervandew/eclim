/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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

import org.apache.commons.lang.StringUtils;

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
  public void searchCamelCase()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_search", "-n", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-p", "NPE", "-s", "project"
    });
    System.out.println(result);

    String[] results = StringUtils.split(result, "\n");
    assertEquals("Wrong number of results.", 2, results.length);

    assertTrue("NullPointerException not found.",
        results[0].endsWith("java.lang.NullPointerException"));
    assertTrue("NoPermissionException not found.",
        results[1].endsWith("javax.naming.NoPermissionException"));
  }

  @Test
  public void searchElement()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_search", "-n", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "180", "-e", "utf-8", "-l", "4"
    });
    System.out.println(result);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|8 col 16|org.eclim.test.search.TestSearch#list", result);
  }

  @Test
  public void searchPattern()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_search", "-n", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-p", "org.eclim.test.search.TestSearch%2A"
    });
    System.out.println(result);

    String[] results = StringUtils.split(result, "\n");
    assertEquals("Wrong number of results.", 2, results.length);

    assertTrue("TestSearch not found.",
        results[0].endsWith("org.eclim.test.search.TestSearch"));
    assertTrue("TestSearchVUnit not found.",
        results[1].endsWith("org.eclim.test.search.TestSearchVUnit"));
  }

  @Test
  public void searchField()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_search", "-n", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-p", "TestSearch.list", "-t", "field"
    });
    System.out.println(result);

    assertTrue("field not found.",
        result.endsWith("org.eclim.test.search.TestSearch#list"));
  }
}
