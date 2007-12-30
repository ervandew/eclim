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
package org.eclim.plugin.jdt.command.webxml;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ValidateCommand.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ValidateCommandTest
{
  private static final String TEST_FILE = "webxml/web.xml";

  @Test
  public void validateXmlErrors ()
  {
    String result = Eclim.execute(new String[]{
      "webxml_validate", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of results", 3, results.length);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|12 col 1|Class 'foo.bar.Listener' not found in project 'eclim_unit_test_java'.|e",
        results[0]);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|22 col 1|Class 'org.apache.solr.servlet.SolrServlet' not found in project 'eclim_unit_test_java'.|e",
        results[1]);

    assertEquals("Wrong result.",
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE) +
        "|32 col 1|No servlet definition with name 'pong' defined.|e",
        results[2]);
  }
}
