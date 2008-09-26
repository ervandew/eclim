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
package org.eclim.plugin.ant.command.complete;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.ant.Ant;

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
  private static final String TEST_FILE = "build.xml";

  @Test
  public void completeProperty ()
  {
    String result = Eclim.execute(new String[]{
      "ant_complete", "-p", Ant.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "220", "-e", "utf-8"
    });

    System.out.println(result);

    assertEquals("Wrong result.",
        "test.ant.property|Test Value|Test Value", result);
  }

  @Test
  public void completeTarget ()
  {
    String result = Eclim.execute(new String[]{
      "ant_complete", "-p", Ant.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "234", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results.", 7, results.length);
    assertTrue("Target 'java' not found.", results[0].startsWith("java|"));
    assertTrue("Target 'javac' not found.", results[1].startsWith("javac|"));
    assertTrue("Target 'javacc' not found.", results[2].startsWith("javacc|"));
    assertTrue("Target 'javadoc' not found.", results[3].startsWith("javadoc|"));
    assertTrue("Target 'javadoc2' not found.", results[4].startsWith("javadoc2|"));
    assertTrue("Target 'javah' not found.", results[5].startsWith("javah|"));
    assertTrue("Target 'javaresource' not found.",
        results[6].startsWith("javaresource|"));
  }
}
