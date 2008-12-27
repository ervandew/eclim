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
package org.eclim.plugin.jdt.command.checkstyle;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.eclim.util.StringUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for PropertiesCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CheckstyleCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/checkstyle/TestCheckstyle.java";

  @Test
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_checkstyle", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results.", results.length, 3);
    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE);
    for (String r : results){
      assertTrue("Result does not start with file path.", r.startsWith(file));
    }
  }
}
