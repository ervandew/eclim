/**
 * Copyright (C) 2012 - 2021  Eric Van Dewoestine
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
 * Test case for ImportOrganizeCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ImportOrganizeCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/include/TestImportOrganize.java";

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

    List<List<String>> results = (List<List<String>>)
      Eclim.execute(new String[]{
        "java_import_organize", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "185", "-e", "utf-8",
      });

    assertEquals(1, results.size());
    List<String> entries = results.get(0);
    assertEquals("java.security.KeyStore.Entry", entries.get(0));
    assertEquals("java.util.Map.Entry", entries.get(1));
    assertEquals("javax.swing.RowFilter.Entry", entries.get(2));

    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertFalse(listImport.matcher(file).find());

    Map<String, Object> position = (Map<String, Object>)Eclim.execute(new String[]{
      "java_import_organize", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "185", "-e", "utf-8", "-t", "java.util.Map.Entry",
    });
    file = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue(listImport.matcher(file).find());
    assertEquals(238, position.get("offset"));
    assertEquals(13, position.get("line"));
    assertEquals(16, position.get("column"));
  }
}
