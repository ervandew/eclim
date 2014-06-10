/**
 * Copyright (C) 2014  Eric Van Dewoestine
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
package org.eclim.plugin.groovy.command.src;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.groovy.Groovy;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_FILE=
    "src/org/eclim/test/src/TestSrc.groovy";

  @Test
  @SuppressWarnings("unchecked")
  public void update()
  {
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "groovy_src_update", "-p", Groovy.TEST_PROJECT, "-f", TEST_FILE, "-v"
      });

    assertEquals("Wrong number of errors.", 2, results.size());

    String file = Eclim.resolveFile(Groovy.TEST_PROJECT, TEST_FILE);

    Map<String,Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "Groovy:unexpected token: } @ line 8, column 3.");
    assertEquals(error.get("line"), 8);
    assertEquals(error.get("column"), 3);
    assertEquals(error.get("warning"), false);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"),
        "Groovy:unexpected token: err @ line 10, column 3.");
    assertEquals(error.get("line"), 10);
    assertEquals(error.get("column"), 3);
    assertEquals(error.get("warning"), false);
  }
}
