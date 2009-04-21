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
package org.eclim.plugin.cdt.command.src;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.cdt.Cdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_FILE = "src/test_src.c";

  @Test
  public void validate()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "c_src_update", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE, "-v"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_FILE);
    assertEquals("Wrong result.",
        file + "|1 col 1|Unresolved inclusion: <stdi.h>|w", results[0]);
    assertEquals("Wrong result.", file + "|5 col 3|Syntax error|e", results[1]);
  }
}
