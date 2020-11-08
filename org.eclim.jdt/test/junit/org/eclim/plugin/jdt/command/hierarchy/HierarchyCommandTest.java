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
package org.eclim.plugin.jdt.command.hierarchy;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for HierarchyCommand.
 *
 * @author Eric Van Dewoestine
 */
public class HierarchyCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/hierarchy/TestHierarchy.java";

  @Test
  @SuppressWarnings("unchecked")
  public void test()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String, Object> result = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_hierarchy", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "1", "-e", "utf-8",
      });

    assertEquals(result.get("name"), "public class TestHierarchy");
    assertEquals(result.get("qualified"), "org.eclim.test.hierarchy.TestHierarchy");

    List<Map<String, Object>> children = (List<Map<String, Object>>)
      result.get("children");
    Map<String, Object> child = children.get(0);
    assertEquals(child.get("name"), "public class Component");
    assertEquals(child.get("qualified"), "java.awt.Component");

    List<Map<String, Object>> subChildren =
      (List<Map<String, Object>>)child.get("children");
    child = subChildren.get(0);
    assertEquals(child.get("name"), "public interface ImageObserver");
    assertEquals(child.get("qualified"), "java.awt.image.ImageObserver");
    assertEquals(((List<Object>)child.get("children")).size(), 0);

    child = subChildren.get(1);
    assertEquals(child.get("name"), "public interface MenuContainer");
    assertEquals(child.get("qualified"), "java.awt.MenuContainer");
    assertEquals(((List<Object>)child.get("children")).size(), 0);

    child = subChildren.get(2);
    assertEquals(child.get("name"), "public interface Serializable");
    assertEquals(child.get("qualified"), "java.io.Serializable");
    assertEquals(((List<Object>)child.get("children")).size(), 0);

    child = children.get(1);
    assertEquals(child.get("name"), "public interface Comparable");
    assertEquals(child.get("qualified"), "java.lang.Comparable");
    assertEquals(((List<Object>)child.get("children")).size(), 0);

    child = children.get(2);
    assertEquals(child.get("name"), "public interface PropertyChangeListener");
    assertEquals(child.get("qualified"), "java.beans.PropertyChangeListener");

    children = (List<Map<String, Object>>)child.get("children");
    child = children.get(0);
    assertEquals(child.get("name"), "public interface EventListener");
    assertEquals(child.get("qualified"), "java.util.EventListener");
    assertEquals(((List<Object>)child.get("children")).size(), 0);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testNested()
  {
    Map<String, Object> result = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_hierarchy", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "967", "-e", "utf-8",
      });

    assertEquals(result.get("name"), "class TestHierarchy.TestNested");
    assertEquals(result.get("qualified"), "org.eclim.test.hierarchy.TestHierarchy");

    List<Map<String, Object>> children =
      (List<Map<String, Object>>)result.get("children");
    Map<String, Object> child = children.get(0);
    assertEquals(child.get("name"), "public interface Comparable");
    assertEquals(child.get("qualified"), "java.lang.Comparable");
    assertEquals(((List<Object>)child.get("children")).size(), 0);

    child = children.get(1);
    assertEquals(child.get("name"), "public interface PropertyChangeListener");
    assertEquals(child.get("qualified"), "java.beans.PropertyChangeListener");

    children = (List<Map<String, Object>>)child.get("children");
    child = children.get(0);
    assertEquals(child.get("name"), "public interface EventListener");
    assertEquals(child.get("qualified"), "java.util.EventListener");
    assertEquals(((List<Object>)child.get("children")).size(), 0);

    result = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_hierarchy", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "1075", "-e", "utf-8",
      });

    assertEquals(result.get("name"), "class TestHierarchy.Test");
    assertEquals(result.get("qualified"), "org.eclim.test.hierarchy.TestHierarchy");
    assertEquals(((List<Object>)result.get("children")).size(), 0);
  }
}

