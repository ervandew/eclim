/**
 * Copyright (c) 2005 - 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  public void method1 ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "javadoc_comment", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "158"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Incorrect comment generated.", contents.indexOf(COMMENT_1) != -1);
  }

  @Test
  public void method2 ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "javadoc_comment", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "58"
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
