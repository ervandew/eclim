/**
 * Copyright (C) 2005 - 2015  Eric Van Dewoestine
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
package org.eclim.plugin.dltkruby.command.complete;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.dltkruby.DltkRuby;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "src/complete/testComplete.rb";

  @Test
  @SuppressWarnings("unchecked")
  public void completeStatic()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "107", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 2, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "ID");
    assertEquals(result.get("menu"), "ID");
    assertEquals(result.get("info"), "");

    result = results.get(1);
    assertEquals(result.get("completion"), "moduleMethodA");
    assertEquals(result.get("menu"), "moduleMethodA() - TestModule");
    assertEquals(result.get("info"), "");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completeStaticPrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "92", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 1, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "moduleMethodA");
    assertEquals(result.get("menu"), "moduleMethodA() - TestModule");
    assertEquals(result.get("info"), "");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completeInstance()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "42", "-e", "utf-8"
      });

    assertTrue("Wrong number of results", results.size() > 2);

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "testA");
    assertEquals(result.get("menu"), "testA() - TestClass");
    assertEquals(result.get("info"), "");

    result = results.get(1);
    assertEquals(result.get("completion"), "testB");
    assertEquals(result.get("menu"), "testB() - TestClass");
    assertEquals(result.get("info"), "");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completeInstancePrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "54", "-e", "utf-8"
      });

    assertTrue("Wrong number of results", results.size() >= 2);
    assertTrue("Wrong number of results", results.size() <= 3);

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "testA");
    assertEquals(result.get("menu"), "testA() - TestClass");
    assertEquals(result.get("info"), "");

    result = results.get(1);
    assertEquals(result.get("completion"), "testB");
    assertEquals(result.get("menu"), "testB() - TestClass");
    assertEquals(result.get("info"), "");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completeBuiltins()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "63", "-e", "utf-8"
      });

    assertTrue("Wrong number of results", results.size() >= 2);

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "each");
    assertEquals(result.get("menu"), "each() - Array");
    assertEquals(result.get("info"), "");

    result = results.get(1);
    assertEquals(result.get("completion"), "each_index");
    assertEquals(result.get("menu"), "each_index() - Array");
    assertEquals(result.get("info"), "");

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "70", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 1, results.size());

    result = results.get(0);
    assertEquals(result.get("completion"), "times");
    assertEquals(result.get("menu"), "times() - Integer");
    assertEquals(result.get("info"), "");
  }
}
