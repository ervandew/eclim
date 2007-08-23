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
package org.eclim.plugin.wst.command.validate;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.wst.Wst;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for HtmlValidateCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class HtmlValidateCommandTest
{
  private static final String TEST_FILE = "html/test.html";

  @Test
  public void validate ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "html_validate", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 3, results.length);

    String file = Eclim.resolveFile(Wst.TEST_PROJECT, TEST_FILE);

    assertTrue("Wrong filename [0].", results[0].startsWith(file));
    assertTrue("Wrong level [0].", results[0].endsWith("|e"));
    assertTrue("Wrong error [0].",
        results[0].indexOf("<h> is not recognized!") != -1);

    assertTrue("Wrong filename [1].", results[1].startsWith(file));
    assertTrue("Wrong level [1].", results[1].endsWith("|w"));
    assertTrue("Wrong error [1].",
        results[1].indexOf("discarding unexpected <h>") != -1);

    assertTrue("Wrong filename [2].", results[2].startsWith(file));
    assertTrue("Wrong level [2].", results[2].endsWith("|w"));
    assertTrue("Wrong error [2].",
        results[2].indexOf("discarding unexpected </div>") != -1);
  }
}
