/**
 * Copyright (c) 2005 - 2007
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
package org.eclim.plugin.pdt.command.complete;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.pdt.Pdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "php/complete/test.php";

  @Test
  public void completeAll ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE, "-o", "213"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 3, results.length);
    assertTrue("Wrong result", results[0].startsWith("methodA1(|"));
    assertTrue("Wrong result", results[1].startsWith("methodA2()|"));
    assertTrue("Wrong result", results[2].startsWith("variable1|"));
  }

  @Test
  public void completePrefix ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE, "-o", "228"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of results", 2, results.length);
    assertTrue("Wrong result", results[0].startsWith("methodA1(|"));
    assertTrue("Wrong result", results[1].startsWith("methodA2()|"));
  }
}
