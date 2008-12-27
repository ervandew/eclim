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
package org.eclim.plugin.jdt.command.doc;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CommentCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CommentCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/doc/TestComment.java";

  private static final String COMMENT_1 =
    "  /**\n" +
    "   * {@inheritDoc}\n" +
    "   * @see Object#equals(Object)\n" +
    "   */";

  private static final String COMMENT_2 =
    "  /**\n" +
    "   * \n" +
    "   * \n" +
    "   * @param _id\n" +
    "   * @param _name\n" +
    "   * @return\n" +
    "   * \n" +
    "   * @throws IOException\n" +
    "   */";

  @Test
  public void method1()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "javadoc_comment", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "158", "-e", "utf-8"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Incorrect comment generated.", contents.indexOf(COMMENT_1) != -1);
  }

  @Test
  public void method2()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "javadoc_comment", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "58", "-e", "utf-8"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    System.out.println("'" + COMMENT_2 + "'");
    System.out.println("'" + contents.substring(56, 158) + "'");
/*char[] c1 = COMMENT_2.toCharArray();
char[] c2 = contents.substring(56, 158).toCharArray();
for(int ii = 0; ii < c1.length; ii++){
  System.out.println("" + ii + " " +
      getChar(c1[ii]) + "=" + getChar(c2[ii]) +
      " : " + (c1[ii] == c2[ii]));
}*/
    assertTrue("Incorrect comment generated.", contents.indexOf(COMMENT_2) != -1);
  }

  /*private String getChar (char _char)
  {
    if(_char == '\n')
      return "\\n";

    if(_char == '\t')
      return "\\t";

    return "" + _char;
  }*/
}
