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
package org.eclim.plugin.jdt.command.classpath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClasspathVariableCommandsTest
{
  private static final String TEST_VARIABLE = "ECLIM_UNIT_TEST_VARIABLE";
  private static final String TEST_PATH = "/tmp";

  @Test
  public void createVariable()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));
    Eclim.execute(new String[]{
      "java_classpath_variable_delete", "-n", TEST_VARIABLE});
    assertFalse("Variable already exists.", variableExists());

    Eclim.execute(new String[]{
      "java_classpath_variable_create", "-n", TEST_VARIABLE, "-p", TEST_PATH});

    assertTrue("Variable not created.", variableExists());
  }

  @Test
  public void deleteVariable()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));
    Eclim.execute(new String[]{
      "java_classpath_variable_create", "-n", TEST_VARIABLE, "-p", TEST_PATH});
    assertTrue("Variable does not exist.", variableExists());

    Eclim.execute(new String[]{
      "java_classpath_variable_delete", "-n", TEST_VARIABLE});

    assertFalse("Variable not deleted.", variableExists());
  }

  /**
   * Determines if the test variable exists.
   *
   * @return true if the variable exists, false otherwise.
   */
  @SuppressWarnings("unchecked")
  private boolean variableExists()
  {
    List<Map<String, String>> list = (List<Map<String, String>>)
      Eclim.execute(new String[]{"java_classpath_variables"});

    Map<String, String> var = new HashMap<String, String>();
    var.put("name", TEST_VARIABLE);
    var.put("path", TEST_PATH);
    return list.contains(var);
  }
}
