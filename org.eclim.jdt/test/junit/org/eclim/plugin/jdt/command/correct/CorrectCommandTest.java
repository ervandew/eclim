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
package org.eclim.plugin.jdt.command.correct;

import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;
import org.eclim.EclimTestCase;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CorrectCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CorrectCommandTest
  extends EclimTestCase
{
  private static final String TEST_FILE =
    "src/org/eclim/test/correct/TestCorrect.java";

  private static final String TEST_FILE_PACKAGE =
    "src/org/eclim/test/correct/TestCorrectPackage.java";

  @Test
  @SuppressWarnings("unchecked")
  public void unresolvedType()
  {
    modifies(Jdt.TEST_PROJECT, TEST_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String, Object> result = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_correct", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-l", "5", "-o", "74", "-e", "utf-8",
      });

    assertEquals("ArrayList cannot be resolved to a type", result.get("message"));
    assertEquals(70, result.get("offset"));

    List<Map<String, Object>> results =
      (List<Map<String, Object>>)result.get("corrections");
    assertEquals(0, results.get(0).get("index"));
    assertEquals(
        "Import 'ArrayList' (java.util)",
        results.get(0).get("description"));

    int apply = -1;
    for(Map<String, Object> r : results){
      if (r.get("description").equals("Import 'ArrayList' (java.util)")){
        apply = ((Integer)r.get("index")).intValue();
        break;
      }
    }
    assertTrue("Missing expected suggestion.", apply > -1);

    List<Map<String, String>> changes = (List<Map<String, String>>)
      Eclim.execute(new String[]{
        "java_correct", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-l", "5", "-o", "74", "-e", "utf-8", "-a", String.valueOf(apply),
      });

    assertEquals(1, changes.size());
    assertEquals(
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE),
        changes.get(0).get("file"));

    String file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Import not found.",
        Pattern.compile("import java\\.").matcher(file).find());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void incorrectPackage()
  {
    modifies(Jdt.TEST_PROJECT, TEST_FILE_PACKAGE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String, Object> result = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_correct", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE_PACKAGE,
        "-l", "1", "-o", "0", "-e", "utf-8",
      });

    assertEquals(
        "The declared package \"org.test\" does not match the expected " +
        "package \"org.eclim.test.correct\"",
        result.get("message"));

    List<Map<String, Object>> results =
      (List<Map<String, Object>>)result.get("corrections");
    assertEquals(
        "Change package declaration to 'org.eclim.test.correct'",
        results.get(1).get("description"));
    assertEquals(
        "...\npackage org.eclim.test.correct;\n...\n",
        results.get(1).get("preview"));

    List<Map<String, String>> changes = (List<Map<String, String>>)
      Eclim.execute(new String[]{
        "java_correct", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE_PACKAGE,
        "-l", "1", "-o", "0", "-e", "utf-8", "-a", "1",
      });

    assertEquals(1, changes.size());
    assertEquals(
        Eclim.resolveFile(Jdt.TEST_PROJECT, TEST_FILE_PACKAGE),
        changes.get(0).get("file"));

    String file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE_PACKAGE);
    String[] lines = StringUtils.split(file, '\n');
    assertEquals("Incorrect package", "package org.eclim.test.correct;", lines[0]);
  }
}
