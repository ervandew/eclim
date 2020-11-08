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
package org.eclim.plugin.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.SystemUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.Services;

import org.eclim.command.Error;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Some xml utility methods.
 *
 * @author Eric Van Dewoestine
 */
public class XmlUtils
{
  private static final Logger logger = Logger.getLogger(XmlUtils.class);

  private static final Pattern WIN_BUG = Pattern.compile("^/[a-zA-Z]:/.*");

  private static XPath XPATH;

  /**
   * Create an XPathExpression from the supplied xpath string.
   *
   * @param xpath The xpath string.
   * @return An XPathExpression.
   */
  public static XPathExpression createXPathExpression(String xpath)
  {
    try{
      if(XPATH == null){
        XPATH = XPathFactory.newInstance().newXPath();
      }
      return XPATH.compile(xpath);
    }catch(XPathExpressionException xpee){
      throw new RuntimeException(xpee);
    }
  }

  /**
   * Validate the supplied xml file.
   *
   * @param project The project name.
   * @param filename The file path to the xml file.
   * @return A possibly empty array of errors.
   */
  public static List<Error> validateXml(String project, String filename)
  {
    return validateXml(project, filename, false, null);
  }

  /**
   * Validate the supplied xml file.
   *
   * @param project The project name.
   * @param filename The file path to the xml file.
   * @param schema True to use schema validation relying on the
   * xsi:schemaLocation attribute of the document.
   * @return A possibly empty array of errors.
   */
  public static List<Error> validateXml(
      String project, String filename, boolean schema)
  {
    return validateXml(project, filename, schema, null);
  }

  /**
   * Validate the supplied xml file.
   *
   * @param project The project name.
   * @param filename The file path to the xml file.
   * @param schema True to use schema validation relying on the
   * xsi:schemaLocation attribute of the document.
   * @param handler The content handler to use while parsing the file.
   * @return A possibly empty list of errors.
   */
  public static List<Error> validateXml(
      String project,
      String filename,
      boolean schema,
      DefaultHandler handler)
  {
    try{
      filename = ProjectUtils.getFilePath(project, filename);
      filename = filename.replace('\\', '/');

      EntityResolver entityResolver = new EntityResolver(
          FileUtils.getFullPath(filename));

      // check if the file has doctype info (would be nice to have a way to
      // detect this without parsing the whole file).
      DocumentBuilder builder = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder();
      builder.setEntityResolver(entityResolver);
      Document doc = builder.parse(new File(filename));

      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(doc.getDoctype() != null);
      factory.setNamespaceAware(true);
      if(schema){
        factory.setFeature("http://apache.org/xml/features/validation/schema", true);
        factory.setFeature(
            "http://apache.org/xml/features/validation/schema-full-checking", true);
      }

      SAXParser parser = factory.newSAXParser();

      ErrorAggregator errorHandler = new ErrorAggregator(filename);
      parser.parse(
          new File(filename),
          getHandler(handler, errorHandler, entityResolver));

      return errorHandler.getErrors();
    }catch(SAXParseException spe){
      ArrayList<Error> errors = new ArrayList<Error>();
      errors.add(
        new Error(
            spe.getMessage(),
            filename,
            spe.getLineNumber(),
            spe.getColumnNumber(),
            false)
        );
      return errors;
    }catch(SAXException se){
      ArrayList<Error> errors = new ArrayList<Error>();
      errors.add(new Error(
            "SAXException: " + se.getMessage(), filename, 1, 1, false));
      return errors;
    }catch(ParserConfigurationException pce){
      ArrayList<Error> errors = new ArrayList<Error>();
      errors.add(new Error(
            "ParserConfigurationException: " + pce.getMessage(),
            filename, 1, 1, false));
      return errors;
    }catch(IOException ioe){
      ArrayList<Error> errors = new ArrayList<Error>();
      errors.add(
          new Error(
            "IOException: " + ioe.getMessage(),
            filename, 1, 1, false));
      return errors;
    }
  }

  /**
   * Validate the supplied xml file against the specified xsd.
   *
   * @param project The project name.
   * @param filename The file path to the xml file.
   * @param schema The file path to the xsd.
   * @return A possibly empty array of errors.
   */
  public static List<Error> validateXml(
      String project, String filename, String schema)
  {
    try{
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      factory.setFeature("http://apache.org/xml/features/validation/schema", true);
      factory.setFeature(
          "http://apache.org/xml/features/validation/schema-full-checking", true);

      SAXParser parser = factory.newSAXParser();
      parser.setProperty(
          "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
          "http://www.w3.org/2001/XMLSchema");
      if(!schema.startsWith("file:")){
        schema = "file://" + schema;
      }
      parser.setProperty(
          "http://java.sun.com/xml/jaxp/properties/schemaSource", schema);
      parser.setProperty(
          "http://apache.org/xml/properties/schema/" +
          "external-noNamespaceSchemaLocation",
          schema.replace('\\', '/'));

      filename = ProjectUtils.getFilePath(project, filename);
      filename = filename.replace('\\', '/');

      ErrorAggregator errorHandler = new ErrorAggregator(filename);
      EntityResolver entityResolver = new EntityResolver(
          FileUtils.getFullPath(filename));
      parser.parse(new File(filename),
          getHandler(null, errorHandler, entityResolver));
      return errorHandler.getErrors();
    }catch(SAXParseException spe){
      ArrayList<Error> errors = new ArrayList<Error>();
      errors.add(
        new Error(
            spe.getMessage(),
            filename,
            spe.getLineNumber(),
            spe.getColumnNumber(),
            false)
        );
      return errors;
    }catch(SAXException se){
      ArrayList<Error> errors = new ArrayList<Error>();
      errors.add(new Error(
            "SAXException: " + se.getMessage(), filename, 1, 1, false));
      return errors;
    }catch(ParserConfigurationException pce){
      ArrayList<Error> errors = new ArrayList<Error>();
      errors.add(new Error(
            "ParserConfigurationException: " + pce.getMessage(),
            filename, 1, 1, false));
      return errors;
    }catch(IOException ioe){
      ArrayList<Error> errors = new ArrayList<Error>();
      errors.add(
          new Error(
            "IOException: " + ioe.getMessage(),
            filename, 1, 1, false));
      return errors;
    }
  }

  /**
   * Gets the value of a named child element.
   *
   * @param element The parent element.
   * @param name The name of the child element to retrieve the value from.
   * @return The text value of the child element.
   */
  public static String getElementValue(Element element, String name)
  {
    return ((Element)element.getElementsByTagName(name).item(0))
      .getFirstChild().getNodeValue();
  }

  /**
   * Gets an aggregate handler which delegates accordingly to the supplied
   * handlers.
   *
   * @param handler Main DefaultHandler to delegate to (may be null).
   * @param errorHandler DefaultHandler to delegate errors to (may be null).
   * @param entityResolver EntityResolver to delegate to (may be null).
   * @return DefaultHandler instance.
   */
  private static DefaultHandler getHandler(
      DefaultHandler handler,
      DefaultHandler errorHandler,
      EntityResolver entityResolver)
  {
    DefaultHandler hdlr = handler != null ? handler : new DefaultHandler();
    return new AggregateHandler(hdlr, errorHandler, entityResolver);
  }

  /**
   * Aggregate DefaultHandler which delegates to other handlers.
   */
  private static class AggregateHandler
    extends DefaultHandler
  {
    private DefaultHandler handler;
    private DefaultHandler errorHandler;
    private org.xml.sax.EntityResolver entityResolver;

    /**
     * Constructs a new instance.
     *
     * @param handler The handler for this instance.
     * @param errorHandler The errorHandler for this instance.
     * @param entityResolver The entityResolver for this instance.
     */
    public AggregateHandler (
        DefaultHandler handler,
        DefaultHandler errorHandler,
        EntityResolver entityResolver)
    {
      this.handler = handler;
      this.errorHandler = errorHandler != null ? errorHandler : handler;
      this.entityResolver = entityResolver != null ? entityResolver : handler;
    }

    /**
     * @see DefaultHandler#resolveEntity(String,String)
     */
    public InputSource resolveEntity(String publicId, String systemId)
      throws IOException, SAXException
    {
      return entityResolver.resolveEntity(publicId, systemId);
    }

    /**
     * @see DefaultHandler#notationDecl(String,String,String)
     */
    public void notationDecl(String name, String publicId, String systemId)
      throws SAXException
    {
      handler.notationDecl(name, publicId, systemId);
    }

    /**
     * @see DefaultHandler#unparsedEntityDecl(String,String,String,String)
     */
    public void unparsedEntityDecl(
        String name, String publicId, String systemId, String notationName)
      throws SAXException
    {
      handler.unparsedEntityDecl(name, publicId, systemId, notationName);
    }

    /**
     * @see DefaultHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator)
    {
      handler.setDocumentLocator(locator);
    }

    /**
     * @see DefaultHandler#startDocument()
     */
    public void startDocument()
      throws SAXException
    {
      handler.startDocument();
    }

    /**
     * @see DefaultHandler#endDocument()
     */
    public void endDocument()
      throws SAXException
    {
      handler.endDocument();
    }

    /**
     * @see DefaultHandler#startPrefixMapping(String,String)
     */
    public void startPrefixMapping(String prefix, String uri)
      throws SAXException
    {
      handler.startPrefixMapping(prefix, uri);
    }

    /**
     * @see DefaultHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix)
      throws SAXException
    {
      handler.endPrefixMapping(prefix);
    }

    /**
     * @see DefaultHandler#startElement(String,String,String,Attributes)
     */
    public void startElement(
        String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      handler.startElement(uri, localName, qName, attributes);
    }

    /**
     * @see DefaultHandler#endElement(String,String,String)
     */
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      handler.endElement(uri, localName, qName);
    }

    /**
     * @see DefaultHandler#characters(char[],int,int)
     */
    public void characters(char[] ch, int start, int length)
      throws SAXException
    {
      handler.characters(ch, start, length);
    }

    /**
     * @see DefaultHandler#ignorableWhitespace(char[],int,int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException
    {
      handler.ignorableWhitespace(ch, start, length);
    }

    /**
     * @see DefaultHandler#processingInstruction(String,String)
     */
    public void processingInstruction(String target, String data)
      throws SAXException
    {
      handler.processingInstruction(target, data);
    }

    /**
     * @see DefaultHandler#skippedEntity(String)
     */
    public void skippedEntity(String name)
      throws SAXException
    {
      handler.skippedEntity(name);
    }

    /**
     * @see DefaultHandler#warning(SAXParseException)
     */
    public void warning(SAXParseException e)
      throws SAXException
    {
      errorHandler.warning(e);
    }

    /**
     * @see DefaultHandler#error(SAXParseException)
     */
    public void error(SAXParseException e)
      throws SAXException
    {
      errorHandler.error(e);
    }

    /**
     * @see DefaultHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException e)
      throws SAXException
    {
      errorHandler.fatalError(e);
    }
  }

  /**
   * Handler for collecting errors during parsing and validation of a xml
   * file.
   */
  private static class ErrorAggregator
    extends DefaultHandler
  {
    private ArrayList<Error> errors = new ArrayList<Error>();
    private String filename;

    /**
     * Constructs a new instance.
     *
     * @param filename The filename for this instance.
     */
    public ErrorAggregator (String filename)
    {
      this.filename = filename;
    }

    @Override
    public void warning(SAXParseException ex)
      throws SAXException
    {
      addError(ex, true);
    }

    @Override
    public void error(SAXParseException ex)
      throws SAXException
    {
      addError(ex, false);
    }

    @Override
    public void fatalError(SAXParseException ex)
      throws SAXException
    {
      addError(ex, false);
    }

    /**
     * Adds the supplied SAXException as an Error.
     *
     * @param ex The SAXException.
     */
    private void addError(SAXParseException ex, boolean warning)
    {
      String location = ex.getSystemId();
      if(location != null){
        if(location.startsWith("file://")){
          location = location.substring("file://".length());
        }else if(location.startsWith("file:")){
          location = location.substring("file:".length());
        }
      }
      // bug where window paths start with /C:/...
      if(location != null && WIN_BUG.matcher(location).matches()){
        location = location.substring(1);
      }
      if(location == null){
        location = filename;
      }
      try{
        errors.add(new Error(
              ex.getMessage(),
              URLDecoder.decode(location, "utf-8"),
              ex.getLineNumber(),
              ex.getColumnNumber(),
              warning));
      }catch(Exception e){
        throw new RuntimeException(e);
      }
    }

    /**
     * Gets the possibly empty array of errors.
     *
     * @return Array of Error.
     */
    public List<Error> getErrors()
    {
      return errors;
    }
  }

  /**
   * EntityResolver extension.
   */
  private static class EntityResolver
    implements org.xml.sax.EntityResolver
  {
    private static String TEMP_PREFIX =
      "file://" + SystemUtils.JAVA_IO_TMPDIR.replace('\\', '/');
    static{
      if(TEMP_PREFIX.endsWith("\\") || TEMP_PREFIX.endsWith("/")){
        TEMP_PREFIX = TEMP_PREFIX.substring(0, TEMP_PREFIX.length() - 1);
      }
    }

    private String path;
    private String lastPath;

    // cache missing sources to avoiding hitting the same url repeatedly
    private static HashMap<String, IOException> missingSources =
      new HashMap<String, IOException>();

    /**
     * Constructs a new instance.
     *
     * @param path The path for all relative entities to be relative to.
     */
    public EntityResolver (String path)
    {
      this.path = path;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException, IOException
    {
      String location = systemId;
      // rolling the dice to fix windows issue where parser, or something, is
      // turning C:/... into C/.  This would cause problems if someone acutally
      // has a single letter directory, but that is doubtful.
      location = location.replaceFirst("^file://([a-zA-Z])/", "file://$1:/");

      if(location.startsWith(TEMP_PREFIX)){
        location = location.substring(TEMP_PREFIX.length());
        return resolveEntity(publicId, lastPath + location);
      }else if(location.startsWith("http://")){
        if (missingSources.containsKey(systemId)){
          throw missingSources.get(systemId);
        }

        int index = location.indexOf('/', 8);
        lastPath = location.substring(0, index + 1);
        location = location.substring(index);

        location = TEMP_PREFIX + location;

        FileSystemManager fsManager = VFS.getManager();
        FileObject tempFile = fsManager.resolveFile(location.replace("%", "%25"));

        // check if temp file already exists.
        if(!tempFile.exists() || tempFile.getContent().getSize() == 0){
          InputStream in = null;
          OutputStream out = null;
          try{
            if(!tempFile.exists()){
              tempFile.createFile();
            }

            // download and save remote file.
            URL remote = new URL(systemId);
            URLConnection conn = remote.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            conn.connect();
            in = conn.getInputStream();
            out = tempFile.getContent().getOutputStream();
            IOUtils.copy(in, out);
          }catch(Exception e){
            logger.warn(e.getMessage());
            IOUtils.closeQuietly(out);
            try{
              tempFile.delete();
            }catch(Exception ignore){
            }

            IOException ex = new IOException(e.getMessage());
            ex.initCause(e);
            missingSources.put(systemId, ex);
            throw ex;
          }finally{
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
          }
        }

        InputSource source =
          new InputSource(tempFile.getContent().getInputStream());
        source.setSystemId(location);
        return source;

      }else if(location.startsWith("file:")){
        location = location.substring("file:".length());
        if(location.startsWith("//")){
          location = location.substring(2);
        }
        if (FileUtils.getFullPath(location).equals(
              FileUtils.getPath(location)))
        {
          location = FileUtils.concat(path, location);
        }

        if(!new File(location).exists()){
          StringBuffer resource = new StringBuffer()
            .append("/resources/")
            .append(FileUtils.getExtension(location))
            .append('/')
            .append(FileUtils.getFileName(location))
            .append('.')
            .append(FileUtils.getExtension(location));
          URL url = Services.getResource(resource.toString());
          if(url != null){
            return new InputSource(url.toString());
          }
        }
        return new InputSource(location);
      }

      return new InputSource(systemId);
    }
  }
}
