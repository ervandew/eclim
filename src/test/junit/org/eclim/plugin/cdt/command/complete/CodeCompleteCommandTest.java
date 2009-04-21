/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.cdt.command.complete;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.cdt.Cdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "src/test_complete.c";

  @Test
  public void completeAll()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "c_complete", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "140", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 2, results.length);
    assertTrue("Wrong result", results[0].startsWith("test_a|test_a"));
    assertTrue("Wrong result", results[1].startsWith("test_b|test_b"));
  }

  @Test
  public void completePrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "c_complete", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "156", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 2, results.length);
    assertTrue("Wrong result", results[0].startsWith("EXIT_FAILURE|EXIT_FAILURE"));
    assertTrue("Wrong result", results[1].startsWith("EXIT_SUCCESS|EXIT_SUCCESS"));
  }
}
