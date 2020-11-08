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
 * Test case for CssCodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CssCodeCompleteCommandTest
{
  private static final String TEST_FILE = "css/complete.css";

  @Test
  @SuppressWarnings("unchecked")
  public void complete()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "css_complete", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "52", "-e", "utf-8",
      });

    assertEquals("Wrong number of errors.", 8, results.size());

    assertEquals(results.get(0).get("completion"), "font");
    assertEquals(results.get(1).get("completion"), "font-family");
    assertEquals(results.get(2).get("completion"), "font-size");
    assertEquals(results.get(3).get("completion"), "font-size-adjust");
    assertEquals(results.get(4).get("completion"), "font-stretch");
    assertEquals(results.get(5).get("completion"), "font-style");
    assertEquals(results.get(6).get("completion"), "font-variant");
    assertEquals(results.get(7).get("completion"), "font-weight");
  }
}
