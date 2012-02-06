/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.delegate;

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
    "src/org/eclim/test/delegate/TestDelegate.java";

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
        "-o", "124", "-e", "utf-8"
      });

    assertEquals("org.eclim.test.delegate.TestDelegate", result.get("type"));

    List<Map<String,Object>> types =
      (List<Map<String,Object>>)result.get("superTypes");
    assertEquals(4, types.size());

    assertEquals("java.util", types.get(0).get("packageName"));
    assertEquals("public interface List<Double>",
        types.get(0).get("signature"));

    assertEquals("public abstract Iterator<Double> iterator()",
        ((List<Map<String,Object>>)
         types.get(0).get("methods")).get(3).get("signature"));
    assertEquals("public abstract boolean add(Double e)",
        ((List<Map<String,Object>>)
         types.get(0).get("methods")).get(6).get("signature"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_delegate", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "124", "-e", "utf-8",
        "-t", "org.eclim.test.delegate.TestDelegate",
        "-s", "java.util.List%3CDouble%3E", "-m", "add(Double)"
      });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public boolean add\\(Double \\w\\)\\s*\\{\n  " +
          "\\s+return list.add\\(\\w\\);")
        .matcher(contents).find());

    types = (List<Map<String,Object>>)result.get("superTypes");
    assertEquals("public abstract boolean add(Double e)",
        ((List<Map<String,Object>>)
         types.get(0).get("methods")).get(6).get("signature"));
    assertEquals(true,
        ((List<Map<String,Object>>)
         types.get(0).get("methods")).get(6).get("implemented"));
  }
}
