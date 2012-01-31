/**
 * Copyright (C) 2012 Eric Van Dewoestine
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
package org.eclim.plugin.adt.project;

import java.io.File;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for AndroidProjectManager.
 *
 * @author Eric Van Dewoestine
 */
public class AndroidProjectManagerTest
{
  @Test
  @SuppressWarnings("unchecked")
  public void create()
  {
    String path = Eclim.getWorkspace() + "/eclim_unit_test_android_junit";
    Eclim.execute(new String[]{
      "project_delete", "-p", "eclim_unit_test_android_junit"}, false);
    Eclim.deleteDirectory(new File(path));

    Object result = Eclim.execute(new String[]{
      "project_create",
      "-f", path,
      "-n", "android",
    }, false);
    assertEquals("Missing required options: target, package, application", result);

    List<Map<String,String>> results = (List<Map<String,String>>)
      Eclim.execute(new String[]{"android_list_targets"});
    assertTrue("No targets returned", results.size() > 0);
    Map<String,String> target = results.get(0);

    result = Eclim.execute(new String[]{
      "project_create",
      "-f", path,
      "-n", "android",
      "-a",
      "--target", target.get("hash"),
    }, false);
    assertEquals(result, "Missing required options: package, application");

    result = Eclim.execute(new String[]{
      "project_create",
      "-f", path,
      "-n", "android",
      "-a",
      "--target", target.get("hash"),
      "--package", "org.test",
      "--application", "Test JUnit App",
      "--activity", "TestJUnitActivity",
    }, false);
    assertEquals("Created project 'eclim_unit_test_android_junit'.", result);
    assertTrue(
        "Activity not created",
        new File(path + "/src/org/test/TestJUnitActivity.java").exists());
  }
}
