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

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.eclim.Eclim;
import org.eclim.plugin.jdt.Jdt;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for JUnitCommand.
 *
 * @author Eric Van Dewoestine
 */
public class JUnitCommandTest
{
  private static final String CLASS =
    "src/org/eclim/test/junit/run/Foo.java";
  private static final String TEST =
    "src/org/eclim/test/junit/run/FooTest.java";

  @Test
  public void executeTest()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = (String)Eclim.execute(new String[]{
      "java_junit", "-p", Jdt.TEST_PROJECT,
      "-t", "org.eclim.test.junit.run.FooTest",
    }, false);

    String[] lines = StringUtils.split(result, '\n');
    assertEquals("Testsuite: org.eclim.test.junit.run.FooTest", lines[1]);
    assertTrue(
        lines[3],
        lines[3].startsWith("Tests run: 3, Failures: 0, Errors: 0"));

    String[] results = new String[lines.length - 4];
    System.arraycopy(lines, 4, results, 0, results.length);
    Arrays.sort(results);
    assertTrue(results[0], results[0].startsWith("Testcase: bar took "));
    assertTrue(results[1], results[1].startsWith("Testcase: foo took "));
    assertTrue(results[2], results[2].startsWith("Testcase: fooString took "));
  }

  @Test
  public void executeMethod()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = (String)Eclim.execute(new String[]{
      "java_junit", "-p", Jdt.TEST_PROJECT,
      "-f", TEST, "-o", "127", "-e", "utf-8",
    }, false);

    String[] lines = StringUtils.split(result, '\n');
    assertEquals("Testsuite: org.eclim.test.junit.run.FooTest", lines[1]);
    assertTrue(lines[3].startsWith("Tests run: 1, Failures: 0, Errors: 0"));
    assertTrue(lines[4].startsWith("Testcase: foo took "));

    result = (String)Eclim.execute(new String[]{
      "java_junit", "-p", Jdt.TEST_PROJECT,
      "-f", CLASS, "-o", "151", "-e", "utf-8",
    }, false);

    lines = StringUtils.split(result, '\n');
    assertEquals("Testsuite: org.eclim.test.junit.run.FooTest", lines[1]);
    assertTrue(lines[3].startsWith("Tests run: 1, Failures: 0, Errors: 0"));
    assertTrue(lines[4].startsWith("Testcase: fooString took "));
  }

  @Test
  public void executePattern()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = (String)Eclim.execute(new String[]{
      "java_junit", "-p", Jdt.TEST_PROJECT, "-t", "**/run/*Test",
    }, false);

    String[] lines = StringUtils.split(result, '\n');
    assertEquals("Testsuite: org.eclim.test.junit.run.BarTest", lines[1]);
    assertTrue(
        lines[3],
        lines[3].startsWith("Tests run: 1, Failures: 0, Errors: 0"));
    assertTrue(lines[4], lines[4].startsWith("Testcase: bar took "));

    assertEquals("Testsuite: org.eclim.test.junit.run.FooTest", lines[6]);
    assertTrue(
        lines[8],
        lines[8].startsWith("Tests run: 3, Failures: 0, Errors: 0"));
    String[] results = new String[lines.length - 9];
    System.arraycopy(lines, 9, results, 0, results.length);
    Arrays.sort(results);
    assertTrue(results[0], results[0].startsWith("Testcase: bar took "));
    assertTrue(results[1], results[1].startsWith("Testcase: foo took "));
    assertTrue(results[2], results[2].startsWith("Testcase: fooString took "));
  }
}
