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
package org.eclim.plugin.ant.command.validate;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.ant.Ant;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ValidateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ValidateCommandTest
{
  private static final String TEST_FILE = "build.xml";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "ant_validate", "-p", Ant.TEST_PROJECT,
        "-f", TEST_FILE,
      });

    Map<String, Object> error = results.get(0);
    assertEquals(error.get("filename"),
        Eclim.resolveFile(Ant.TEST_PROJECT, TEST_FILE));
    assertEquals(error.get("message"),
        "Default target none does not exist in this project");
    assertEquals(error.get("line"), 5);
    assertEquals(error.get("column"), 2);
    assertEquals(error.get("warning"), false);
  }
}
