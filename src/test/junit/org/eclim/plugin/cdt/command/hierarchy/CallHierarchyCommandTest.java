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
package org.eclim.plugin.cdt.command.hierarchy;

import org.eclim.Eclim;

import org.eclim.plugin.cdt.Cdt;

import org.eclim.util.StringUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CallHierarchyCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CallHierarchyCommandTest
{
  private static final String TEST_FILE = "src/callhierarchy/mod2.c";

  @Test
  public void execute()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    // reference to fun2
    String result = Eclim.execute(new String[]{
      "c_callhierarchy", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "57", "-l", "4", "-e", "utf-8"
    });

    String path = Eclim.getProjectPath(Cdt.TEST_PROJECT) + "/src/callhierarchy/";
    String[] lines = StringUtils.split(result, '\n');
    assertEquals(lines[0], path + "mod2.c|1 col 5|fun2(int)");
    assertEquals(lines[1], "\t " + path + "mod1.c|5 col 10|fun1(int)");
    assertEquals(lines[2], "\t\t " + path + "main.c|6 col 28|main()");
    assertEquals(lines[3], "\t\t " + path + "mod2.c|7 col 10|fun3(int)");
    assertEquals(lines[4], "\t " + path + "mod2.c|6 col 3|fun3(int)");
    assertEquals(lines[5], "\t " + path + "mod2.c|7 col 20|fun3(int)");
  }
}
