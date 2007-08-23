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
package org.eclim.plugin.jdt.command.src;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_FILE_ERRORS =
    "src/org/eclim/test/src/TestSrcError.java";
  private static final String TEST_FILE_WARNINGS =
    "src/org/eclim/test/src/TestSrcWarning.java";

  @Test
  public void errors ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_src_update", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE_ERRORS, "-v"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of errors.", 1, results.length);

    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE_ERRORS);
    for(int ii = 0; ii < results.length; ii++){
      assertTrue("Wrong filename [" + ii + "].", results[ii].startsWith(file));
      assertTrue("Wrong level [" + ii + "].", results[ii].endsWith("|e"));
    }

    assertTrue("Wrong error.", results[0].indexOf("Syntax error,") != -1);
  }

  @Test
  public void warnings ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_src_update", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE_WARNINGS, "-v"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of warnings.", 2, results.length);

    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE_WARNINGS);
    for(int ii = 0; ii < results.length; ii++){
      assertTrue("Wrong filename [" + ii + "].", results[ii].startsWith(file));
      assertTrue("Wrong level [" + ii + "].", results[ii].endsWith("|w"));
    }

    assertTrue("Wrong first warning.",
        results[0].indexOf("The import java.util.ArrayList is never used") != -1);
    assertTrue("Wrong second warning.",
        results[1].indexOf("The import java.util.List is never used") != -1);
  }
}
