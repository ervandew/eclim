/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.correct;

import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CorrectCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CorrectCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/correct/TestCorrect.java";

  private static final String TEST_FILE_PACKAGE =
    "src/org/eclim/test/correct/TestCorrectPackage.java";

  @Test
  @SuppressWarnings("unchecked")
  public void suggest()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_correct", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-l", "5", "-o", "74", "-e", "utf-8"
      });

    assertEquals("ArrayList cannot be resolved to a type", result.get("message"));
    assertEquals(70, result.get("offset"));

    List<Map<String,Object>> results =
      (List<Map<String,Object>>)result.get("corrections");
    assertEquals(1, results.get(1).get("index"));
    assertEquals("Import 'ArrayList' (java.util)", results.get(1).get("description"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void apply()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_correct", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-l", "5", "-o", "74", "-e", "utf-8"
      });

    List<Map<String,Object>> results =
      (List<Map<String,Object>>)result.get("corrections");
    int apply = -1;
    for(Map<String,Object> r : results){
      if (r.get("description").equals("Import 'ArrayList' (java.util)")){
        apply = ((Integer)r.get("index")).intValue();
        break;
      }
    }
    assertTrue("Missing expected suggestion.", apply > -1);

    String applied = (String)Eclim.execute(new String[]{
      "java_correct", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-l", "5", "-o", "74", "-e", "utf-8", "-a", String.valueOf(apply)
    });

    assertTrue("Import not found.",
        Pattern.compile("import java\\.").matcher(applied).find());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void suggestPackage()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_correct", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE_PACKAGE,
        "-l", "1", "-o", "0", "-e", "utf-8"
      });

    assertEquals(
        "The declared package \"org.test\" does not match the expected " +
        "package \"org.eclim.test.correct\"",
        result.get("message"));

    List<Map<String,Object>> results =
      (List<Map<String,Object>>)result.get("corrections");
    assertEquals(
        "Change package declaration to 'org.eclim.test.correct'",
        results.get(0).get("description"));
    assertEquals(
        "...\npackage org.eclim.test.correct;\n...\n",
        results.get(0).get("preview"));
  }

  @Test
  public void applyPackage()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = (String)Eclim.execute(new String[]{
      "java_correct", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE_PACKAGE,
      "-l", "1", "-o", "0", "-e", "utf-8", "-a", "0"
    });

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Incorrect package", "package org.eclim.test.correct;", results[0]);
  }
}
