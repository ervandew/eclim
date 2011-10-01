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
package org.eclim.plugin.jdt.command.junit;

import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for JUnitImplCommand.
 *
 * @author Eric Van Dewoestine
 */
public class JUnitImplCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/junit/SomeClassTest.java";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_junit_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-b", "org.eclim.test.junit.SomeClass"
      });

    assertEquals("org.eclim.test.junit.SomeClassTest", result.get("type"));

    List<Map<String,Object>> types =
      (List<Map<String,Object>>)result.get("superTypes");
    assertEquals(3, types.size());

    assertEquals("org.eclim.test.junit", types.get(0).get("packageName"));
    assertEquals("public class SomeClass", types.get(0).get("signature"));
    List<Map<String,Object>> methods =
      (List<Map<String,Object>>)types.get(0).get("methods");
    assertEquals("public void aMethod()", methods.get(0).get("signature"));
    assertEquals(false, methods.get(0).get("implemented"));
    assertEquals("public void aMethod(String name)", methods.get(1).get("signature"));
    assertEquals(false, methods.get(1).get("implemented"));
    assertEquals("public void anotherMethod(int id)", methods.get(2).get("signature"));
    assertEquals(false, methods.get(2).get("implemented"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_junit_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-b", "org.eclim.test.junit.SomeClass",
        "-t", "org.eclim.test.junit.SomeClassTest",
        "-s", "org.eclim.test.junit.SomeClass",
        "-m", "aMethod(String)"
      });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("@Test\n\\s+public void aMethod\\(\\)")
        .matcher(contents).find());

    types = (List<Map<String,Object>>)result.get("superTypes");

    assertEquals("org.eclim.test.junit", types.get(0).get("packageName"));
    assertEquals("public class SomeClass", types.get(0).get("signature"));
    methods = (List<Map<String,Object>>)types.get(0).get("methods");
    assertEquals("public void aMethod()", methods.get(0).get("signature"));
    assertEquals(true, methods.get(0).get("implemented"));
    assertEquals("public void aMethod(String name)", methods.get(1).get("signature"));
    assertEquals(true, methods.get(1).get("implemented"));
    assertEquals("public void anotherMethod(int id)", methods.get(2).get("signature"));
    assertEquals(false, methods.get(2).get("implemented"));
  }
}
