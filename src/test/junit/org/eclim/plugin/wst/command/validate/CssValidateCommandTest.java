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
package org.eclim.plugin.wst.command.validate;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.wst.Wst;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CssValidateCommand.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class CssValidateCommandTest
{
  private static final String TEST_FILE = "css/validate.css";
  private static final String TEST_LEXICAL_FILE = "css/lexical.css";

  @Test
  public void validate ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "css_validate", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 2, results.length);

    String file = Eclim.resolveFile(Wst.TEST_PROJECT, TEST_FILE);
    for(int ii = 0; ii < results.length; ii++){
      assertTrue("Wrong filename [" + ii + "].", results[ii].startsWith(file));
      assertTrue("Wrong level [" + ii + "].", results[ii].endsWith("|e"));
    }

    assertTrue("Wrong error.",
        results[0].indexOf("bald is not a font-weight value") != -1);
    assertTrue("Wrong error.",
        results[1].indexOf("Property fnt-size doesn't exist") != -1);
  }

  @Test
  public void lexical ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "css_validate", "-p", Wst.TEST_PROJECT, "-f", TEST_LEXICAL_FILE
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 1, results.length);

    String file = Eclim.resolveFile(Wst.TEST_PROJECT, TEST_LEXICAL_FILE);
    for(int ii = 0; ii < results.length; ii++){
      assertTrue("Wrong filename [" + ii + "].", results[ii].startsWith(file));
      assertTrue("Wrong level [" + ii + "].", results[ii].endsWith("|e"));
    }

    assertTrue("Wrong error.", results[0].indexOf("Lexical error") != -1);
  }
}
