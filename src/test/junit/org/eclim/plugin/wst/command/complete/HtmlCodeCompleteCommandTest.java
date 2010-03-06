/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

import org.apache.tools.ant.taskdefs.condition.Os;

import org.eclim.Eclim;

import org.eclim.plugin.wst.Wst;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for HtmlCodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class HtmlCodeCompleteCommandTest
{
  private static final String TEST_FILE = "html/test.html";

  @Test
  public void completeAttribute()
  {
    // html code completion disabled on windows
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      return;
    }

    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "html_complete", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "152", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 2, results.length);
    assertTrue("Wrong result.", results[0].startsWith("href"));
  }

  @Test
  public void completeElement()
  {
    // html code completion disabled on windows
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      return;
    }

    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "html_complete", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "141", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 7, results.length);
    assertTrue("Wrong result.", results[0].startsWith("h1"));
    assertTrue("Wrong result.", results[1].startsWith("h2"));
  }

  @Test
  public void completeCss()
  {
    // html code completion disabled on windows
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      return;
    }

    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "html_complete", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "131", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 8, results.length);
    assertTrue("Wrong result.", results[0].startsWith("font"));
    assertTrue("Wrong result.", results[1].startsWith("font-family"));
  }
}
