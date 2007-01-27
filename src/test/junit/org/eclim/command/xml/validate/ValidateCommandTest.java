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
package org.eclim.command.xml.validate;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test ValidateCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ValidateCommandTest
{
  private static final String TEST_FILE = "xml/test_dtd.xml";

  @Test
  public void execute ()
  {
    String file = TEST_FILE;
    String project = Eclim.TEST_PROJECT;
    String result = Eclim.execute(
        new String[]{"xml_validate", "-p", project, "-f", file });
    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of errors.", 2, results.length);

    assertTrue("Wrong filename.", results[0].indexOf(file) != -1);
    assertTrue("Wrong filename.", results[1].indexOf(file) != -1);

    assertTrue("Wrong error.",
        results[0].indexOf("The content of element type \"bar\" is incomplete") != -1);
    assertTrue("Wrong error.",
        results[1].indexOf("The content of element type \"foo\" must match") != -1);
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(ValidateCommandTest.class);
  }
}
