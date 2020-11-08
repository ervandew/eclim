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

import java.util.List;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for JUnitTestsCommand.
 *
 * @author Eric Van Dewoestine
 */
public class JUnitTestsCommandTest
{
  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<String> results = (List<String>)
      Eclim.execute(new String[]{
        "java_junit_tests", "-p", Jdt.TEST_PROJECT,
      });

    // remove for consistency since the vunit test adds the imports that makes
    // this a test found by eclipse.
    results.remove("org.eclim.test.junit.SomeClassVUnitTest");

    assertEquals(3, results.size());
    assertEquals("org.eclim.test.junit.SomeClassTest", results.get(0));
    assertEquals("org.eclim.test.junit.run.BarTest", results.get(1));
    assertEquals("org.eclim.test.junit.run.FooTest", results.get(2));
  }
}
