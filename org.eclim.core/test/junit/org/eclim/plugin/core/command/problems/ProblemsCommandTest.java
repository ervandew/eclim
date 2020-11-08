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
package org.eclim.plugin.core.command.problems;

import java.util.HashMap;
import java.util.List;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the problems command.
 *
 * @author Eric Van Dewoestine
 */
public class ProblemsCommandTest
{
  private static final String TEST_PROJECT = "eclim_unit_test";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    List<Object> results = (List<Object>)Eclim.execute(new String[]{
      "problems", "-p", TEST_PROJECT,
    });

    HashMap<String, Object> error = new HashMap<String, Object>();
    error.put("message", "ArrayList cannot be resolved to a type");
    error.put("filename",
        Eclim.getWorkspace() + "/" + TEST_PROJECT + "/src/org/eclim/test/Test.java");
    error.put("line", 5);
    error.put("column", 11);
    error.put("endLine", -1);
    error.put("endColumn", -1);
    error.put("warning", false);

    assertTrue(results.contains(error));
  }
}
