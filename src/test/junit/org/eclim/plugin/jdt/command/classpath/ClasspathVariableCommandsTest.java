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
package org.eclim.plugin.jdt.command.classpath;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClasspathVariableCommandsTest
{
  private static final String TEST_VARIABLE = "/tmp";
  private static final String TEST_PATH = "ECLIM_UNIT_TEST_VARIABLE";
  private static final Pattern VARIABLE_PATTERN =
    Pattern.compile(TEST_VARIABLE + "\\s+-");

  @Test
  public void createVariable()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));
    assertFalse("Variable already exists.", variableExists());

    String result = Eclim.execute(new String[]{
      "java_classpath_variable_create", "-n", TEST_VARIABLE, "-p", TEST_PATH});
    System.out.println(result);

    assertTrue("Variable not created.", variableExists());
  }

  @Test
  public void deleteVariable()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));
    assertTrue("Variable does not exist.", variableExists());

    String result = Eclim.execute(new String[]{
      "java_classpath_variable_delete", "-n", TEST_VARIABLE});
    System.out.println(result);

    assertFalse("Variable not deleted.", variableExists());
  }

  /**
   * Determines if the test variable exists.
   *
   * @return true if the variable exists, false otherwise.
   */
  private boolean variableExists()
  {
    String list = Eclim.execute(new String[]{"java_classpath_variables"});

    return VARIABLE_PATTERN.matcher(list).find();
  }
}
