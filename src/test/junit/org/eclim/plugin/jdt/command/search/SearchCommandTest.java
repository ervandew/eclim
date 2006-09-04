/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SearchCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/search/TestSearch.java";

  @Test
  public void searchCamelCase ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_search", "-n", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
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
  public void searchElement ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_search", "-n", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-o", "180", "-l", "4"
    });
    System.out.println(result);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|8 col 16|org.eclim.test.search.TestSearch#list", result);
  }

  @Test
  public void searchPattern ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_search", "-n", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-p", "org.eclim.eclipse.Eclim%2A"
    });
    System.out.println(result);

    String[] results = StringUtils.split(result, "\n");
    assertTrue("Not enough results.", results.length == 2);

    assertTrue("EclimApplication not found.",
        results[0].endsWith("org.eclim.eclipse.EclimApplication"));
    assertTrue("EclimPlugin not found.",
        results[1].endsWith("org.eclim.eclipse.EclimPlugin"));
  }

  @Test
  public void searchField ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_search", "-n", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-p", "Jdt.TEST_PROJECT", "-t", "field"
    });
    System.out.println(result);

    assertTrue("EclimApplication not found.",
        result.endsWith("org.eclim.plugin.jdt.Jdt#TEST_PROJECT"));
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(SearchCommandTest.class);
  }
}
