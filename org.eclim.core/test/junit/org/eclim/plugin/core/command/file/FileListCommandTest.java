/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclim.Eclim;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test tests the file_list command.
 *
 * @author Lukas Roth
 *
 */
@SuppressWarnings("unchecked")
public class FileListCommandTest
{
  private static final String BASE_FOLDER = "fileListCommandTest_temp/";
  private static final String TEST_FILE_1_PATH = BASE_FOLDER
      + "my/example/folder/testFile1.txt";
  private static final String TEST_FILE_2_PATH = BASE_FOLDER + "testFile2.txt";
  private static final String TEST_CONTENT = "some content";
  private static final String NOT_EXISTING_PROJECT = "someNotExistingProject";
  private static final String ERROR_PROJECT_DOES_NOT_EXIST = "Could not get absolute path of the project '"
      + NOT_EXISTING_PROJECT + "'.";
  private static final String ILLEGAL_PATH = "asdf/../asdfw";
  private static final String ERROR_ILLEGAL_PATH = "Illegal path '" + ILLEGAL_PATH
      + "'.";
  private static final String NOT_EXISTING_FILE_PATH = "egrgg97z2hg9s9gh9z20w0gu02/";

  @Before
  public void setup()
  {
    setupExampleFiles();
  }

  @After
  public void cleanUp()
  {
    Eclim.execute(new String[] { "file_delete", "-p", Eclim.TEST_PROJECT, "-f",
        BASE_FOLDER });
    assertTrue("File deleted",
        !(new File(Eclim.getProjectPath(Eclim.TEST_PROJECT) + "/" + BASE_FOLDER))
            .exists());
  }

  private void setupExampleFiles()
  {
    saveExampleFile(TEST_FILE_1_PATH);
    saveExampleFile(TEST_FILE_2_PATH);
  }

  private void saveExampleFile(String path)
  {
    String createResult = (String) Eclim.execute(new String[] { "file_save", "-p",
        Eclim.TEST_PROJECT, "-f", path, "-c", TEST_CONTENT });
    assertEquals("File saved at location '" + path + "'.", createResult);
  }

  @Test
  public void testNormal()
  {
    List<String> fileListResult = (List<String>) Eclim.execute(
        new String[] { "file_list", "-p", Eclim.TEST_PROJECT, "-f", BASE_FOLDER });
    List<String> expectedResult = new ArrayList<String>();
    expectedResult.add("my/");
    expectedResult.add("testFile2.txt");
    Assert.assertEquals(fileListResult, expectedResult);
  }

  @Test
  public void testRecursive()
  {
    List<String> fileListResult = (List<String>) Eclim.execute(new String[] {
        "file_list", "-p", Eclim.TEST_PROJECT, "-f", BASE_FOLDER, "-r", "true" });
    List<String> expectedResult = new ArrayList<String>();
    expectedResult.add("my/");
    expectedResult.add("my/example/");
    expectedResult.add("my/example/folder/");
    expectedResult.add("my/example/folder/testFile1.txt");
    expectedResult.add("testFile2.txt");
    Assert.assertEquals(fileListResult, expectedResult);
  }

  @Test
  public void notExistingProject()
  {
    Map<String, String> createResult = (Map<String, String>) Eclim
        .execute(new String[] { "file_list", "-p", NOT_EXISTING_PROJECT, "-f",
            NOT_EXISTING_FILE_PATH });
    assertEquals(ERROR_PROJECT_DOES_NOT_EXIST, createResult.get("message"));
  }

  @Test
  public void illegalPath()
  {
    Map<String, String> createResult = (Map<String, String>) Eclim.execute(
        new String[] { "file_list", "-p", Eclim.TEST_PROJECT, "-f", ILLEGAL_PATH });
    assertEquals(ERROR_ILLEGAL_PATH, createResult.get("message"));
  }

  @Test
  public void nonExistingPath()
  {
    Map<String, String> createResult = (Map<String, String>) Eclim
        .execute(new String[] { "file_list", "-p", Eclim.TEST_PROJECT, "-f",
            NOT_EXISTING_FILE_PATH });
    assertEquals("No file at '" + NOT_EXISTING_FILE_PATH + "' in project '"
        + Eclim.TEST_PROJECT + "'.", createResult.get("message"));
  }
}