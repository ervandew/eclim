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
package org.eclim.plugin.dltkruby.command.complete;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.dltkruby.DltkRuby;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "src/complete/testComplete.rb";

  @Test
  public void completeStatic()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "107", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 2, results.length);
    assertTrue("Wrong result", results[0].startsWith("ID|"));
    assertTrue("Wrong result", results[1].startsWith("moduleMethodA|"));
  }

  @Test
  public void completeStaticPrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "92", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 1, results.length);
    assertTrue("Wrong result", results[0].startsWith("moduleMethodA|"));
  }

  @Test
  public void completeInstance()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "42", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertTrue("Wrong number of results", results.length > 50);
    assertTrue("Wrong result", results[0].startsWith("testA|"));
    assertTrue("Wrong result", results[1].startsWith("testB|"));
    //assertTrue("Wrong result", results[2].startsWith("==(|"));
    //assertTrue("Wrong result", results[3].startsWith("===(|"));
    //assertTrue("Wrong result", results[4].startsWith("=~(|"));
    //assertTrue("Wrong result", results[5].startsWith("__id__|"));
  }

  @Test
  public void completeInstancePrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "54", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 2, results.length);
    assertTrue("Wrong result", results[0].startsWith("testA|"));
    assertTrue("Wrong result", results[1].startsWith("testB|"));
  }

  @Test
  public void completeBuiltins()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "63", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertTrue("Wrong number of results", results.length >= 2);
    assertTrue("Wrong result", results[0].startsWith("each|"));
    assertTrue("Wrong result", results[1].startsWith("each_index|"));
    //assertTrue("Wrong result", results[2].startsWith("each_with_index|"));

    /*result = Eclim.execute(new String[]{
      "ruby_complete", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "70", "-e", "utf-8"
    });

    System.out.println(result);

    results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 1, results.length);
    assertTrue("Wrong result", results[0].startsWith("times|"));*/
  }
}
