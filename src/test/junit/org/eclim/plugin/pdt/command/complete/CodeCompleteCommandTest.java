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
package org.eclim.plugin.pdt.command.complete;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.pdt.Pdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "php/complete/test.php";

  @Test
  public void completeAll()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "213", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 3, results.length);
    assertTrue("Wrong result", results[0].startsWith("methodA1(|"));
    assertTrue("Wrong result", results[1].startsWith("methodA2()|"));
    assertTrue("Wrong result", results[2].startsWith("variable1|"));
  }

  @Test
  public void completePrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "228", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 2, results.length);
    assertTrue("Wrong result", results[0].startsWith("methodA1(|"));
    assertTrue("Wrong result", results[1].startsWith("methodA2()|"));
  }
}
