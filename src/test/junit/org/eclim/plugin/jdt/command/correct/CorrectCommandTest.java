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
package org.eclim.plugin.jdt.command.correct;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CorrectCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CorrectCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/correct/TestCorrect.java";

  @Test
  public void suggest ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_correct", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-l", "5", "-o", "74"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong error.",
        "ArrayList cannot be resolved to a type", results[0]);
    assertTrue("Wrong suggestion.",
        results[1].indexOf(".70:  Change to") != -1);
  }

  @Test
  public void apply ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_correct", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-l", "5", "-o", "74", "-a", "3"
    });

    System.out.println(result);

    assertTrue("Import not found.",
        Pattern.compile("import java\\.").matcher(result).find());
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(CorrectCommandTest.class);
  }
}
