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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ImportMissingCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ImportMissingCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/include/TestImportMissing.java";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "java_import_missing", "-p", Jdt.TEST_PROJECT, "-f", TEST_FILE
      });

    // remove any com.sun entries
    for(Map<String,Object> entry : results){
      List<String> imports = new ArrayList<String>();
      for (String imprt : (List<String>)entry.get("imports")){
        if (imprt.matches("com\\.sun.*?")){
          continue;
        }
        imports.add(imprt);
      }
      entry.put("imports", imports);
    }

    assertEquals(3, results.size());

    List<String> imports = (List<String>)results.get(0).get("imports");
    assertEquals("List", results.get(0).get("type"));
    assertEquals(2, imports.size());
    assertEquals("java.awt.List", imports.get(0));
    assertEquals("java.util.List", imports.get(1));

    imports = (List<String>)results.get(1).get("imports");
    assertEquals("ArrayList", results.get(1).get("type"));
    assertEquals(1, imports.size());
    assertEquals("java.util.ArrayList", imports.get(0));

    imports = (List<String>)results.get(2).get("imports");
    assertEquals("FooBar", results.get(2).get("type"));
    assertEquals(0, imports.size());
  }
}
