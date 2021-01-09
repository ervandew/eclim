/**
 * Copyright (C) 2005 - 2021  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.complete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/complete/TestCompletion.java";

  @Test
  @SuppressWarnings("unchecked")
  public void completionAllMembers()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String, Object> results = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_complete", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "184", "-e", "utf-8", "-l", "standard",
      });
    List<Map<String, Object>> completions = (List<Map<String, Object>>)
      results.get("completions");

    assertTrue("Wrong number of completions.", completions.size() > 30);

    Map<String, Object> result = completions.get(0);
    assertEquals(result.get("completion"), "add(");
    assertEquals(result.get("menu"), "add(int index, Object element) : void - List");
    assertEquals(result.get("info"), "add(int index, Object element) : void - List");
    assertEquals(result.get("type"), "f");

    result = completions.get(completions.size() - 1);
    assertEquals(result.get("completion"), "wait()");
    assertEquals(result.get("menu"), "wait() : void - Object");
    assertEquals(result.get("info"), "wait() : void - Object");
    assertEquals(result.get("type"), "f");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completionByPrefix()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String, Object> results = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_complete", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "266", "-e", "utf-8", "-l", "standard",
      });
    List<Map<String, Object>> completions = (List<Map<String, Object>>)
      results.get("completions");

    // as of eclipse neon (4.6), any method containing the prefix anywhere in
    // the method name ('a' in this case) is returned as a proposal, with the
    // prefixed versions at the top.
    assertEquals("Wrong number of completions.", 30, completions.size());

    Map<String, Object> result = completions.get(0);
    assertEquals(result.get("completion"), "add");
    assertEquals(result.get("menu"), "add(int index, Object element) : void - List");
    assertEquals(result.get("info"), "add(int index, Object element) : void - List");
    assertEquals(result.get("type"), "f");

    result = completions.get(3);
    assertEquals(result.get("completion"), "addAll");
    assertEquals(result.get("menu"), "addAll(Collection c) : boolean - List");
    assertEquals(result.get("info"), "addAll(Collection c) : boolean - List");
    assertEquals(result.get("type"), "f");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completionMissingImport()
  {
    Map<String, Object> results = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_complete", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "371", "-e", "utf-8", "-l", "standard",
      });
    List<Map<String, Object>> completions = (List<Map<String, Object>>)
      results.get("completions");

    assertEquals("Wrong number of completions.", 0, completions.size());
    assertTrue("Missing key 'imports'", results.containsKey("imports"));
    ArrayList<String> imports = new ArrayList<String>();
    imports.add("java.util.Map");
    assertEquals("Wrong imports", imports, results.get("imports"));

    assertTrue("Missing key 'error'", results.containsKey("error"));
    Map<String, Object> error = (Map<String, Object>)results.get("error");
    assertEquals("Wrong error message",
        "Map cannot be resolved to a type", error.get("message"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completionMissingImportStatic()
  {
    Map<String, Object> results = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_complete", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "436", "-e", "utf-8", "-l", "standard",
      });
    List<Map<String, Object>> completions = (List<Map<String, Object>>)
      results.get("completions");

    assertEquals("Wrong number of completions.", 0, completions.size());
    assertTrue("Missing key 'imports'", results.containsKey("imports"));
    ArrayList<String> imports = new ArrayList<String>();
    imports.add("java.awt.Component");
    assertEquals("Wrong imports", imports, results.get("imports"));

    assertTrue("Missing key 'error'", results.containsKey("error"));
    Map<String, Object> error = (Map<String, Object>)results.get("error");
    assertEquals("Wrong error message",
        "Component cannot be resolved to a variable", error.get("message"));
  }
}
