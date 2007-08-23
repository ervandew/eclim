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
package org.eclim.plugin.wst.command.complete;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.wst.Wst;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for HtmlCodeCompleteCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class HtmlCodeCompleteCommandTest
{
  private static final String TEST_FILE = "html/test.html";

  @Test
  public void completeAttribute ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "html_complete", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE, "-o", "152"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 2, results.length);
    assertTrue("Wrong result.", results[0].startsWith("href"));
  }

  @Test
  public void completeElement ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "html_complete", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE, "-o", "141"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 7, results.length);
    assertTrue("Wrong result.", results[0].startsWith("h1"));
    assertTrue("Wrong result.", results[1].startsWith("h2"));
  }

  @Test
  public void completeCss ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "html_complete", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE, "-o", "131"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 8, results.length);
    assertTrue("Wrong result.", results[0].startsWith("font"));
    assertTrue("Wrong result.", results[1].startsWith("font-family"));
  }
}
