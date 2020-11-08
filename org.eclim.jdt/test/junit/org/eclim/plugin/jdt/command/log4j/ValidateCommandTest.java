/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.log4j;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ValidateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ValidateCommandTest
{
  private static final String TEST_FILE = "log4j/log4j.xml";

  @Test
  @SuppressWarnings("unchecked")
  public void validateXmlErrors()
  {
    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "log4j_validate", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
    });

    assertEquals("Wrong number of results", 6, results.size());

    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE);

    Map<String, Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "Element type \"pram\" must be declared.");
    assertEquals(error.get("line"), 23);
    assertEquals(error.get("column"), 45);
    assertEquals(error.get("warning"), false);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "The content of element type \"appender\" must match " +
        "\"(errorHandler?,param*,layout?,filter*,appender-ref*)\".");
    assertEquals(error.get("line"), 27);
    assertEquals(error.get("column"), 14);
    assertEquals(error.get("warning"), false);

    error = results.get(2);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "Type 'org.apache.log4j.RollingFileAppender' not found " +
        "in project 'eclim_unit_test_java'.");
    assertEquals(error.get("line"), 9);
    assertEquals(error.get("column"), 1);
    assertEquals(error.get("warning"), false);

    error = results.get(3);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "Type 'org.apache.log4j.PatternLayout' not found " +
        "in project 'eclim_unit_test_java'.");
    assertEquals(error.get("line"), 14);
    assertEquals(error.get("column"), 1);
    assertEquals(error.get("warning"), false);

    error = results.get(4);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "Type 'org.eclim.util.logging.ConsoleAppender' not found " +
        "in project 'eclim_unit_test_java'.");
    assertEquals(error.get("line"), 22);
    assertEquals(error.get("column"), 1);
    assertEquals(error.get("warning"), false);

    error = results.get(5);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "Type 'org.apache.log4j.PatternLayout' not found " +
        "in project 'eclim_unit_test_java'.");
    assertEquals(error.get("line"), 24);
    assertEquals(error.get("column"), 1);
    assertEquals(error.get("warning"), false);
  }
}
