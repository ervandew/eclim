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
package org.eclim.plugin.jdt.command.doc;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for DocSearchCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class DocSearchCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/doc/TestDocSearch.java";

  @Test
  public void elementSearch ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_docsearch", "-n", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-o", "68", "-l", "4", "-x", "declarations"
    });

    System.out.println(result);

    assertEquals("Wrong result.",
        "http://java.sun.com/j2se/1.5.0/docs/api/java/awt/List.html", result);
  }

  @Test
  public void patternSearch ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_docsearch", "-n", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-p", "ArrayList"
    });

    System.out.println(result);

    assertEquals("Wrong result.",
        "http://java.sun.com/j2se/1.5.0/docs/api/java/util/ArrayList.html\n" +
        "http://java.sun.com/j2se/1.5.0/docs/api/java/util/Arrays.ArrayList.html",
        result);
  }

  @Test
  public void methodSearch ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_docsearch", "-n", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-p", "currentTime%2A", "-t", "method"
    });

    System.out.println(result);

    assertEquals("Wrong result.",
        "http://java.sun.com/j2se/1.5.0/docs/api/java/lang/" +
        "System.html#currentTimeMillis()",
        result);
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(DocSearchCommandTest.class);
  }
}
