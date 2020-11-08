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
package org.eclim.plugin.jdt.command.src;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/src/TestSrc.java";

  @Test
  @SuppressWarnings("unchecked")
  public void update()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "java_src_update", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE, "-v",
      });

    assertEquals("Wrong number of errors.", 3, results.size());

    String file = Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE);

    Map<String, Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertTrue(((String)error.get("message")).indexOf("List is a raw type") != -1);
    assertEquals(error.get("line"), 10);
    assertEquals(error.get("column"), 5);
    assertEquals(error.get("warning"), true);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertTrue(
        ((String)error.get("message")).indexOf("ArrayList is a raw type") != -1);
    assertEquals(error.get("line"), 10);
    assertEquals(error.get("column"), 21);
    assertEquals(error.get("warning"), true);

    error = results.get(2);
    assertEquals(error.get("filename"), file);
    assertTrue(
        ((String)error.get("message")).indexOf("The method a() is undefined") != -1);
    assertEquals(error.get("line"), 11);
    assertEquals(error.get("column"), 10);
    assertEquals(error.get("warning"), false);
  }
}
