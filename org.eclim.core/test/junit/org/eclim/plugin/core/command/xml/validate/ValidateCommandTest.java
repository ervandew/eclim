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
package org.eclim.plugin.core.command.xml.validate;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test ValidateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ValidateCommandTest
{
  private static final String TEST_FILE = "xml/test_dtd.xml";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "xml_validate", "-p", Eclim.TEST_PROJECT, "-f", TEST_FILE,
      });

    assertEquals("Wrong number of errors.", 2, results.size());

    String file = Eclim.resolveFile(Eclim.TEST_PROJECT, TEST_FILE);

    Map<String, Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertTrue(((String)error.get("message"))
        .indexOf("The content of element type \"bar\" is incomplete") != -1);
    assertEquals(error.get("line"), 12);
    assertEquals(error.get("column"), 11);
    assertEquals(error.get("warning"), false);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertTrue(((String)error.get("message"))
        .indexOf("The content of element type \"foo\" must match") != -1);
    assertEquals(error.get("line"), 13);
    assertEquals(error.get("column"), 9);
    assertEquals(error.get("warning"), false);
  }
}
