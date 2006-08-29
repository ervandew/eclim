/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.plugin.jdt.command.complete;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;
import org.eclim.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/complete/TestCompletion.java";

  @Test
  public void completion1 ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_complete", "-p", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-o", "184", "-l", "standard"
    });

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results.", 32, results.length);
    assertEquals("Invalid First Result",
        "f|add(|add(int index, Object element)  void - List|", results[0]);
    assertEquals("Invalid Last Result",
        "f|wait()|wait()  void - Object|", results[results.length - 1]);
  }

  @Test
  public void completion2 ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_complete", "-p", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-o", "266", "-l", "standard"
    });

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results.", 4, results.length);
    assertEquals("Invalid First Result",
        "f|add(|add(int index, Object element)  void - List|", results[0]);
    assertEquals("Invalid Last Result",
        "f|addAll(|addAll(Collection c)  boolean - List|",
        results[results.length - 1]);
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(CodeCompleteCommandTest.class);
  }
}
