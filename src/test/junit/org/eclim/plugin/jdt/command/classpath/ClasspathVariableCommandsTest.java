/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  public void createVariable ()
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
  public void deleteVariable ()
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
  private boolean variableExists ()
  {
    String list = Eclim.execute(new String[]{"java_classpath_variables"});

    return VARIABLE_PATTERN.matcher(list).find();
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(
        ClasspathVariableCommandsTest.class);
  }
}
