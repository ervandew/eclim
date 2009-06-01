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
package org.eclim.plugin.core.command.history;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for history commands
 *
 * @author Eric Van Dewoestine
 */
public class HistoryCommandTest
{
  private static final String TEST_FILE = "history/sample.txt";
  private static final String ENTRY =
    "\\{'timestamp': '(\\d+)'," +
    "'datetime': '\\d\\d:\\d\\d \\w{3} \\w{3} \\d{2} \\d{4}'," +
    "'delta': '\\d+ (millis|seconds?) ago'\\}";

  /**
   * Test the command.
   */
  @Test
  public void execute()
    throws Exception
  {
    String result = Eclim.execute(new String[]{
      "history_clear", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE
    });
    System.out.println(result);
    assertEquals("Wrong result.", result, "History Cleared.");

    result = Eclim.execute(new String[]{
      "history_list", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE
    });
    System.out.println(result);
    assertEquals("Wrong result.", result, "[]");

    assertEquals("Wrong file contents.",
        Eclim.fileToString(Eclim.TEST_PROJECT, TEST_FILE), "line 1\n");

    Eclim.execute(new String[]{
      "history_add", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE
    });
    Eclim.execute(new String[]{
      "project_refresh_file", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE
    });

    BufferedWriter out = null;
    try{
      out = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(Eclim.resolveFile(TEST_FILE), true)));
      out.write("line 2");
    }finally{
      try{
        out.close();
      }catch(Exception ignore){
      }
    }

    Eclim.execute(new String[]{
      "history_add", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE
    });
    Eclim.execute(new String[]{
      "project_refresh_file", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE
    });

    result = Eclim.execute(new String[]{
      "history_list", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE
    });
    System.out.println(result);

    Pattern pattern = Pattern.compile("^\\[" + ENTRY + "," + ENTRY + "\\]$");
    Matcher matcher = pattern.matcher(result);
    assertTrue("Wrong result.", matcher.matches());

    pattern = Pattern.compile(ENTRY);
    matcher = pattern.matcher(result);
    matcher.find();
    matcher.find();
    String ts = matcher.group(1);
    System.out.println(ts);

    result = Eclim.execute(new String[]{
      "history_revision", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE, "-r", ts
    });
    System.out.println(result);
    assertEquals("Wrong result.", result, "line 1\n");
  }
}
