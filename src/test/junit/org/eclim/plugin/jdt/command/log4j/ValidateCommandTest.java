/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ValidateCommand.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ValidateCommandTest
{
  private static final String TEST_FILE = "log4j/log4j.xml";

  @Test
  public void validateXmlErrors ()
  {
    String result = Eclim.execute(new String[]{
      "log4j_validate", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results", 6, results.length);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|23 col 45|Element type \"pram\" must be declared.|e",
        results[0]);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|27 col 14|The content of element type \"appender\" must match " +
        "\"(errorHandler?,param*,layout?,filter*,appender-ref*)\".|e",
        results[1]);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|9 col 1|Type 'org.apache.log4j.RollingFileAppender' not found in project 'eclim_unit_test_java'.|e",
        results[2]);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|14 col 1|Type 'org.apache.log4j.PatternLayout' not found in project 'eclim_unit_test_java'.|e",
        results[3]);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|22 col 1|Type 'org.eclim.util.logging.ConsoleAppender' not found in project 'eclim_unit_test_java'.|e",
        results[4]);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|24 col 1|Type 'org.apache.log4j.PatternLayout' not found in project 'eclim_unit_test_java'.|e",
        results[5]);
  }
}
