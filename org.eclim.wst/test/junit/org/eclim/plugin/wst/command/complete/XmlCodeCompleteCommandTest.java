/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.wst.Wst;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for XmlCodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class XmlCodeCompleteCommandTest
{
  private static final String TEST_FILE_XSD = "xsd/test.xsd";

  @Test
  @SuppressWarnings("unchecked")
  public void completeXsd()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "xml_complete", "-p", Wst.TEST_PROJECT,
        "-f", TEST_FILE_XSD, "-o", "584", "-e", "utf-8"
      });

    assertEquals("Wrong number of results.", 1, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "xs:unique");
    assertEquals(result.get("menu"), "");
    assertEquals(result.get("info"),
        "<p><b>Element : </b>unique</p><dl><p><b>Content Model : </b>" +
        "((annotation?), (selector, field+))</p>");
  }
}
