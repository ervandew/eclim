/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.doc;

import java.util.regex.Pattern;

import org.eclim.Eclim;
import org.eclim.EclimTestCase;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CommentCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CommentCommandTest
  extends EclimTestCase
{
  private static final String TEST_FILE =
    "src/org/eclim/test/doc/TestComment.java";

  private static final Pattern COMMENT_1 = Pattern.compile(
    "\\s+/\\*\\*\n" +
    "\\s+ \\* \\{@inheritDoc\\}\n" +
    "\\s+ \\*\n" +
    "\\s+ \\* @see Object#equals\\(Object\\)\n" +
    "\\s+ \\*/");

  private static final Pattern COMMENT_2 = Pattern.compile(
    "\\s+/\\*\\*\n" +
    "\\s+ \\*\n" +
    "\\s+ \\*\n" +
    "\\s+ \\* @param _id\n" +
    "\\s+ \\* @param _name\n" +
    "\\s+ \\* @return\n" +
    "\\s+ \\*\n" +
    "\\s+ \\* @throws IOException\n" +
    "\\s+ \\*/");

  @Test
  public void method1()
  {
    modifies(Jdt.TEST_PROJECT, TEST_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "javadoc_comment", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "58", "-e", "utf-8",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    System.out.println("'" + COMMENT_2 + "'");
    System.out.println("'" + contents.substring(56, 158) + "'");
    assertTrue("Incorrect comment generated.", COMMENT_2.matcher(contents).find());
  }

  @Test
  public void method2()
  {
    modifies(Jdt.TEST_PROJECT, TEST_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "javadoc_comment", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "158", "-e", "utf-8",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Incorrect comment generated.", COMMENT_1.matcher(contents).find());
  }
}
