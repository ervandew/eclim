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
package org.eclim.plugin.wst.command.complete;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.wst.Wst;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CssCodeCompleteCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CssCodeCompleteCommandTest
{
  private static final String TEST_FILE = "css/complete.css";

  @Test
  public void complete ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "css_complete", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "52", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 8, results.length);
    assertTrue("Wrong result.", results[0].startsWith("font"));
    assertTrue("Wrong result.", results[1].startsWith("font-family"));
    assertTrue("Wrong result.", results[2].startsWith("font-size"));
    assertTrue("Wrong result.", results[3].startsWith("font-size-adjust"));
    assertTrue("Wrong result.", results[4].startsWith("font-stretch"));
    assertTrue("Wrong result.", results[5].startsWith("font-style"));
    assertTrue("Wrong result.", results[6].startsWith("font-variant"));
    assertTrue("Wrong result.", results[7].startsWith("font-weight"));
  }
}
