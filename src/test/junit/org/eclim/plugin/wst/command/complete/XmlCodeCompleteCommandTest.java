/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
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
