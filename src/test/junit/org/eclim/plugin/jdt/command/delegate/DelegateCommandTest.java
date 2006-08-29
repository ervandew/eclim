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
package org.eclim.plugin.jdt.command.delegate;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for DelegateCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class DelegateCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/delegate/TestDelegate.java";

  @Test
  public void execute ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_delegate", "-p", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-o", "124"
    });

    System.out.println(result);

    assertTrue("Wrong first line.",
        result.startsWith("org.eclim.test.delegate.TestDelegate"));
    assertTrue("Interface not in results.",
        result.indexOf("package java.util;\npublic interface List {") != -1);
    assertTrue("Method not in results.",
        result.indexOf("\tpublic abstract boolean remove (Object o)") != -1);

    result = Eclim.execute(new String[]{
      "java_delegate", "-p", Jdt.TEST_PROJECT,
      "-f", Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
      "-t", "org.eclim.test.delegate.TestDelegate",
      "-s", "java.util.List", "-m", "remove(Object)"
    });

    System.out.println(result);

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public boolean remove \\(Object o\\)\n  \\{\n  " +
          "\treturn list.remove\\(o\\);")
        .matcher(contents).find());

    assertTrue("Method not commented out in results.",
        result.indexOf("//public abstract boolean remove (Object o)") != -1);
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(DelegateCommandTest.class);
  }
}
