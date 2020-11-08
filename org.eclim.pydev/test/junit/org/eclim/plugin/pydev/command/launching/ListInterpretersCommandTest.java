/**
 * Copyright (C) 2014 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.launching;

import java.util.List;

import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.pydev.Pydev;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ListInterpretersCommand, AddInterpreterCommand, and
 * RemoveInterpreterCommand.
 *
 * Note: this test expects that an interpreter has already been added.
 *
 * @author Eric Van Dewoestine
 */
public class ListInterpretersCommandTest
{
  @Test
  @SuppressWarnings("unchecked")
  public void list()
  {
    assertTrue("Python project doesn't exist.",
        Eclim.projectExists(Pydev.TEST_PROJECT));

    List<Map<String, String>> results = (List<Map<String, String>>)
      Eclim.execute(new String[]{"python_list_interpreters"});

    assertTrue(results.size() > 0);

    Map<String, String> result = results.get(0);
    assertTrue(result.containsKey("name"));
    assertTrue(result.containsKey("version"));
    assertTrue(result.containsKey("path"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void removeAdd()
  {
    assertTrue("Python project doesn't exist.",
        Eclim.projectExists(Pydev.TEST_PROJECT));

    List<Map<String, String>> results = (List<Map<String, String>>)
      Eclim.execute(new String[]{"python_list_interpreters"});

    int size = results.size();
    assertTrue(size > 0);

    Map<String, String> result = results.get(0);
    assertTrue(result.containsKey("name"));
    assertTrue(result.containsKey("version"));
    assertTrue(result.containsKey("path"));

    String name = result.get("name");
    String path = result.get("path");

    // remove it
    Eclim.execute(new String[]{
      "python_remove_interpreter", "-p", path,
    });
    results = (List<Map<String, String>>)
      Eclim.execute(new String[]{"python_list_interpreters"});
    assertEquals(results.size(), size - 1);

    // re-add it
    Eclim.execute(new String[]{
      "python_add_interpreter", "-p", path, "-n", name,
    });
    results = (List<Map<String, String>>)
      Eclim.execute(new String[]{"python_list_interpreters"});
    assertEquals(results.size(), size);
    assertEquals(results.get(0), result);
  }
}
