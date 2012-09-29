/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
package org.eclim.plugin.cdt.command.src;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.cdt.Cdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_C_FILE = "src/test_src.c";
  private static final String TEST_CPP_FILE = "src/test_src.cpp";

  @Test
  @SuppressWarnings("unchecked")
  public void validateC()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_src_update", "-p", Cdt.TEST_PROJECT, "-f", TEST_C_FILE, "-v"
      });

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_C_FILE);

    Map<String,Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"), "Unresolved inclusion: <stdi.h>");
    assertEquals(error.get("line"), 1);
    assertEquals(error.get("column"), 1);
    assertEquals(error.get("warning"), true);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"), "Syntax error");
    assertEquals(error.get("line"), 5);
    assertEquals(error.get("column"), 3);
    assertEquals(error.get("warning"), false);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validateCPP()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "c_src_update", "-p", Cdt.TEST_PROJECT, "-f", TEST_CPP_FILE, "-v"
      });

    String file = Eclim.resolveFile(Cdt.TEST_PROJECT, TEST_CPP_FILE);

    Map<String,Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"), "Invalid redeclaration of 'arg'");
    assertEquals(error.get("line"), 3);
    assertEquals(error.get("column"), 7);
    assertEquals(error.get("warning"), false);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"), "Function 'foo' could not be resolved");
    assertEquals(error.get("line"), 4);
    assertEquals(error.get("column"), 3);
    assertEquals(error.get("warning"), false);
  }
}
