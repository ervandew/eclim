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
package org.eclim.plugin.jdt.command.src;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcFileExistsCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SrcFileExistsCommandTest
{
  private static final String TEST_FILE =
    "org/eclim/test/src/TestPrototype.java";

  @Test
  public void execute ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_src_exists", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE
    });

    assertEquals("Wrong result.", "true", result);

    result = Eclim.execute(new String[]{
      "java_src_exists", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE.replace('t', 'o')
    });

    assertEquals("Wrong result.", "false", result);
  }
}
