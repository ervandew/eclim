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

import org.eclim.util.StringUtils;

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
  private static final String TEST_FILE_C = "src/test.c";
  private static final String TEST_FILE_H = "src/test.h";

  @Test
  public void searchElement()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    // testFunction definition (c file result)
    String result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "136", "-l", "12", "-e", "utf-8", "-x", "definitions"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE_C);
    assertEquals("Wrong Result", file + "|1 col 6|", result);

    // testFunction declarations (header file result)
    result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "136", "-l", "12", "-e", "utf-8", "-x", "declarations"
    });

    System.out.println(result);

    file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE_H);
    assertEquals("Wrong Result", file + "|4 col 6|", result);

    // testFunction references
    result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "136", "-l", "12", "-e", "utf-8", "-x", "references"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results", results.length, 2);
    String file1 = Eclim.resolveFile(Cdt.TEST_PROJECT, "src/test_search_vunit.c");
    String file2 = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);
    assertEquals("Wrong Result", file1 + "|11 col 3|", results[0]);
    assertEquals("Wrong Result", file2 + "|11 col 3|", results[1]);

    // testFunction all
    result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "136", "-l", "12", "-e", "utf-8", "-x", "all"
    });

    System.out.println(result);

    results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results", results.length, 4);

    file1 = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE_H);
    file2 = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE_C);
    String file3 = Eclim.resolveFile(Cdt.TEST_PROJECT, "src/test_search_vunit.c");
    String file4 = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);
    assertEquals("Wrong Result", file3 + "|11 col 3|", results[2]);
    assertEquals("Wrong Result", file4 + "|11 col 3|", results[3]);

    // EXIT_SUCCESS
    result = Eclim.execute(new String[]{
      "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "186", "-l", "12", "-e", "utf-8", "-x", "declarations"
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
    assertEquals("Wrong Result", file + "|16 col 5|", result);
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
    assertEquals("Wrong Result", file + "|5 col 8|", result);
  }
}
