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
package org.eclim.command.xml.format;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test FormatCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class FormatCommandTest
{
  private static final String TEST_FILE = "xml/format.xml";

  @Test
  public void execute ()
  {
    String result = Eclim.execute(new String[]{
      "xml_format",
      "-f", Eclim.resolveFile(TEST_FILE), "-w", "80", "-i", "2"
    });
    System.out.println(result);

    String[] lines = StringUtils.split(result, '\n');

    assertEquals("Wrong number of lines.", 5, lines.length);

    assertEquals("<blah attr1=\"one\" attr2=\"two\" attr3=\"three\" attr4=\"four\" attr5=\"five\" attr6=\"six\" attr7=\"seven\">", lines[1]);
    assertEquals("  <one>one</one>", lines[2]);
    assertEquals("  <two/>", lines[3]);
    assertEquals("</blah>", lines[4]);
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(FormatCommandTest.class);
  }
}
