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
package org.eclim.plugin.jdt.command.include;

import java.util.List;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for UnusedImportsCommand.
 *
 * @author Eric Van Dewoestine
 */
public class UnusedImportsCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/include/TestUnusedImport.java";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<String> results = (List<String>)Eclim.execute(new String[]{
      "java_imports_unused", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE
    });

    assertEquals(3, results.size());
    assertEquals("java.lang.Math.PI", results.get(0));
    assertEquals("java.util.ArrayList", results.get(1));
    assertEquals("java.util.List", results.get(2));
  }
}
