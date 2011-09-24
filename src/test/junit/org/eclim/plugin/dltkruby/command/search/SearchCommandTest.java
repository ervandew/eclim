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
package org.eclim.plugin.dltkruby.command.search;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.dltkruby.DltkRuby;

import org.junit.Test;

import static org.junit.Assert.*;

public class SearchCommandTest
{
  private static final String TEST_FILE = "src/search/testSearch.rb";

  @Test
  @SuppressWarnings("unchecked")
  public void searchClass()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "TestClass", "-t", "class"
      });

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "type TestClass");
    assertEquals(result.get("line"), 11);
    assertEquals(result.get("column"), 7);

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "23", "-l", "9", "-e", "utf-8", "-x", "declarations"
      });

    result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "type TestClass");
    assertEquals(result.get("line"), 11);
    assertEquals(result.get("column"), 7);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchModule()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "TestModule",
      });

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "type TestModule");
    assertEquals(result.get("line"), 1);
    assertEquals(result.get("column"), 8);

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "69", "-l", "10", "-e", "utf-8", "-x", "declarations"
      });

    result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "type TestModule");
    assertEquals(result.get("line"), 1);
    assertEquals(result.get("column"), 8);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchMethod()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "testA", "-t", "method"
      });

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "type TestClass : method testA");
    assertEquals(result.get("line"), 13);
    assertEquals(result.get("column"), 7);

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "42", "-l", "5", "-e", "utf-8", "-x", "declarations"
      });

    result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "type TestClass : method testA");
    assertEquals(result.get("line"), 13);
    assertEquals(result.get("column"), 7);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchFunction()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "testFunction", "-t", "function"
      });

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "function testFunction");
    assertEquals(result.get("line"), 21);
    assertEquals(result.get("column"), 5);

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "104", "-l", "12", "-e", "utf-8", "-x", "declarations"
      });

    result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "function testFunction");
    assertEquals(result.get("line"), 21);
    assertEquals(result.get("column"), 5);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchField()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "CONSTANT", "-t", "field"
      });

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "type TestClass : field CONSTANT");
    assertEquals(result.get("line"), 12);
    assertEquals(result.get("column"), 3);

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "59", "-l", "8", "-e", "utf-8", "-x", "declarations"
      });

    result = results.get(0);
    assertEquals(result.get("filename"), file);
    assertEquals(result.get("message"), "type TestClass : field CONSTANT");
    assertEquals(result.get("line"), 12);
    assertEquals(result.get("column"), 3);
  }
}
