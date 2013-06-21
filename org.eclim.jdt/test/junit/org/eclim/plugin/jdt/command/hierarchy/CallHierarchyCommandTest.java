/**
 * Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.hierarchy;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CallHierarchyCommand.
 *
 * @author Alexandre Fonseca
 */
public class CallHierarchyCommandTest
{
  private static final String TEST_FILE_CALLEES = "src/hierarchy/TestHierarchy";
  private static final String TEST_FILE_CALLERS = 
    "src/hierarchy/TestHierarchyExternal";

  @Test
  @SuppressWarnings("unchecked")
  public void executeCallers()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    // reference to fun2
    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_callhierarchy", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE_CALLERS,
        "-o", "850", "-l", "22", "-e", "utf-8",
      });

    String path = Eclim.getProjectPath(Jdt.TEST_PROJECT) + "/src/hierarchy/";

    Map<String,Object> position = (Map<String,Object>)result.get("position");
    List<Map<String,Object>> calls =
      (List<Map<String,Object>>)result.get("callers");
    assertEquals(result.get("name"), "getEurythmics() : SweetDreams" +
        " - org.eclim.test.hierarchy.TestCallHierarchyExternal");
    assertEquals(path + "TestCallHierarchyExternal.java", position.get("filename"));
    assertEquals(21, position.get("line"));
    assertEquals(3, position.get("column"));
    assertEquals(2, calls.size());

    result = calls.get(0);
    position = (Map<String,Object>)result.get("position");
    List<Map<String,Object>> nestedCalls =
      (List<Map<String,Object>>)result.get("callers");
    assertEquals(result.get("name"), "barWithStuff(Object) : Object" +
        " - org.eclim.test.hierarchy.TestCallHierarchy");
    assertEquals(path + "TestCallHierarchy.java", position.get("filename"));
    assertEquals(35, position.get("line"));
    assertEquals(7, position.get("column"));
    assertEquals(1, nestedCalls.size());

    result = nestedCalls.get(0);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "foo() : void" +
        " - org.eclim.test.hierarchy.TestCallHierarchy");
    assertEquals(path + "TestCallHierarchy.java", position.get("filename"));
    assertEquals(25, position.get("line"));
    assertEquals(5, position.get("column"));

    result = calls.get(1);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "foo() : void" +
        " - org.eclim.test.hierarchy.TestCallHierarchy");
    assertEquals(path + "TestCallHierarchy.java", position.get("filename"));
    assertEquals(26, position.get("line"));
    assertEquals(27, position.get("column"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void executeCallees()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_callhierarchy", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE_CALLEES,
        "-o", "790", "-l", "22", "-e", "utf-8", "-c"
      });

    String path = Eclim.getProjectPath(Jdt.TEST_PROJECT) + "/src/hierarchy/";

    Map<String,Object> position = (Map<String,Object>)result.get("position");
    List<Map<String,Object>> calls =
      (List<Map<String,Object>>)result.get("callers");
    assertEquals(result.get("name"), "foo() : void" +
        " - org.eclim.test.hierarchy.TestCallHierarchy");
    assertEquals(path + "TestCallHierarchy.java", position.get("filename"));
    assertEquals(22, position.get("line"));
    assertEquals(3, position.get("column"));
    assertEquals(3, calls.size());

    result = calls.get(0);
    List<Map<String,Object>> nestedCalls =
      (List<Map<String,Object>>)result.get("callers");
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "SubClass()" +
        " - org.eclim.test.hierarchy.TestCallHierarchy.SubClass");
    assertEquals(path + "TestCallHierarchy.java", position.get("filename"));
    assertEquals(23, position.get("line"));
    assertEquals(42, position.get("column"));
    assertEquals(0, nestedCalls.size());

    result = calls.get(1);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "bar() : void" +
        " - org.eclim.test.hierarchy.TestCallHierarchy.SubClass");
    assertEquals(path + "TestCallHierarchy.java", position.get("filename"));
    assertEquals(24, position.get("line"));
    assertEquals(5, position.get("column"));

    result = calls.get(2);
    nestedCalls = (List<Map<String, Object>>) result.get("callers");
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "barWithStuff(Object) : Object" + 
        " - org.eclim.test.hierarchy.TestCallHierarchy.SubClass");
    assertEquals(path + "TestCallHierarchy.java", position.get("filename"));
    assertEquals(25, position.get("line"));
    assertEquals(5, position.get("column"));
    assertEquals(1, nestedCalls.size());

    result = nestedCalls.get(0);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "getEurythmics() : SweetDreams" + 
        " - org.eclim.test.hierarchy.TestCallHierarchyExternal");
    assertEquals(path + "TestCallHierarchy.java", position.get("filename"));
    assertEquals(35, position.get("line"));
    assertEquals(7, position.get("column"));
  }
}
