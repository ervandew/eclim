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
package org.eclim.plugin.pdt.command.src;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.pdt.Pdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_FILE = "php/src/test.php";

  @Test
  public void validate ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_src_update", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE, "-v"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/src/test.php");
    assertEquals("Wrong result.",
        file + "|5 col 5|Syntax Error: expecting: ',' or ';'|e", results[0]);
  }
}
