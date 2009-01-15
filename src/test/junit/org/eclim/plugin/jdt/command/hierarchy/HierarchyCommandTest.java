/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
  public void test()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_hierarchy", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "1", "-e", "utf-8"
    });

    System.out.println(result);

    assertEquals("Wrong result.", result, "{'name':'public class TestHierarchy','qualified':'org.eclim.test.hierarchy.TestHierarchy','children':[{'name':'public class Component','qualified':'java.awt.Component','children':[{'name':'public interface ImageObserver','qualified':'java.awt.image.ImageObserver','children':[]},{'name':'public interface MenuContainer','qualified':'java.awt.MenuContainer','children':[]},{'name':'public interface Serializable','qualified':'java.io.Serializable','children':[]}]},{'name':'public interface Comparable','qualified':'java.lang.Comparable','children':[]},{'name':'public interface PropertyChangeListener','qualified':'java.beans.PropertyChangeListener','children':[{'name':'public interface EventListener','qualified':'java.util.EventListener','children':[]}]}]}");

    result = Eclim.execute(new String[]{
      "java_hierarchy", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "967", "-e", "utf-8"
    });

    System.out.println(result);

    assertEquals("Wrong result.", result, "{'name':'class TestHierarchy.TestNested','qualified':'org.eclim.test.hierarchy.TestHierarchy','children':[{'name':'public interface Comparable','qualified':'java.lang.Comparable','children':[]},{'name':'public interface PropertyChangeListener','qualified':'java.beans.PropertyChangeListener','children':[{'name':'public interface EventListener','qualified':'java.util.EventListener','children':[]}]}]}");

    result = Eclim.execute(new String[]{
      "java_hierarchy", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "1075", "-e", "utf-8"
    });

    System.out.println(result);

    assertEquals("Wrong result.", result, "{'name':'class TestHierarchy.Test','qualified':'org.eclim.test.hierarchy.TestHierarchy','children':[]}");
  }
}
