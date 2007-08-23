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
package org.eclim.plugin.ant.command.validate;

import org.eclim.Eclim;

import org.eclim.plugin.ant.Ant;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ValidateCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ValidateCommandTest
{
  private static final String TEST_FILE = "build.xml";

  @Test
  public void execute ()
  {
    String result = Eclim.execute(new String[]{
      "ant_validate", "-p", Ant.TEST_PROJECT,
      "-f", TEST_FILE
    });

    System.out.println(result);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Ant.TEST_PROJECT, TEST_FILE) +
        "|5 col 2|Default target none does not exist in this project|e",
        result);
  }
}
