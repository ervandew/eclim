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
package org.eclim.plugin.jdt.command.complete;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/complete/TestCompletion.java";

  @Test
  public void completion1 ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_complete", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "184", "-l", "standard"
    });

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results.", 32, results.length);
    assertEquals("Invalid First Result",
        "f|add(|add(int index, Object element) : void - List|", results[0]);
    assertEquals("Invalid Last Result",
        "f|wait()|wait() : void - Object|", results[results.length - 1]);
  }

  @Test
  public void completion2 ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_complete", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "266", "-l", "standard"
    });

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results.", 4, results.length);
    assertEquals("Invalid First Result",
        "f|add(|add(int index, Object element) : void - List|", results[0]);
    assertEquals("Invalid Last Result",
        "f|addAll(|addAll(Collection c) : boolean - List|",
        results[results.length - 1]);
  }
}
