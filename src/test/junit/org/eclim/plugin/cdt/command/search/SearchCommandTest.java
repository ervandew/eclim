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
package org.eclim.plugin.cdt.command.search;

import org.eclim.Eclim;

import org.eclim.plugin.cdt.Cdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SearchCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SearchCommandTest
{
  private static final String TEST_FILE = "src/test_search.c";

  @Test
  public void searchElement()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "150", "-l", "12", "-e", "utf-8", "-x", "declarations"
    });

    System.out.println(result);

    assertEquals("Wrong Result", "/usr/include/stdlib.h|135 col 9|", result);
  }

  @Test
  public void searchFunction()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT,
      "-p", "test_search_function", "-t", "function"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);
    assertEquals("Wrong Result", file + "|14 col 5|", result);
  }

  @Test
  public void searchConstant()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT, "-p", "EXIT_FAILURE", "-t", "macro"
    });

    System.out.println(result);

    assertEquals("Wrong Result", "/usr/include/stdlib.h|134 col 9|", result);
  }

  @Test
  public void searchStruct()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT,
      "-p", "test_search_struct", "-t", "class_struct"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);
    assertEquals("Wrong Result", file + "|4 col 8|", result);
  }
}
