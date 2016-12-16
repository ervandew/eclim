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
package org.eclim.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.Services;
import org.eclim.logging.Logger;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * The class {@code HTTPServer} provides the HTTP Interface of eclim. The class
 * {@code HTTPServer} handles all requests which come over the HTTP Interface
 * (and not over the NailGun interface).
 *
 * Look at the {@code org.eclim.EclimHTTPClient} inside the test cases for a
 * reference on how to use the HTTP Interface of eclim.
 *
 * @author Lukas Roth
 *
 */
public class HTTPServer implements HttpHandler
{
  private static final Logger logger = Logger.getLogger(HTTPServer.class);
  private HttpServer server;
  private boolean serverIsRunning = false;

  private static final String POST = "POST";
  private static final String GET = "GET";
  private static final String CONTENT_TYPE = "Content-type";
  private static final String CONTENT_TYPE_FORM =
      "application/x-www-form-urlencoded";
  private static final String CONTENT_TYPE_APPLICATION_JSON =
      "application/json";
  private static final String ENCODING = "UTF-8";

  /**
   * Starts the {@code HTTPServer} at the location specified in the arguments.
   *
   * @param hostname
   * @param port
   * @throws IOException
   */
  public void start(String hostname, int port)
      throws IOException
  {
    if (serverIsRunning) {
      server.stop(0);
    }
    InetSocketAddress address = new InetSocketAddress(hostname, port);
    server = HttpServer.create(address, 0);
    server.createContext("/eclim/command", this);
    server.setExecutor(null); // creates a default executor
    server.start();
    serverIsRunning = true;
    logger.info("Started HTTP server on: " + address.toString());
  }

  /**
   * Stops the {@code HTTPServer} if he is running. If it is already stopped
   * nothing is done.
   */
  public void stop()
  {
    if (serverIsRunning && server != null) {
      server.stop(0);
      logger.info("Stopped HTTP server");
    }
  }

  @Override
  public void handle(HttpExchange httpExchange)
      throws IOException
  {
    EclimHTTPResponse eclimResponse = null;
    String requestMethod = httpExchange.getRequestMethod();
    try {
      if (requestMethod.equals(POST)) {
        eclimResponse = handlePost(httpExchange);
      } else if (requestMethod.equals(GET)) {
        eclimResponse = handleGet(httpExchange);
      } else {
        throw new HTTPServerException(
            Services.getMessage("server.error.wrong.method"), 405);
      }
    } catch (HTTPServerException e) {
      logger.error("Error while processing the http request (http status code: " +
          e.getStatusCode() + ", error message: " + e.getMessage() + ")", e);
      eclimResponse = createHTTPResponse(e);
    }

    sendResponse(httpExchange, eclimResponse);
  }

  private EclimHTTPResponse createHTTPResponse(HTTPServerException e)
      throws IOException
  {
    String exceptionMessage = createExceptionMessage(e);
    return new EclimHTTPResponse(exceptionMessage, "", "", e.getStatusCode());
  }

  private void sendResponse(HttpExchange httpExchange,
      EclimHTTPResponse eclimResponse)
      throws IOException
  {
    int statusCode = eclimResponse.getStatusCode();
    String body = (new Gson()).toJson(eclimResponse);
    httpExchange.getResponseHeaders().add("Content-Type",
        CONTENT_TYPE_APPLICATION_JSON);
    httpExchange.sendResponseHeaders(statusCode, body.length());
    OutputStream os = httpExchange.getResponseBody();
    os.write(body.getBytes());
    os.close();
  }

  private String createExceptionMessage(HTTPServerException e)
  {
    return (new Gson()).toJson(new ExceptionResponse(e));
  }

  class ExceptionResponse
  {
    private final String exception; // NOSONAR used to generate JSON response

    public ExceptionResponse(Exception e)
    {
      StringWriter buf = new StringWriter();
      e.printStackTrace(new PrintWriter(buf));
      this.exception = buf.toString();
    }
  }

  private EclimHTTPResponse handleGet(HttpExchange httpExchange)
      throws HTTPServerException
  {
    return callCommand(parseParameters(httpExchange.getRequestURI().getQuery()));
  }

  private EclimHTTPResponse handlePost(HttpExchange httpExchange)
      throws HTTPServerException
  {
    Headers requestHeaders = httpExchange.getRequestHeaders();
    if (requestHeaders.containsKey(CONTENT_TYPE)) {
      List<String> contentType = requestHeaders.get(CONTENT_TYPE);
      if (contentType.contains(CONTENT_TYPE_FORM)) {
        return handlePostForm(httpExchange);
      } else {
        return handlePostFileUpload(httpExchange);
      }
    } else {
      throw new HTTPServerException("server.error.content.type.not.set", 415);
    }
  }

  private EclimHTTPResponse handlePostFileUpload(HttpExchange httpExchange)
      throws HTTPServerException
  {
    Map<String, String> params = parseParameters(
        httpExchange.getRequestURI().getQuery());
    InputStream fileInputStream = httpExchange.getRequestBody();
    return callCommand(params, fileInputStream);
  }

  private EclimHTTPResponse handlePostForm(HttpExchange httpExchange)
      throws HTTPServerException
  {
    InputStream inputStream = null;
    try {
      inputStream = httpExchange.getRequestBody();
      String inString = convertStreamToString(inputStream);
      Map<String, String> params = parseParameters(inString);
      return callCommand(params);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          logger.warn("Could not close the request body input stream", e);
        }
      }
    }
  }

  private EclimHTTPResponse callCommand(Map<String, String> parameters)
      throws HTTPServerException
  {
    try {
      CommandCaller eclimCaller = new CommandCaller(parameters);
      return eclimCaller.callCommand();
    } catch (InvalidCommandException e) {
      throw new HTTPServerException(e, 400);
    } catch (Exception e) {
      throw new HTTPServerException(e, 500);
    }
  }

  private EclimHTTPResponse callCommand(Map<String, String> parameters,
      InputStream fileInputStream)
      throws HTTPServerException
  {
    try {
      CommandCaller eclimCaller = new CommandCaller(parameters, fileInputStream);
      return eclimCaller.callCommand();
    } catch (InvalidCommandException e) {
      throw new HTTPServerException(e, 400);
    } catch (Exception e) {
      throw new HTTPServerException(e, 500);
    }
  }

  static String convertStreamToString(java.io.InputStream is)
  {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  private static Map<String, String> parseParameters(String parameters)
      throws HTTPServerException
  {
    try {
      final Map<String, String> res = new HashMap<String, String>();
      final String[] pairs = parameters.split("&");
      for (String pair : pairs) {
        final int idx = pair.indexOf('=');
        final String key = idx > 0 ?
            URLDecoder.decode(pair.substring(0, idx), ENCODING) : pair;
        if (res.containsKey(key)) {
          throw new HTTPServerException(
              Services.getMessage("server.error.argument.key.not.unique"), 400);
        }
        final String value = idx > 0 && pair.length() > idx + 1 ?
            URLDecoder.decode(pair.substring(idx + 1), ENCODING) : null;
        if (value != null) {
          res.put(key, value);
        } else {
          res.put(key, "");
        }
      }
      return res;
    } catch (UnsupportedEncodingException e) {
      String errMsg = Services.getMessage("server.error.argument.decoding");
      logger.error(errMsg, e);
      throw new HTTPServerException(errMsg, 400);
    }
  }
}