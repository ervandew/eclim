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
package org.eclim.plugin.jdt.command.include;

import java.util.List;
import java.util.Map;

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

    Pattern listImport = Pattern.compile("import\\s+java\\.util\\.List;");
    String file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertFalse(listImport.matcher(file).find());

    List<String> results = (List<String>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "72", "-e", "utf-8",
    });

    assertEquals(2, results.size());
    assertEquals("java.awt.List", results.get(0));
    assertEquals("java.util.List", results.get(1));
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertFalse(listImport.matcher(file).find());

    Map<String,Object> position = (Map<String,Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "72", "-e", "utf-8", "-t", "java.util.List",
    });
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue(listImport.matcher(file).find());
    assertEquals(96, position.get("offset"));
    assertEquals(7, position.get("line"));
    assertEquals(14, position.get("column"));

    position = (Map<String,Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "109", "-e", "utf-8",
    });
    Pattern arrayListImport = Pattern.compile("import\\s+java\\.util\\.ArrayList;");
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue(arrayListImport.matcher(file).find());
    assertEquals(137, position.get("offset"));
    assertEquals(8, position.get("line"));
    assertEquals(27, position.get("column"));

    position = (Map<String,Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "163", "-e", "utf-8",
    });
    Pattern patternImport = Pattern.compile("import\\s+java\\.util\\.regex\\.Pattern;");
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue(patternImport.matcher(file).find());
    assertEquals(196, position.get("offset"));
    assertEquals(11, position.get("line"));
    assertEquals(14, position.get("column"));

    position = (Map<String,Object>)Eclim.execute(new String[]{
      "java_import", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "220", "-e", "utf-8",
    });
    Pattern fileImport = Pattern.compile("import\\s+java\\.io\\.File;");
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue(fileImport.matcher(file).find());
    assertEquals(242, position.get("offset"));
    assertEquals(14, position.get("line"));
    assertEquals(11, position.get("column"));
  }
}
