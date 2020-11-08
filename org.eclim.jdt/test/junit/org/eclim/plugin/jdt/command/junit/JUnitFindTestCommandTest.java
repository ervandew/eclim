/**
 * Copyright (C) 2012 - 2020  Eric Van Dewoestine
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

import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for JUnitFindTestCommand.
 *
 * @author Eric Van Dewoestine
 */
public class JUnitFindTestCommandTest
{
  private static final String CLASS =
    "src/org/eclim/test/junit/run/Foo.java";
  private static final String TEST =
    "src/org/eclim/test/junit/run/FooTest.java";

  @Test
  @SuppressWarnings("unchecked")
  public void executeClassToTest()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String, Object> result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_junit_find_test", "-p", Jdt.TEST_PROJECT,
      "-f", CLASS, "-o", "0", "-e", "utf-8",
    });
    assertEquals(Eclim.resolveFile(Jdt.TEST_PROJECT, TEST), result.get("filename"));
    assertEquals("FooTest", result.get("message"));
    assertEquals(7, result.get("line"));
    assertEquals(1, result.get("column"));

    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_junit_find_test", "-p", Jdt.TEST_PROJECT,
      "-f", CLASS, "-o", "70", "-e", "utf-8",
    });
    assertEquals(Eclim.resolveFile(Jdt.TEST_PROJECT, TEST), result.get("filename"));
    assertEquals("foo", result.get("message"));
    assertEquals(9, result.get("line"));
    assertEquals(3, result.get("column"));

    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_junit_find_test", "-p", Jdt.TEST_PROJECT,
      "-f", CLASS, "-o", "163", "-e", "utf-8",
    });
    assertEquals(Eclim.resolveFile(Jdt.TEST_PROJECT, TEST), result.get("filename"));
    assertEquals("fooString", result.get("message"));
    assertEquals(16, result.get("line"));
    assertEquals(3, result.get("column"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void executeTestToClass()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String, Object> result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_junit_find_test", "-p", Jdt.TEST_PROJECT,
      "-f", TEST, "-o", "0", "-e", "utf-8",
    });
    assertEquals(Eclim.resolveFile(Jdt.TEST_PROJECT, CLASS), result.get("filename"));
    assertEquals("Foo", result.get("message"));
    assertEquals(3, result.get("line"));
    assertEquals(1, result.get("column"));

    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_junit_find_test", "-p", Jdt.TEST_PROJECT,
      "-f", TEST, "-o", "178", "-e", "utf-8",
    });
    assertEquals(Eclim.resolveFile(Jdt.TEST_PROJECT, CLASS), result.get("filename"));
    assertEquals("foo", result.get("message"));
    assertEquals(5, result.get("line"));
    assertEquals(3, result.get("column"));

    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_junit_find_test", "-p", Jdt.TEST_PROJECT,
      "-f", TEST, "-o", "374", "-e", "utf-8",
    });
    assertEquals(Eclim.resolveFile(Jdt.TEST_PROJECT, CLASS), result.get("filename"));
    assertEquals("foo", result.get("message"));
    assertEquals(10, result.get("line"));
    assertEquals(3, result.get("column"));
  }
}
