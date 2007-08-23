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
package org.eclim.plugin.jdt.command.constructor;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ConstructorCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ConstructorCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/constructor/TestConstructor.java";

  @Test
  public void emptyConstructor ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "1"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("public TestConstructor \\(\\)")
        .matcher(contents).find());
  }

  @Test
  public void argConstructor ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-r", "id,name"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("public TestConstructor \\(int id, String name\\)")
        .matcher(contents).find());
  }
}
