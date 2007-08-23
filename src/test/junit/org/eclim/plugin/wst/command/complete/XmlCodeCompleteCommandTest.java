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
 * Test case for XmlCodeCompleteCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class XmlCodeCompleteCommandTest
{
  private static final String TEST_FILE_XSD = "xsd/test.xsd";
  private static final String TEST_FILE_WSDL = "wsdl/GoogleSearch.wsdl";

  @Test
  public void completeXsd ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "xml_complete", "-p", Wst.TEST_PROJECT,
      "-f", TEST_FILE_XSD, "-o", "584", "-d", ",,"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 1, results.length);
    assertTrue("Wrong result.", results[0].indexOf("xs:unique,,Content Model") != -1);
  }

  @Test
  public void completeWsdl ()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "xml_complete", "-p", Wst.TEST_PROJECT,
      "-f", TEST_FILE_WSDL, "-o", "516"
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 3, results.length);
    assertTrue("Wrong result.", results[0].indexOf("annotation") != -1);
    assertTrue("Wrong result.", results[1].indexOf("attribute") != -1);
    assertTrue("Wrong result.", results[2].indexOf("attributeGroup") != -1);
  }
}
