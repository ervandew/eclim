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
  private static final String TEST_FILE_ERRORS =
    "src/org/eclim/test/src/TestSrcError.java";
  private static final String TEST_FILE_WARNINGS =
    "src/org/eclim/test/src/TestSrcWarning.java";

  @Test
  public void errors()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_src_update", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE_ERRORS, "-v"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of errors.", 1, results.length);

    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE_ERRORS);
    for(int ii = 0; ii < results.length; ii++){
      assertTrue("Wrong filename [" + ii + "].", results[ii].startsWith(file));
      assertTrue("Wrong level [" + ii + "].", results[ii].endsWith("|e"));
    }

    assertTrue("Wrong error.", results[0].indexOf("Syntax error,") != -1);
  }

  @Test
  public void warnings()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_src_update", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE_WARNINGS, "-v"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of warnings.", 2, results.length);

    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE_WARNINGS);
    for(int ii = 0; ii < results.length; ii++){
      assertTrue("Wrong filename [" + ii + "].", results[ii].startsWith(file));
      assertTrue("Wrong level [" + ii + "].", results[ii].endsWith("|w"));
    }

    assertTrue("Wrong first warning.",
        results[0].indexOf("The import java.util.ArrayList is never used") != -1);
    assertTrue("Wrong second warning.",
        results[1].indexOf("The import java.util.List is never used") != -1);
  }
}
