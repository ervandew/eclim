/**
 * Copyright (C) 2012 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.refactoring;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for MoveCommand.
 *
 * @author Eric Van Dewoestine
 */
public class MoveCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/refactoring/move/p1/TestMove.java";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
    throws Exception
  {
    String p = Eclim.resolveFile(Jdt.TEST_PROJECT, "");
    Map<String, Object> result = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_refactor_move", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-n", "org.eclim.test.refactoring.move.p3",
      });
    assertTrue(result.containsKey("errors"));
    List<String> errors = (List<String>)result.get("errors");
    assertEquals(1, errors.size());
    assertEquals(
        "'org.eclim.test.refactoring.move.p3' already contains a 'TestMove.java'.",
        errors.get(0));

    List<Map<String, String>> results = (List<Map<String, String>>)
      Eclim.execute(new String[]{
        "java_refactor_move", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-n", "org.eclim.test.refactoring.move.p2",
      });
    Map<String, String> file = results.get(0);
    Map<String, String> move = results.get(1);
    if (file.get("file") == null){
      file = results.get(1);
      move = results.get(0);
    }
    assertEquals(p + "src/org/eclim/test/refactoring/move/p1/UsesMove.java",
        file.get("file"));
    assertEquals(p + "src/org/eclim/test/refactoring/move/p1/TestMove.java",
        move.get("from"));
    assertEquals(p + "src/org/eclim/test/refactoring/move/p2/TestMove.java",
        move.get("to"));

    results = (List<Map<String, String>>)
      Eclim.execute(new String[]{"refactor_undo"});
    file = results.get(0);
    move = results.get(1);
    if (file.get("file") == null){
      file = results.get(1);
      move = results.get(0);
    }
    assertEquals(p + "src/org/eclim/test/refactoring/move/p1/UsesMove.java",
        file.get("file"));
    assertEquals(p + "src/org/eclim/test/refactoring/move/p2/TestMove.java",
        move.get("from"));
    assertEquals(p + "src/org/eclim/test/refactoring/move/p1/TestMove.java",
        move.get("to"));
  }
}
