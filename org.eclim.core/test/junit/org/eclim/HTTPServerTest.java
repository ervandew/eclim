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
package org.eclim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclim.http.EclimHTTPResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the HTTP Interface of eclim.
 *
 * @author Lukas Roth
 *
 */
public class HTTPServerTest extends EclimHTTPClient
{
  private String eclimAddress = EclimHTTPClient.getEclimAddress();
  private EclimHTTPClient eclimHTTPClient = new EclimHTTPClient();

  @Test
  public void getExpected()
      throws IOException
  {
    Map<String, String> projectsParams = new HashMap<String, String>();
    projectsParams.put("command", "projects");
    EclimHTTPClient eclimHTTPClient = new EclimHTTPClient();
    EclimHTTPResponse result = eclimHTTPClient.get(projectsParams);
    assertProjectsExist(result);
  }

  @Test
  public void postExpected()
      throws IOException
  {
    Map<String, String> projectsParams = new HashMap<String, String>();
    projectsParams.put("command", "projects");
    EclimHTTPClient eclimHTTPClient = new EclimHTTPClient();
    EclimHTTPResponse result = eclimHTTPClient.post(projectsParams);
    assertProjectsExist(result);
  }

  private void assertProjectsExist(EclimHTTPResponse result)
  {
    Assert.assertTrue(
        result.getResult().contains("\"name\":\"RemoteSystemsTempFiles\""));
    Assert.assertTrue(result.getResult().contains("\"name\":\"eclim_unit_test\""));
    Assert.assertTrue(
        result.getResult().contains("\"name\":\"eclim_unit_test_java\""));
  }

  @Test
  public void unsupportedMethod()
      throws IOException
  {
    URL url = new URL(eclimAddress + "?command=projects");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("DELETE");
    connection.connect();
    Assert.assertEquals(405, connection.getResponseCode());
  }

  @Test
  public void doubleArgumentKey()
      throws IOException
  {
    String requestAddress = EclimHTTPClient.getEclimAddress()
        + "?command=a&command=a";
    URL url = new URL(requestAddress);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    InputStream result;
    if (200 <= connection.getResponseCode()
        && connection.getResponseCode() <= 299) {
      result = connection.getInputStream();
    } else {
      result = connection.getErrorStream();
    }

    String stringResult = convertStreamToString(result);
    Assert.assertTrue(stringResult.contains("Each argument needs an unique key."));
  }
  
  // copied from
  // http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
  private static String convertStreamToString(java.io.InputStream is)
  {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  @Test
  public void postFile()
      throws IOException
  {
    String testFilePath = "testFile.txt";
    File testFile = new File(
        Eclim.getProjectPath(Eclim.TEST_PROJECT) + "/" + testFilePath);

    Map<String, String> saveStreamParams = new HashMap<String, String>();
    saveStreamParams.put("command", "file_save");
    saveStreamParams.put("p", Eclim.TEST_PROJECT);
    saveStreamParams.put("f", testFilePath);
    InputStream file = new ByteArrayInputStream(
        "Test Content".getBytes(StandardCharsets.UTF_8));
    EclimHTTPClient eclimHTTPClient = new EclimHTTPClient();
    eclimHTTPClient.post(saveStreamParams, file, "application/txt");

    deleteFile(testFilePath, testFile);
  }

  private void deleteFile(String testFilePath, File testFile)
  {
    String deleteResult = (String) Eclim.execute(new String[] { "file_delete", "-p",
        Eclim.TEST_PROJECT, "-f", testFilePath });
    assertEquals("File '" + testFilePath + "' deleted.", deleteResult);
    assertTrue("File deleted", !testFile.exists());
  }

  @Test
  public void postFileNotAllowedParameterKey()
      throws IOException
  {
    Map<String, String> saveStreamParams = new HashMap<String, String>();
    saveStreamParams.put("command", "file_save");
    saveStreamParams.put("s", "something");
    InputStream file = new ByteArrayInputStream(
        "Test Content".getBytes(StandardCharsets.UTF_8));
    EclimHTTPClient eclimHTTPClient = new EclimHTTPClient();
    EclimHTTPResponse result = eclimHTTPClient.post(saveStreamParams, file,
        "application/txt");
    assertCorrectStatusCode(result, 400);
    Assert.assertTrue(
        result.getResult().contains("Could not create a command line out of"));
  }

  @Test
  public void postFileNotAllowedParameter()
      throws IOException
  {
    Map<String, String> saveStreamParams = new HashMap<String, String>();
    saveStreamParams.put("command", "file_save");
    // The 'c' command key is not allowed in a file post
    saveStreamParams.put("c", "something");
    InputStream file = new ByteArrayInputStream(
        "Test content".getBytes(StandardCharsets.UTF_8));
    EclimHTTPClient eclimHTTPClient = new EclimHTTPClient();
    EclimHTTPResponse result = eclimHTTPClient.post(saveStreamParams, file,
        "application/txt");
    assertCorrectStatusCode(result, 400);
    Assert.assertTrue(result.getResult().contains(
        "key is not allowed in a command which passes a file in the body of the request."));
  }

  @Test
  public void commandException()
      throws IOException
  {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("asdfasdf", "");
    EclimHTTPResponse response = eclimHTTPClient.get(parameters);
    Assert.assertTrue(
        response.getResult().contains("Could not create a command line out of"));
  }

  private void assertCorrectStatusCode(EclimHTTPResponse result, int expected)
  {
    Assert.assertEquals("Correct error code", expected, result.getStatusCode());
  }
}
