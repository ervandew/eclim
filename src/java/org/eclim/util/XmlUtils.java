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
package org.eclim.util;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FilenameUtils;

import org.apache.log4j.Logger;

import org.eclim.command.Error;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Some xml utility methods.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class XmlUtils
{
  private static final Logger logger = Logger.getLogger(XmlUtils.class);

  /**
   * Validate the supplied xml file.
   *
   * @param _filename The file path to the xml file.
   * @return A possibly empty array of errors.
   */
  public static Error[] validateXml (String _filename)
    throws Exception
  {
    // jdk < 1.5 requires a doctype to validate (won't just check well formness
    // if no doctype specified like 1.5 does)
    /*SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);

    return validate(_filename, factory.newSAXParser());*/

    ErrorAggregator handler = new ErrorAggregator();
    org.apache.xerces.parsers.SAXParser parser =
      new org.apache.xerces.parsers.SAXParser();
    parser.setFeature("http://xml.org/sax/features/validation", true);
    parser.setErrorHandler(handler);
    parser.setEntityResolver(
        new EntityResolver(FilenameUtils.getFullPath(_filename)));
    try{
      parser.parse(_filename);
    }catch(SAXParseException spe){
      return new Error[]{
        new Error(
            spe.getMessage(),
            _filename,
            spe.getLineNumber(),
            spe.getColumnNumber(),
            false)};
    }catch(Exception e){
      return new Error[]{new Error(e.getMessage(), _filename, 1, 1, false)};
    }

    return handler.getErrors();
  }

  /**
   * Validate the supplied xml file against the specified xsd.
   *
   * @param _filename The file path to the xml file.
   * @param _schema The file path to the xsd.
   * @return A possibly empty array of errors.
   */
  public static Error[] validateXml (String _filename, String _schema)
    throws Exception
  {
    // doesn't work on jdk < 1.5
    /*SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(true);

    SAXParser parser = factory.newSAXParser();
    parser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");
    parser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaSource",
        "file://" + _schema);

    return validate(_filename, parser);*/

    ErrorAggregator handler = new ErrorAggregator();
    org.apache.xerces.parsers.SAXParser parser =
      new org.apache.xerces.parsers.SAXParser();
    parser.setFeature("http://xml.org/sax/features/validation", true);
    parser.setFeature("http://apache.org/xml/features/validation/schema", true);
    parser.setFeature(
        "http://apache.org/xml/features/validation/schema-full-checking", true);
    parser.setProperty(
        "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
        _schema.replace('\\', '/'));
        //"file://" + _schema.replace('\\', '/'));
    parser.setErrorHandler(handler);
    parser.setEntityResolver(
        new EntityResolver(FilenameUtils.getFullPath(_filename)));
    try{
      parser.parse(_filename);
    }catch(SAXParseException spe){
      return new Error[]{
        new Error(
            spe.getMessage(),
            _filename,
            spe.getLineNumber(),
            spe.getColumnNumber(),
            false)};
    }catch(Exception e){
      return new Error[]{new Error(e.getMessage(), _filename, 1, 1, false)};
    }

    return handler.getErrors();
  }

  /**
   * Validate the supplied file with the specified parser.
   *
   * @param _filename The path to the xml file.
   * @param _parser The SAXParser.
   * @return A possibly empty array of errors.
   * FIXME: When start using this again, need to test relative xml entities
   * (test with ant/cvs.xml).
   */
  private static Error[] validate (String _filename, SAXParser _parser)
    throws Exception
  {
    ErrorAggregator handler = new ErrorAggregator();
    try{
      _parser.parse(_filename, handler);
    }catch(SAXParseException spe){
      // handler should catch these
      logger.debug("Unhandled SAXParseException", spe);
    }

    return handler.getErrors();
  }

  /**
   * Handler for collecting errors durring parsing and validation of a xml
   * file.
   */
  private static class ErrorAggregator
    extends DefaultHandler
  {
    private List errors = new ArrayList();

    /**
     * {@inheritDoc}
     */
    public void warning (SAXParseException _ex)
      throws SAXException
    {
      addError(_ex, true);
    }

    /**
     * {@inheritDoc}
     */
    public void error (SAXParseException _ex)
      throws SAXException
    {
      addError(_ex, false);
    }

    /**
     * {@inheritDoc}
     */
    public void fatalError (SAXParseException _ex)
      throws SAXException
    {
      addError(_ex, false);
    }

    /**
     * Adds the supplied SAXException as an Error.
     *
     * @param _ex The SAXException.
     */
    private void addError (SAXParseException _ex, boolean _warning)
    {
      String location = _ex.getSystemId();
      if(location.startsWith("file://")){
        location = location.substring("file://".length());
      }
      errors.add(new Error(
            _ex.getMessage(),
            location,
            _ex.getLineNumber(),
            _ex.getColumnNumber(),
            _warning));
    }

    /**
     * Gets the possibly empty array of errors.
     *
     * @return Array of Error.
     */
    public Error[] getErrors ()
    {
      return (Error[])errors.toArray(new Error[errors.size()]);
    }
  }

  /**
   * Extension to apache xerces entity manager.
   */
  private static class EntityResolver
    implements org.xml.sax.EntityResolver
  {
    private String path;

    /**
     * Constructs a new instance.
     *
     * @param _path The path for all relative entities to be relative to.
     */
    public EntityResolver (String _path)
    {
      path = _path;
    }

    /**
     * {@inheritDoc}
     */
    public InputSource resolveEntity (String _publicId, String _systemId)
      throws SAXException, java.io.IOException
    {
      String location = _systemId;
      if(location.startsWith("file:")){
        location = location.substring("file:".length());
        if(location.startsWith("//")){
          location = location.substring(2);
        }
        if(FilenameUtils.getFullPath(location).equals(
              FilenameUtils.getPath(location)))
        {
          location = FilenameUtils.concat(path, location);
        }
      }
      return new InputSource(location);
    }
  }
}
