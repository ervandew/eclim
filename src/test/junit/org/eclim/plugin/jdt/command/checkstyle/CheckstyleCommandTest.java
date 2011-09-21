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
package org.eclim.plugin.jdt.command.checkstyle;

import java.util.HashMap;
import java.util.List;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for PropertiesCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CheckstyleCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/checkstyle/TestCheckstyle.java";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<HashMap<String,Object>> results = (List<HashMap<String,Object>>)
      Eclim.execute(new String[]{
        "java_checkstyle", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      });

    assertEquals("Wrong number of results.", 4, results.size());

    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE);

    HashMap<String,Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"), "'(' is not preceded with whitespace.");
    assertEquals(error.get("line"), 21);
    assertEquals(error.get("column"), 19);
    assertEquals(error.get("warning"), true);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "Redundant throws: 'TestException' is unchecked exception.");
    assertEquals(error.get("line"), 22);
    assertEquals(error.get("column"), 12);
    assertEquals(error.get("warning"), true);

    error = results.get(2);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"), "'{' should be on a new line.");
    assertEquals(error.get("line"), 22);
    assertEquals(error.get("column"), 26);
    assertEquals(error.get("warning"), true);

    error = results.get(3);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"), "',' is not followed by whitespace.");
    assertEquals(error.get("line"), 25);
    assertEquals(error.get("column"), 33);
    assertEquals(error.get("warning"), true);
  }
}
