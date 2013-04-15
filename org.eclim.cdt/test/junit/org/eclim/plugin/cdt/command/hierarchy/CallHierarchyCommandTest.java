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
package org.eclim.plugin.cdt.command.hierarchy;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.cdt.Cdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CallHierarchyCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CallHierarchyCommandTest
{
  private static final String TEST_FILE = "src/callhierarchy/mod2.c";
  private static final String TEST_FILE_LINK = "src-link/link.c";

  @Test
  @SuppressWarnings("unchecked")
  public void executeCallers()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    // reference to fun2
    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "c_callhierarchy", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "57", "-l", "4", "-e", "utf-8"
      });

    String path = Eclim.getProjectPath(Cdt.TEST_PROJECT) + "/src/callhierarchy/";

    Map<String,Object> position = (Map<String,Object>)result.get("position");
    List<Map<String,Object>> calls =
      (List<Map<String,Object>>)result.get("callers");
    assertEquals(result.get("name"), "fun2(int)");
    assertEquals(path + "mod2.c", position.get("filename"));
    assertEquals(1, position.get("line"));
    assertEquals(5, position.get("column"));
    assertEquals(3, calls.size());

    result = calls.get(0);
    position = (Map<String,Object>)result.get("position");
    List<Map<String,Object>> nestedCalls =
      (List<Map<String,Object>>)result.get("callers");
    assertEquals(result.get("name"), "fun1(int)");
    assertEquals(path + "mod1.c", position.get("filename"));
    assertEquals(5, position.get("line"));
    assertEquals(10, position.get("column"));
    assertEquals(2, nestedCalls.size());

    result = nestedCalls.get(0);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "main()");
    assertEquals(path + "main.c", position.get("filename"));
    assertEquals(6, position.get("line"));
    assertEquals(28, position.get("column"));

    result = nestedCalls.get(1);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "fun3(int)");
    assertEquals(path + "mod2.c", position.get("filename"));
    assertEquals(7, position.get("line"));
    assertEquals(10, position.get("column"));

    result = calls.get(1);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "fun3(int)");
    assertEquals(path + "mod2.c", position.get("filename"));
    assertEquals(6, position.get("line"));
    assertEquals(3, position.get("column"));

    result = calls.get(2);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "fun3(int)");
    assertEquals(path + "mod2.c", position.get("filename"));
    assertEquals(7, position.get("line"));
    assertEquals(20, position.get("column"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void executeCallees()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    // callees in fun3
    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "c_callhierarchy", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "41", "-l", "4", "-e", "utf-8", "-c"
      });

    String path = Eclim.getProjectPath(Cdt.TEST_PROJECT) + "/src/callhierarchy/";

    Map<String,Object> position = (Map<String,Object>)result.get("position");
    List<Map<String,Object>> calls =
      (List<Map<String,Object>>)result.get("callees");
    assertEquals(result.get("name"), "fun3(int)");
    assertEquals(path + "mod2.c", position.get("filename"));
    assertEquals(5, position.get("line"));
    assertEquals(5, position.get("column"));
    assertEquals(3, calls.size());

    result = calls.get(0);
    position = (Map<String,Object>)result.get("position");
    List<Map<String,Object>> nestedCalls =
      (List<Map<String,Object>>)result.get("callees");
    assertEquals(result.get("name"), "fun1(int)");
    assertEquals(path + "mod2.c", position.get("filename"));
    assertEquals(7, position.get("line"));
    assertEquals(10, position.get("column"));
    assertEquals(1, nestedCalls.size());

    result = nestedCalls.get(0);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "fun2(int)");
    assertEquals(path + "mod1.c", position.get("filename"));
    assertEquals(5, position.get("line"));
    assertEquals(10, position.get("column"));

    result = calls.get(1);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "fun2(int)");
    assertEquals(path + "mod2.c", position.get("filename"));
    assertEquals(6, position.get("line"));
    assertEquals(3, position.get("column"));

    result = calls.get(2);
    position = (Map<String,Object>)result.get("position");
    assertEquals(result.get("name"), "fun2(int)");
    assertEquals(path + "mod2.c", position.get("filename"));
    assertEquals(7, position.get("line"));
    assertEquals(20, position.get("column"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void executeLinked()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Cdt.TEST_PROJECT));

    // reference to link1
    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "c_callhierarchy", "-p", Cdt.TEST_PROJECT, "-f", TEST_FILE_LINK,
        "-o", "107", "-l", "5", "-e", "utf-8"
      });

    String path = Eclim.getProjectPath(Cdt.TEST_PROJECT) + "/src-link/";

    Map<String,Object> position = (Map<String,Object>)result.get("position");
    List<Map<String,Object>> calls =
      (List<Map<String,Object>>)result.get("callers");
    assertEquals(result.get("name"), "link1(int)");
    assertEquals(path + "link.c", position.get("filename"));
    assertEquals(1, position.get("line"));
    assertEquals(5, position.get("column"));
    assertEquals(2, calls.size());
  }
}
