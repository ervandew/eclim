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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclim.http.EclimHTTPResponse;

import com.google.gson.Gson;

/**
 * Client to call eclim over the HTTP Interface.
 *
 * @author Lukas Roth
 *
 */
public class EclimHTTPClient
{

  private static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
  private static final String PORT = System.getProperty("http.server.port");
  private static final String HOST = System.getProperty("http.server.host", "localhost");
  private static String eclimAddress = "http://" + HOST + ":" + PORT + "/eclim/command/";

  /**
   * Calls eclim over a HTTP POST request with the parameters specified in the
   * argument map <code>parameters</code>.
   *
   * Example parameters: parameters = {"command":"projects"}
   *
   * @param parameters
   *          The eclim parameters
   * @return EcimHTTPResponse The response of eclim
   * @throws IOException
   */
  public EclimHTTPResponse post(Map<String, String> parameters)
      throws IOException
  {
    byte[] postData = urlEncodeUTF8(parameters).getBytes(StandardCharsets.UTF_8);
    String unparsedResponse = post(postData, getEclimAddress(), APPLICATION_FORM);
    return (new Gson()).fromJson(unparsedResponse, EclimHTTPResponse.class);
  }

  /**
   * Calls eclim over a HTTP POST request with the parameters specified in the
   * argument map <code>parameters</code>. The file will be posted to the body
   * while the parameters will passed as query parameters.
   *
   * Example parameters: InputStream file = new FileInputStream("my.jar");
   * parameters = {"command":"jar_upload", "p":"exampleProject",
   * "f","lib/my.jar"}
   * post(parameters, file);
   *
   * @param parameters
   *          The eclim parameters.
   * @param file
   *          The content of the inputstream inside the <code>file</code>
   *          parameter will be in the body of the request to eclim.
   * @param conatentType
   *          The content type of your post request.
   * @return EcimHTTPResponse The response of eclim
   * @throws IOException
   */
  public EclimHTTPResponse post(Map<String, String> parameters, InputStream file, String contentType)
      throws IOException
  {
    byte[] postData = toByteArray(file);
    String unparsedResponse = post(postData, getEclimAddress(parameters),
        contentType);
    return (new Gson()).fromJson(unparsedResponse, EclimHTTPResponse.class);
  }

  /**
   * Calls eclim over a HTTP GET request with the parameters specified in the
   * argument map <code>parameters</code>.
   *
   * Example parameters: parameters = {"command":"projects"}
   *
   * @param parameters
   * @return EcimHTTPResponse The response of eclim
   * @throws IOException
   */
  public EclimHTTPResponse get(Map<String, String> parameters)
      throws IOException
  {
    String unparsedResponse = get(getEclimAddress(parameters));
    return (new Gson()).fromJson(unparsedResponse, EclimHTTPResponse.class);
  }

  protected String post(byte[] postData, String request, String contentType)
      throws IOException
  {
    int postDataLength = postData.length;
    URL url = new URL(request);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setInstanceFollowRedirects(false);
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", contentType);
    connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
    connection.setUseCaches(false);

    DataOutputStream wr = null;
    try {
      wr = new DataOutputStream(connection.getOutputStream());
      wr.write(postData);
    } finally {
      wr.close();
    }
    
    InputStream result;
    if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
      result = connection.getInputStream();
    }else{
      result = connection.getErrorStream();
    }
    return convertStreamToString(result);
  }

  private String get(String requestAddress)
      throws IOException
  {
    URL url = new URL(requestAddress);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    InputStream result;
    if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
      result = connection.getInputStream();
    }else{
      result = connection.getErrorStream();
    }
    return convertStreamToString(result);  }

  private String getEclimAddress(Map<String, String> parameters)
  {
    return getEclimAddress() + "?" + urlEncodeUTF8(parameters);
  }

  public static String getEclimAddress()
  {
    return eclimAddress;
  }

  public static void setEclimAddress(String eclimAddress)
  {
    EclimHTTPClient.eclimAddress = eclimAddress;
  }

  // copied from
  // http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
  private static String convertStreamToString(java.io.InputStream is)
  {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  // copied from
  // http://stackoverflow.com/questions/1264709/convert-inputstream-to-byte-array-in-java
  private static byte[] toByteArray(InputStream inputStream)
      throws IOException
  {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[16384];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }
    buffer.flush();
    return buffer.toByteArray();
  }

  // copied from
  // http://stackoverflow.com/questions/2809877/how-to-convert-map-to-url-query-string
  private static String urlEncodeUTF8(String s)
  {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  // copied from
  // http://stackoverflow.com/questions/2809877/how-to-convert-map-to-url-query-string
  private static String urlEncodeUTF8(Map<?, ?> map)
  {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (sb.length() > 0) {
        sb.append("&");
      }
      sb.append(String.format("%s=%s", urlEncodeUTF8(entry.getKey().toString()),
          urlEncodeUTF8(entry.getValue().toString())));
    }
    return sb.toString();
  }
}
