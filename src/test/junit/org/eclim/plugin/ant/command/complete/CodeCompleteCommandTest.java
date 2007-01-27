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
package org.eclim.plugin.ant.command.complete;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.ant.Ant;

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
  private static final String TEST_FILE = "build.xml";

  @Test
  public void completeProperty ()
  {
    String result = Eclim.execute(new String[]{
      "ant_complete", "-p", Ant.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "220"
    });

    System.out.println(result);

    assertEquals("Wrong result.",
        "test.ant.property|Test Value|Test Value", result);
  }

  @Test
  public void completeTarget ()
  {
    String result = Eclim.execute(new String[]{
      "ant_complete", "-p", Ant.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "234"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results.", 6, results.length);
    assertTrue("Target 'java' not found.", results[0].startsWith("java|"));
    assertTrue("Target 'javac' not found.", results[1].startsWith("javac|"));
    assertTrue("Target 'javacc' not found.", results[2].startsWith("javacc|"));
    assertTrue("Target 'javadoc' not found.", results[3].startsWith("javadoc|"));
    assertTrue("Target 'javadoc2' not found.", results[4].startsWith("javadoc2|"));
    assertTrue("Target 'javah' not found.", results[5].startsWith("javah|"));
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(CodeCompleteCommandTest.class);
  }
}
