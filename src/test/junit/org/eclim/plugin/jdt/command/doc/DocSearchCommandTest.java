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
package org.eclim.plugin.jdt.command.doc;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for DocSearchCommand.
 *
 * @author Eric Van Dewoestine
 */
public class DocSearchCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/doc/TestDocSearch.java";

  @Test
  public void elementSearch()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_docsearch", "-n", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "68", "-e", "utf-8", "-l", "4", "-x", "declarations"
    });

    System.out.println(result);

    result = result.replaceAll(
        "java.sun.com/(.*?)/docs", "java.sun.com/\\${version}/docs");
    assertEquals("Wrong result.",
        "http://java.sun.com/${version}/docs/api/java/awt/List.html", result);
  }

  @Test
  public void patternSearch()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_docsearch", "-n", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-p", "ArrayList"
    });

    System.out.println(result);

    result = result.replaceAll(
        "java.sun.com/(.*?)/docs", "java.sun.com/\\${version}/docs");
    assertEquals("Wrong result.",
        "http://java.sun.com/${version}/docs/api/java/util/ArrayList.html\n" +
        "http://java.sun.com/${version}/docs/api/java/util/Arrays.ArrayList.html",
        result);
  }

  @Test
  public void methodSearch()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_docsearch", "-n", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-p", "currentTime%2A", "-t", "method"
    });

    System.out.println(result);

    result = result.replaceAll(
        "java.sun.com/(.*?)/docs", "java.sun.com/\\${version}/docs");
    assertEquals("Wrong result.",
        "http://java.sun.com/${version}/docs/api/java/lang/" +
        "System.html#currentTimeMillis()",
        result);
  }
}
