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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclim.Eclim;
import org.eclim.EclimHTTPClient;
import org.junit.Test;

/**
 * The class {@code FileSaveDeleteCommandTest} tests the file_save and
 * file_delete commands of eclim.
 *
 * It uses the HTTP Interface of eclim to test the commands.
 *
 * For the file save it tests both, the stream parameter over the HTTP Interface
 * and the string parameter.
 *
 * @author Lukas Roth
 *
 */
public class FileSaveDeleteCommandTest
{
  private static final String TEST_FILE_PATH = "testFile.txt";
  private static final File TEST_FILE = new File(
      Eclim.getProjectPath(Eclim.TEST_PROJECT) + "/" + TEST_FILE_PATH);
  private static final String TEST_FOLDER = "/testDir/";
  private static final String TEST_CONTENT = "some content";
  private static final String NOT_EXISTING_PROJECT = "someNotExistingProject";
  private static final String WRONG_PATH = "some/path/containing/../bla/bli";
  private static final String ERROR_PROJECT_DOES_NOT_EXIST = "Project '"
      + NOT_EXISTING_PROJECT + "' does not exist.";
  private static final String ERROR_ILLEGAL_PATH = "Illegal path '" + WRONG_PATH
      + "'.";

  @Test
  public void saveString()
      throws Exception
  {
    saveStringFile();
    assertFileIsThere();
    deleteFile();
  }

  private void saveStringFile()
  {
    String createResult = (String) Eclim.execute(new String[] { "file_save", "-p",
        Eclim.TEST_PROJECT, "-f", TEST_FILE_PATH, "-c", TEST_CONTENT });
    assertEquals("File saved at location '" + TEST_FILE_PATH + "'.", createResult);
  }

  @Test
  public void saveStream()
      throws IOException
  {
    saveStreamFile();
    assertFileIsThere();
    deleteFile();
  }

  private void saveStreamFile()
      throws IOException
  {
    Map<String, String> saveStreamParams = new HashMap<String, String>();
    saveStreamParams.put("command", "file_save");
    saveStreamParams.put("p", Eclim.TEST_PROJECT);
    saveStreamParams.put("f", TEST_FILE_PATH);
    InputStream file = new ByteArrayInputStream(
        TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
    EclimHTTPClient eclimHTTPClient = new EclimHTTPClient();
    eclimHTTPClient.post(saveStreamParams, file, "application/txt");
  }

  private void assertFileIsThere()
      throws IOException
  {
    assertTrue("File exists", TEST_FILE.exists());
    assertEquals("Right content", readFile(TEST_FILE.getPath()), TEST_CONTENT);
  }

  private void deleteFile()
  {
    String deleteResult = (String) Eclim.execute(new String[] { "file_delete", "-p",
        Eclim.TEST_PROJECT, "-f", TEST_FILE_PATH });
    assertEquals("File '" + TEST_FILE_PATH + "' deleted.", deleteResult);
    assertTrue("File deleted", !TEST_FILE.exists());
  }

  @Test
  public void wrongPathSave()
  {
    Map<String, String> createResult = (Map<String, String>) Eclim
        .execute(new String[] { "file_save", "-p", Eclim.TEST_PROJECT, "-f",
            WRONG_PATH, "-c", TEST_CONTENT });
    assertEquals(ERROR_ILLEGAL_PATH, createResult.get("message"));
  }

  @Test
  public void wrongPathDelete()
  {
    Map<String, String> createResult = (Map<String, String>) Eclim.execute(
        new String[] { "file_delete", "-p", Eclim.TEST_PROJECT, "-f", WRONG_PATH });
    assertEquals(ERROR_ILLEGAL_PATH, createResult.get("message"));
  }

  @Test
  public void notExistingProjectSave()
  {
    Map<String, String> createResult = (Map<String, String>) Eclim
        .execute(new String[] { "file_save", "-p", NOT_EXISTING_PROJECT, "-f",
            TEST_FILE_PATH, "-c", TEST_CONTENT });
    assertEquals(ERROR_PROJECT_DOES_NOT_EXIST, createResult.get("message"));
  }

  @Test
  public void notExistingProjectDelete()
  {
    Map<String, String> createResult = (Map<String, String>) Eclim
        .execute(new String[] { "file_delete", "-p", NOT_EXISTING_PROJECT, "-f",
            TEST_FILE_PATH });
    assertEquals(ERROR_PROJECT_DOES_NOT_EXIST, createResult.get("message"));
  }

  @Test
  public void deleteFolder()
  {
    File f = new File(Eclim.getProjectPath(Eclim.TEST_PROJECT) + "/" + TEST_FOLDER);
    f.mkdirs();
    String createResult = (String) Eclim.execute(new String[] { "file_delete", "-p",
        Eclim.TEST_PROJECT, "-f", TEST_FOLDER });
    assertEquals("Folder '" + TEST_FOLDER + "' deleted.", createResult);
    assertTrue("Folder deleted", !f.exists());
  }

  private static String readFile(String path)
      throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, Charset.defaultCharset());
  }
}
