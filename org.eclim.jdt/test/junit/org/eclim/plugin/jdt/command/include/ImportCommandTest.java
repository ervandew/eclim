/**
 * Copyright (C) 2005 - 2021  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.include;

import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ImportCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ImportCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/include/TestImport.java";

  @BeforeClass
  public static void setUp()
  {
    Eclim.setProjectSetting(Jdt.TEST_PROJECT,
        "org.eclim.java.import.package_separation_level", "-1");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Pattern entryImport = Pattern.compile("import java\\.util\\.Map\\.Entry;");
    String file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertFalse(entryImport.matcher(file).find());

    List<String> results = (List<String>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "73", "-e", "utf-8",
    });

    assertEquals(3, results.size());
    assertEquals("java.security.KeyStore.Entry", results.get(0));
    assertEquals("java.util.Map.Entry", results.get(1));
    assertEquals("javax.swing.RowFilter.Entry", results.get(2));
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertFalse(entryImport.matcher(file).find());

    Map<String, Object> position = (Map<String, Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "72", "-e", "utf-8", "-t", "java.util.Map.Entry",
    });
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    Matcher entryMatcher = entryImport.matcher(file);
    assertTrue(entryMatcher.find());
    assertEquals(33, entryMatcher.start());
    assertEquals(101, position.get("offset"));
    assertEquals(7, position.get("line"));
    assertEquals(14, position.get("column"));

    position = (Map<String, Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "111", "-e", "utf-8",
    });
    Pattern callableImport =
      Pattern.compile("import java\\.util\\.concurrent\\.Callable;");
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    Matcher callableMatcher = callableImport.matcher(file);
    entryMatcher = entryImport.matcher(file);
    assertTrue(entryMatcher.find());
    assertTrue(callableMatcher.find());
    assertEquals(33, entryMatcher.start());
    assertEquals(62, callableMatcher.start());
    assertEquals(150, position.get("offset"));
    assertEquals(9, position.get("line"));
    assertEquals(24, position.get("column"));

    position = (Map<String, Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "179", "-e", "utf-8",
    });
    Pattern mapImport = Pattern.compile("import java\\.util\\.Map;");
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    Matcher mapMatcher = mapImport.matcher(file);
    entryMatcher = entryImport.matcher(file);
    callableMatcher = callableImport.matcher(file);
    assertTrue(entryMatcher.find());
    assertTrue(mapMatcher.find());
    assertTrue(callableMatcher.find());
    assertEquals(55, entryMatcher.start());
    assertEquals(33, mapMatcher.start());
    assertEquals(84, callableMatcher.start());
    assertEquals(201, position.get("offset"));
    assertEquals(11, position.get("line"));
    assertEquals(11, position.get("column"));

    position = (Map<String, Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "233", "-e", "utf-8",
    });
    Pattern patternImport = Pattern.compile("import java\\.util\\.regex\\.Pattern;");
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    Matcher patternMatcher = patternImport.matcher(file);
    entryMatcher = entryImport.matcher(file);
    mapMatcher = mapImport.matcher(file);
    callableMatcher = callableImport.matcher(file);
    assertTrue(entryMatcher.find());
    assertTrue(mapMatcher.find());
    assertTrue(callableMatcher.find());
    assertTrue(patternMatcher.find());
    assertEquals(55, entryMatcher.start());
    assertEquals(33, mapMatcher.start());
    assertEquals(84, callableMatcher.start());
    assertEquals(123, patternMatcher.start());
    assertEquals(266, position.get("offset"));
    assertEquals(14, position.get("line"));
    assertEquals(11, position.get("column"));

    position = (Map<String, Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "293", "-e", "utf-8",
    });
    Pattern fileImport = Pattern.compile("import java\\.io\\.File;");
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    Matcher fileMatcher = fileImport.matcher(file);
    entryMatcher = entryImport.matcher(file);
    mapMatcher = mapImport.matcher(file);
    callableMatcher = callableImport.matcher(file);
    patternMatcher = patternImport.matcher(file);
    assertTrue(fileMatcher.find());
    assertTrue(entryMatcher.find());
    assertTrue(mapMatcher.find());
    assertTrue(callableMatcher.find());
    assertTrue(patternMatcher.find());
    assertEquals(33, fileMatcher.start());
    assertEquals(77, entryMatcher.start());
    assertEquals(55, mapMatcher.start());
    assertEquals(106, callableMatcher.start());
    assertEquals(145, patternMatcher.start());
    assertEquals(315, position.get("offset"));
    assertEquals(17, position.get("line"));
    assertEquals(11, position.get("column"));

    Pattern classDecl = Pattern.compile("public class TestImport");
    Matcher classMatcher = classDecl.matcher(file);
    assertTrue(classMatcher.find());
    assertEquals(178, classMatcher.start());
  }
}
