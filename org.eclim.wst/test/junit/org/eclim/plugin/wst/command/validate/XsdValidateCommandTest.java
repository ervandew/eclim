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
package org.eclim.plugin.wst.command.validate;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.wst.Wst;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for XsdValidateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class XsdValidateCommandTest
{
  private static final String TEST_FILE = "xsd/test.xsd";

  @Test
  @SuppressWarnings("unchecked")
  public void validate()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "xsd_validate", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE,
      });

    assertEquals("Wrong number of errors.", 2, results.size());

    String file = Eclim.resolveFile(Wst.TEST_PROJECT, TEST_FILE);
    Map<String, Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertTrue(((String)error.get("message"))
        .indexOf("Cannot resolve the name 'Model'") != -1);
    assertEquals(error.get("line"), 3);
    assertEquals(error.get("column"), 43);
    assertEquals(error.get("warning"), false);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertTrue(((String)error.get("message"))
        .indexOf("The content of 'project' must match") != -1);
    assertEquals(error.get("line"), 11);
    assertEquals(error.get("column"), 12);
    assertEquals(error.get("warning"), false);
  }
}
