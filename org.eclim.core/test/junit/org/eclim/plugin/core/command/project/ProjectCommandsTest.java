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
package org.eclim.plugin.core.command.project;

import java.io.File;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the project commands.
 *
 * @author Eric Van Dewoestine
 */
public class ProjectCommandsTest
{
  private static final String TEST_PROJECT = "unit_test";
  private static final String TEST_PROJECT_CREATE = "unit_test_create";
  private static final String TEST_PROJECT_IMPORT = "unit_test_import";

  @Before
  public void setUp()
  {
    if (!Eclim.projectExists(TEST_PROJECT)){
      Eclim.execute(new String[]{
        "project_create",
        "-f", Eclim.getWorkspace() + "/" + TEST_PROJECT,
        "-n", "java",
      });
    }
  }

  @After
  public void tearDown()
  {
    if (Eclim.projectExists(TEST_PROJECT)){
      Eclim.execute(new String[]{"project_delete", "-p", TEST_PROJECT});

      // delete the project files + dir
      File dir = new File(Eclim.getWorkspace() + "/" + TEST_PROJECT);
      for(File f : dir.listFiles()){
        f.delete();
      }
      dir.delete();
    }
  }

  @Test
  public void createDeleteProject()
  {
    // delete the test project if it exists
    if (Eclim.projectExists(TEST_PROJECT)){
      Eclim.execute(new String[]{"project_delete", "-p", TEST_PROJECT_CREATE});
    }
    assertFalse("Project already exists.", Eclim.projectExists(TEST_PROJECT_CREATE));

    Eclim.execute(new String[]{
      "project_create",
      "-f", Eclim.getWorkspace() + "/" + TEST_PROJECT_CREATE,
      "-n", "java",
    });

    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT_CREATE));

    Eclim.execute(new String[]{"project_delete", "-p", TEST_PROJECT_CREATE});

    assertFalse("Project not deleted.", Eclim.projectExists(TEST_PROJECT_CREATE));

    // delete the project files + dir
    File dir = new File(Eclim.getWorkspace() + "/" + TEST_PROJECT_CREATE);
    for(File f : dir.listFiles()){
      f.delete();
    }
    dir.delete();
  }

  @Test
  public void openCloseProject()
  {
    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));

    Eclim.execute(new String[]{
      "project_close", "-p", TEST_PROJECT});
    assertFalse("Project not closed.", projectOpen());

    Eclim.execute(new String[]{
      "project_open", "-p", TEST_PROJECT});
    assertTrue("Project not opened.", projectOpen());
  }

  @Test
  public void renameProject()
  {
    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));

    String renamed = TEST_PROJECT + "_renamed";
    Eclim.execute(new String[]{
      "project_rename", "-p", TEST_PROJECT, "-n", renamed});

    assertFalse("Previous project name still exists.",
        Eclim.projectExists(TEST_PROJECT));
    assertTrue("New project name doesn't exist.",
        Eclim.projectExists(renamed));

    Eclim.execute(new String[]{
      "project_rename", "-p", renamed, "-n", TEST_PROJECT});

    assertFalse("Previous project name still exists.",
        Eclim.projectExists(renamed));
    assertTrue("New project name doesn't exist.",
        Eclim.projectExists(TEST_PROJECT));
  }

  @Test
  public void moveProject()
    throws Exception
  {
    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT));

    String path = new File(Eclim.getWorkspace() + "/../")
      .getCanonicalPath().replace('\\', '/');
    Eclim.execute(new String[]{"project_move", "-p", TEST_PROJECT, "-d", path});

    assertTrue("Project does not exist.",
        Eclim.projectExists(TEST_PROJECT));
    assertEquals("Wrong project path",
        path + '/' + TEST_PROJECT,
        Eclim.getProjectPath(TEST_PROJECT));

    Eclim.execute(new String[]{
      "project_move", "-p", TEST_PROJECT, "-d", Eclim.getWorkspace()});

    assertTrue("Project does not exist.",
        Eclim.projectExists(TEST_PROJECT));
    path = new File(Eclim.getWorkspace() + '/' + TEST_PROJECT)
      .getCanonicalPath().replace('\\', '/');
    assertEquals("Wrong project path", path, Eclim.getProjectPath(TEST_PROJECT));
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void importProject()
  {
    // delete the test project if it exists
    if (Eclim.projectExists(TEST_PROJECT_IMPORT)){
      Eclim.execute(new String[]{"project_delete", "-p", TEST_PROJECT_IMPORT});
    }
    assertFalse("Project already exists.", Eclim.projectExists(TEST_PROJECT_IMPORT));

    // first create a project
    Eclim.execute(new String[]{
      "project_create",
      "-f", Eclim.getWorkspace() + "/" + TEST_PROJECT_IMPORT,
      "-n", "java",
    });

    assertTrue("Project not created.", Eclim.projectExists(TEST_PROJECT_IMPORT));

    // then delete it
    Eclim.execute(new String[]{"project_delete", "-p", TEST_PROJECT_IMPORT});

    assertFalse("Project not deleted.", Eclim.projectExists(TEST_PROJECT_IMPORT));

    // now import it
    Eclim.execute(new String[]{
      "project_import",
      "-f", Eclim.getWorkspace() + "/" + TEST_PROJECT_IMPORT,
    });

    assertTrue("Project not imported.", Eclim.projectExists(TEST_PROJECT_IMPORT));

    List<Map<String, Object>> results = (List<Map<String, Object>>)
      Eclim.execute(new String[]{
        "project_natures", "-p", TEST_PROJECT_IMPORT,
      });
    assertEquals(1, results.size());

    List natures = (List)results.get(0).get("natures");

    assertEquals(1, natures.size());
    assertEquals("java", natures.get(0));

    // delete the project and the project files + dir
    Eclim.execute(new String[]{"project_delete", "-p", TEST_PROJECT_IMPORT});
    File dir = new File(Eclim.getWorkspace() + "/" + TEST_PROJECT_IMPORT);
    for(File f : dir.listFiles()){
      f.delete();
    }
    dir.delete();
  }

  /**
   * Determines if the unit test project is open.
   *
   * @return true if the project is open, false otherwise.
   */
  @SuppressWarnings("unchecked")
  private boolean projectOpen()
  {
    List<Map<String, Object>> projects = (List<Map<String, Object>>)
      Eclim.execute(new String[]{"project_list"});

    for (Map<String, Object> project : projects){
      if (project.get("name").equals(TEST_PROJECT) &&
          project.get("open").equals(Boolean.TRUE))
      {
        return true;
      }
    }
    return false;
  }
}
