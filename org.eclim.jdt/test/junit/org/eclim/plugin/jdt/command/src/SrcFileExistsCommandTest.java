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
package org.eclim.plugin.jdt.command.src;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcFileExistsCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SrcFileExistsCommandTest
{
  private static final String TEST_FILE =
    "org/eclim/test/src/TestPrototype.java";

  @Test
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = (String)Eclim.execute(new String[]{
      "java_src_exists", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
    });

    assertEquals("Wrong result.", "true", result);

    result = (String)Eclim.execute(new String[]{
      "java_src_exists", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE.replace('t', 'o'),
    });

    assertEquals("Wrong result.", "false", result);
  }
}
