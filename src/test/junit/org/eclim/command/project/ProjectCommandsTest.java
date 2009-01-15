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
package org.eclim.command.project;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the project commands.
 *
 * @author Eric Van Dewoestine
 */
public class ProjectCommandsTest
{
  private static final String TEST_PROJECT = "unit_test_created";
  private static final Pattern PROJECT_OPEN_PATTERN =
    Pattern.compile(TEST_PROJECT + "\\s+- open");

  @Test
  public void createProject()
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
  public void closeProject()
  {
    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "project_close", "-p", TEST_PROJECT});
    System.out.println(result);

    assertFalse("Project not closed.", projectOpen());
  }

  @Test
  public void openProject()
  {
    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "project_open", "-p", TEST_PROJECT});
    System.out.println(result);

    assertTrue("Project not opened.", projectOpen());
  }

  @Test
  public void deleteProject()
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
  private boolean projectOpen()
  {
    String list = Eclim.execute(new String[]{"project_list"});

    return PROJECT_OPEN_PATTERN.matcher(list).find();
  }
}
