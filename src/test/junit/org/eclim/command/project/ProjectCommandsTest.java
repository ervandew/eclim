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
package org.eclim.command.project;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the project commands.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectCommandsTest
{
  private static final String TEST_PROJECT = "eclim_unit_test";
  private static final Pattern PROJECT_OPEN_PATTERN =
    Pattern.compile(TEST_PROJECT + "\\s+- open");

  @Test
  public void createProject ()
  {
    assertFalse("Project already exists.", Eclim.projectExists(TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "project_create",
      "-f", Eclim.getWorkspace() + "/" + TEST_PROJECT,
      "-n", "java"
    });
    System.out.println(result);

    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));
  }

  @Test
  public void closeProject ()
  {
    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "project_close", "-p", TEST_PROJECT});
    System.out.println(result);

    assertFalse("Project not closed.", projectOpen());
  }

  @Test
  public void openProject ()
  {
    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "project_open", "-p", TEST_PROJECT});
    System.out.println(result);

    assertTrue("Project not opened.", projectOpen());
  }

  @Test
  public void deleteProject ()
  {
    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "project_delete", "-p", TEST_PROJECT});
    System.out.println(result);

    assertFalse("Project not deleted.", Eclim.projectExists(TEST_PROJECT));
  }

  /**
   * Determines if the unit test project is open.
   *
   * @return true if the project is open, false otherwise.
   */
  private boolean projectOpen ()
  {
    String list = Eclim.execute(new String[]{"project_list"});

    return PROJECT_OPEN_PATTERN.matcher(list).find();
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(ProjectCommandsTest.class);
  }
}
