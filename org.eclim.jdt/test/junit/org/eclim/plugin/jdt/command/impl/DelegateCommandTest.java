/**
 * Copyright (C) 2005 - 2018  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for DelegateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class DelegateCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/impl/TestDelegate.java";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_delegate", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "120", "-e", "utf-8"
      });

    assertEquals("org.eclim.test.impl.TestDelegate.list", result.get("type"));

    List<Map<String,Object>> types =
      (List<Map<String,Object>>)result.get("superTypes");
    assertEquals(3, types.size());

    assertEquals("java.util", types.get(0).get("packageName"));
    assertEquals("interface List<Double>",
        types.get(0).get("signature"));
    HashSet<String> methods = new HashSet<String>(
        (List<String>)types.get(0).get("methods"));

    assertTrue(methods.contains("public abstract Iterator<Double> iterator()"));
    assertTrue(methods.contains("public abstract boolean add(Double)"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_delegate", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "120", "-e", "utf-8",
        "-v", "org.eclim.test.impl.TestDelegate.list",
        "-s", "java.util.List", "-m", "[\"add(Double)\", \"iterator()\"]"
      });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public boolean add\\(Double \\w\\)\\s*\\{\n" +
          "\\s+return list.add\\(\\w\\);")
        .matcher(contents).find());
    assertTrue("Method not found or invalid.",
        Pattern.compile("public Iterator<Double> iterator\\(\\)\\s*\\{\n" +
          "\\s+return list.iterator\\(\\);")
        .matcher(contents).find());

    types = (List<Map<String,Object>>)result.get("superTypes");
    methods = new HashSet<String>( (List<String>)types.get(0).get("methods"));
    assertFalse(methods.contains("public abstract Iterator<Double> iterator()"));
    assertFalse(methods.contains("public abstract boolean add(Double)"));
  }
}
