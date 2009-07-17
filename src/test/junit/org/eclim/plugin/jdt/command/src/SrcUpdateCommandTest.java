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
package org.eclim.plugin.jdt.command.src;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_FILE=
    "src/org/eclim/test/src/TestSrc.java";

  @Test
  public void update()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_src_update", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE, "-v"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of errors.", 3, results.length);

    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Wrong filename [0].", results[0].startsWith(file));
    assertTrue("Wrong level [0].", results[0].endsWith("|w"));
    assertTrue("Wrong message [0].",
        results[0].indexOf("List is a raw type") != -1);
    assertTrue("Wrong filename [1].", results[1].startsWith(file));
    assertTrue("Wrong level [1].", results[1].endsWith("|w"));
    assertTrue("Wrong message [1].",
        results[1].indexOf("ArrayList is a raw type") != -1);
    assertTrue("Wrong filename [2].", results[2].startsWith(file));
    assertTrue("Wrong level [2].", results[2].endsWith("|e"));
    assertTrue("Wrong message [2].",
        results[2].indexOf("The method a() is undefined") != -1);
  }
}
