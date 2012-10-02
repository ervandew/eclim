/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
package org.eclim.plugin.pdt.command.search;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.pdt.Pdt;

import org.junit.Test;

import static org.junit.Assert.*;

public class SearchCommandTest
{
  private static final String TEST_FILE = "php/search/find.php";

  @Test
  @SuppressWarnings("unchecked")
  public void searchElementClass()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "45", "-l", "5", "-e", "utf-8"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "class TestA");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 7);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchElementMethod()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "62", "-l", "8", "-e", "utf-8"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "class TestA -> method methodA2");
    assertEquals(result.get("line"), 13);
    assertEquals(result.get("column"), 19);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchElementVariable()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "82", "-l", "9", "-e", "utf-8"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "class TestA -> field $variable1");
    assertEquals(result.get("line"), 8);
    assertEquals(result.get("column"), 7);
  }

  // as of pdt 3.1.1 (eclipse 4.2.1), element based search for a constant
  // doesn't work, but the hyperlink in the gui editor does.
  /*@Test
  @SuppressWarnings("unchecked")
  public void searchElementConstant()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "111", "-l", "9", "-e", "utf-8"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "field CONSTANT1");
    assertEquals(result.get("line"), 60);
    assertEquals(result.get("column"), 1);
  }*/

  @Test
  @SuppressWarnings("unchecked")
  public void searchElementFunction()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "94", "-l", "9", "-e", "utf-8"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "function functionA");
    assertEquals(result.get("line"), 3);
    assertEquals(result.get("column"), 10);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchClass()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-p", "TestA", "-t", "class"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "class TestA");
    assertEquals(result.get("line"), 6);
    assertEquals(result.get("column"), 7);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchMethod()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-p", "methodA1", "-t", "function"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "class TestA -> method methodA1");
    assertEquals(result.get("line"), 9);
    assertEquals(result.get("column"), 19);
  }

  // as of pdt 3.1.1 (eclipse 4.2.1), pattern based search for a constant
  // doesn't work, including in the eclipse gui.
  /*@Test
  @SuppressWarnings("unchecked")
  public void searchConstant()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-p", "CONSTANT1", "-t", "field"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "field CONSTANT1");
    assertEquals(result.get("line"), 60);
    assertEquals(result.get("column"), 1);
  }*/

  @Test
  @SuppressWarnings("unchecked")
  public void searchFunction()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_search", "-n", Pdt.TEST_PROJECT, "-p", "functionA", "-t", "function"
      });

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "function functionA");
    assertEquals(result.get("line"), 3);
    assertEquals(result.get("column"), 10);
  }
}
