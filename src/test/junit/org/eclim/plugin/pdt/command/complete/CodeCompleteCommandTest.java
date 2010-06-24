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
package org.eclim.plugin.pdt.command.complete;

import java.io.FileWriter;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.pdt.Pdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "php/complete/test.php";
  private static final String TEST_FILE_ERRATIC = "test.php";

  @Test
  public void completeAll()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "213", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 3, results.length);
    assertTrue("Wrong result", results[0].startsWith("methodA1(|"));
    assertTrue("Wrong result", results[1].startsWith("methodA2()|"));
    assertTrue("Wrong result", results[2].startsWith("variable1|"));
  }

  @Test
  public void completePrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "228", "-e", "utf-8"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 2, results.length);
    assertTrue("Wrong result", results[0].startsWith("methodA1(|"));
    assertTrue("Wrong result", results[1].startsWith("methodA2()|"));
  }

  /**
   * Test the case where pdt will complete the first completion attempt, but
   * return no results for the second attempt on a new line (sometimes takes
   * several lines before the problem surfaces).
   */
  @Test
  public void completeErratic()
    throws Exception
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String[] contents = new String[]{
      "<?php",
      "",
      "class Test {",
      "  function getName() {",
      "  }",
      "  function getValue() {",
      "  }",
      "}",
      "",
      "$test = new Test();",
      "$test->;"
    };

    int index = 105;
    for (int ii = 0 ; ii < 10; ii++){
      FileWriter out = new FileWriter(Eclim.resolveFile(Pdt.TEST_PROJECT, TEST_FILE_ERRATIC));
      out.write(StringUtils.join(contents, "\n"));
      out.close();

      String result = Eclim.execute(new String[]{
        "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE_ERRATIC,
        "-o", String.valueOf(index), "-e", "utf-8"
      });

      System.out.println("## iteration " + ii + ":\n" + result);

      String[] results = StringUtils.split(result, '\n');

      assertEquals("Wrong number of results", 2, results.length);
      assertTrue("Wrong result", results[0].startsWith("getName()|"));
      assertTrue("Wrong result", results[1].startsWith("getValue()|"));

      String[] newContents = new String[contents.length + 1];
      System.arraycopy(contents, 0, newContents, 0, contents.length - 1);
      newContents[contents.length - 1] = "$test->getName();";
      newContents[contents.length] = "$test->;";
      contents = newContents;
      index += 18;
      // sleep a little bit since no human can type this fast and the pdt
      // appears to need a little time to update.
      Thread.sleep(500);
    }
  }
}
