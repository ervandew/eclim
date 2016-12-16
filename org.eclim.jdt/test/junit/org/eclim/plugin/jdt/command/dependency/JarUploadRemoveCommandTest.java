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
package org.eclim.plugin.jdt.command.dependency;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.Eclim;
import org.eclim.EclimHTTPClient;
import org.eclim.http.EclimHTTPResponse;
import org.eclim.plugin.jdt.Jdt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonParser;

import junit.framework.Assert;

/**
 * This test we upload a dependency jar, manipulate the classpat file such that
 * eclipse knows we uploaded a jar and then remove the dependency jar again.
 *
 * Test idea: Upload and remove a GSON Library and check that - before the
 * upload Gson is not there - after the upload Gson is there - after the remove
 * Gson is not there again
 *
 * We check if the Gson library is correctly uploaded with the help of the
 * completion command. --> if we get a completion for GsonB (-> GsonBuilder) we
 * know that the dependency was added correctly.
 *
 * @author Lukas Roth
 */
public class JarUploadRemoveCommandTest
{
  private static final String TEST_FILE = "src/org/eclim/test/exampleClass.java";
  private static final String TEST_FILE_CONTENT = "package org.eclim.test;\nclass A{\nvoid f(){\nGsonB}\n}";
  private static final int TEST_FILE_OFFSET = 48;
  private static final String PATH_TO_DEPENDENCY = "lib/gson-1.7.1.jar";
  private static final String DEPENDENCY_LOCATION_ON_FILE_SYSTEM = "org.eclim/lib/gson-1.7.1.jar";
  private EclimHTTPClient eclimHTTPClient = new EclimHTTPClient();

  @Before
  public void setupTestFile()
      throws IOException
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));
    setupCompletionFile();
  }

  @After
  public void deleteTestFile()
      throws IOException
  {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("command", "file_delete");
    parameters.put("p", Jdt.TEST_PROJECT);
    parameters.put("f", TEST_FILE);
    eclimHTTPClient.get(parameters);

    updateProject();
  }

  private void setupCompletionFile()
      throws IOException
  {
    createTestFile();
    updateProject();
  }

  private void updateProject()
      throws IOException
  {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("command", "project_update");
    parameters.put("p", Jdt.TEST_PROJECT);
    eclimHTTPClient.get(parameters);
  }

  private void createTestFile()
      throws IOException
  {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("command", "file_save");
    parameters.put("p", Jdt.TEST_PROJECT);
    parameters.put("f", TEST_FILE);
    parameters.put("c", TEST_FILE_CONTENT);
    eclimHTTPClient.get(parameters);
  }

  @Test
  public void jarUploadRemoveTest()
      throws IOException
  {
    assertLibraryNotAvailable();
    uploadJar();
    updateProject();
    assertLibraryAvailable();
    removeJar();
    updateProject();
    assertLibraryNotAvailable();
  }

  private void removeJar()
      throws IOException
  {
    deleteJar();
    removeFromClasspath();
  }

  private void removeFromClasspath()
      throws IOException
  {
    Map<String, String> uploadJarParams = new HashMap<String, String>();
    uploadJarParams.put("command", "remove_dependency");
    uploadJarParams.put("p", Jdt.TEST_PROJECT);
    uploadJarParams.put("f", PATH_TO_DEPENDENCY);
    eclimHTTPClient.post(uploadJarParams);
  }

  private void deleteJar()
      throws IOException
  {
    Map<String, String> saveJarParams = new HashMap<String, String>();
    saveJarParams.put("command", "file_delete");
    saveJarParams.put("p", Jdt.TEST_PROJECT);
    saveJarParams.put("f", PATH_TO_DEPENDENCY);
    eclimHTTPClient.post(saveJarParams);
  }

  private void uploadJar()
      throws IOException
  {
    saveJar();
    addToClasspath();
  }

  private void addToClasspath()
      throws IOException
  {
    Map<String, String> uploadJarParams = new HashMap<String, String>();
    uploadJarParams.put("command", "add_dependency");
    uploadJarParams.put("p", Jdt.TEST_PROJECT);
    uploadJarParams.put("f", PATH_TO_DEPENDENCY);
    eclimHTTPClient.post(uploadJarParams);
  }

  private void saveJar()
      throws IOException
  {
    Map<String, String> saveJarParams = new HashMap<String, String>();
    saveJarParams.put("command", "file_save");
    saveJarParams.put("p", Jdt.TEST_PROJECT);
    saveJarParams.put("f", PATH_TO_DEPENDENCY);
    InputStream file = new FileInputStream(DEPENDENCY_LOCATION_ON_FILE_SYSTEM);
    eclimHTTPClient.post(saveJarParams, file, "application/jar");
  }

  private void assertLibraryNotAvailable()
      throws IOException
  {
    List<Map<String, Object>> completions = getCompletions(TEST_FILE_OFFSET);
    Assert.assertEquals("No completion should be found for the prefix 'GsonB'", 0,
        completions.size());
  }

  private void assertLibraryAvailable()
      throws IOException
  {
    List<Map<String, Object>> completions = getCompletions(TEST_FILE_OFFSET);
    Assert.assertEquals("GsonBuilder",
        (String) completions.get(0).get("completion"));
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getCompletions(int offset)
      throws IOException
  {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("command", "java_complete");
    parameters.put("p", Jdt.TEST_PROJECT);
    parameters.put("f", TEST_FILE);
    parameters.put("o", Integer.toString(offset));
    parameters.put("e", "utf-8");
    parameters.put("l", "standard");
    EclimHTTPResponse response = eclimHTTPClient.post(parameters);
    Map<String, Object> parsedResponse = (Map<String, Object>) Eclim
        .toType((new JsonParser()).parse(response.getResult()));
    return (List<Map<String, Object>>) parsedResponse.get("completions");
  }
}
