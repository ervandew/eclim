/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import java.util.List;
import java.util.Map;

import org.apache.tools.ant.taskdefs.condition.Os;

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
  private static final String TEST_FILE_C = "src/test.c";
  private static final String TEST_FILE_H = "src/test.h";

  @Test
  @SuppressWarnings("unchecked")
  public void searchElement()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    // testFunction definition (c file result)
    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "136", "-l", "12", "-e", "utf-8", "-x", "definitions"
      });

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE_C);

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "");
    assertEquals(result.get("line"), 1);
    assertEquals(result.get("column"), 6);

    // testFunction declarations (header file result)
    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "136", "-l", "12", "-e", "utf-8", "-x", "declarations"
      });

    file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE_H);

    result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "");
    assertEquals(result.get("line"), 4);
    assertEquals(result.get("column"), 6);

    // testFunction references
    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "136", "-l", "12", "-e", "utf-8", "-x", "references"
      });

    assertEquals("Wrong number of results", 2, results.size());

    file = Eclim.resolveFile(Cdt.TEST_PROJECT, "src/test_search_vunit.c");
    result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "");
    assertEquals(result.get("line"), 11);
    assertEquals(result.get("column"), 3);

    file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);
    result = results.get(1);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "");
    assertEquals(result.get("line"), 11);
    assertEquals(result.get("column"), 3);

    // testFunction all
    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "136", "-l", "12", "-e", "utf-8", "-x", "all"
      });

    assertEquals("Wrong number of results", 4, results.size());

    file = Eclim.resolveFile(Cdt.TEST_PROJECT, "src/test_search_vunit.c");
    result = results.get(2);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "");
    assertEquals(result.get("line"), 11);
    assertEquals(result.get("column"), 3);

    file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);
    result = results.get(3);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "");
    assertEquals(result.get("line"), 11);
    assertEquals(result.get("column"), 3);

    // puts
    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "154", "-l", "4", "-e", "utf-8", "-x", "declarations"
      });

    result = results.get(0);
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      assertTrue(((String)result.get("filename"))
          .endsWith("/include/stdio.h"));
      assertEquals(result.get("message"), "");
    }else{
      assertEquals(result.get("filename"), "/usr/include/stdio.h");
      assertEquals(result.get("message"), "");
      assertEquals(result.get("column"), 12);
      int line = ((Integer)result.get("line")).intValue();
      assertTrue(line > 650 && line < 750);
    }

    // EXIT_SUCCESS
    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "186", "-l", "12", "-e", "utf-8", "-x", "declarations"
      });

    result = results.get(0);
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      assertTrue(((String)result.get("filename"))
          .endsWith("/include/stdlib.h"));
      assertEquals(result.get("message"), "");
      assertEquals(result.get("line"), 33);
      assertEquals(result.get("column"), 9);
    }else{
      assertEquals(result.get("filename"), "/usr/include/stdlib.h");
      assertEquals(result.get("message"), "");
      assertEquals(result.get("column"), 9);
      int line = ((Integer)result.get("line")).intValue();
      assertTrue(line > 50 && line < 150);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchFunction()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT,
        "-p", "test_search_function", "-t", "function"
      });

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "int test_search_function(void) {");
    assertEquals(result.get("line"), 16);
    assertEquals(result.get("column"), 5);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchConstant()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT, "-p", "EXIT_FAILURE", "-t", "macro"
      });

    Map<String,Object> result = results.get(0);
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      assertTrue(((String)result.get("filename"))
          .endsWith("/include/stdlib.h"));
      assertEquals(result.get("line"), 34);
      assertEquals(result.get("column"), 9);
    }else{
      assertEquals(result.get("filename"), "/usr/include/stdlib.h");
      assertEquals(result.get("message"),
          "#define	EXIT_FAILURE	1	/* Failing exit status.  */");
      assertEquals(result.get("column"), 9);
      int line = ((Integer)result.get("line")).intValue();
      assertTrue(line > 50 && line < 150);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchStruct()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_search", "-n", Cdt.TEST_PROJECT,
        "-p", "test_search_struct", "-t", "class_struct"
      });

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "struct test_search_struct {");
    assertEquals(result.get("line"), 5);
    assertEquals(result.get("column"), 8);
  }
}
