/**
 * Copyright (c) 2004 - 2005
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

import org.eclim.command.Error;

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
  /**
   * Validate the supplied xml file.
   *
   * @param _filename The file path to the xml file.
   * @return A possibly empty array of errors.
   */
  public static Error[] validateXml (String _filename)
    throws Exception
  {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);

    return validate(_filename, factory.newSAXParser());
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
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(true);

    SAXParser parser = factory.newSAXParser();
    parser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");
    parser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaSource",
        "file://" + _schema);

    return validate(_filename, parser);
  }

  /**
   * Validate the supplied file with the specified parser.
   *
   * @param _filename The path to the xml file.
   * @param _parser The SAXParser.
   * @return A possibly empty array of errors.
   */
  private static Error[] validate (String _filename, SAXParser _parser)
    throws Exception
  {
    ErrorAggregator handler = new ErrorAggregator(_filename);
    try{
      _parser.parse(_filename, handler);
    }catch(SAXParseException spe){
      // handler will catch these
    }

    return handler.getErrors();
  }

  /**
   * Handler for collecting errors durring parsing and validation of a xml
   * file.
   */
  public static class ErrorAggregator
    extends DefaultHandler
  {
    private List errors = new ArrayList();
    private String filename;

    /**
     * Creates a new ErrorAggregator for reporting errors for the supplied
     * filename.
     *
     * @param _filename The file being validated.
     */
    public ErrorAggregator (String _filename)
    {
      this.filename = _filename;
    }

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
      errors.add(new Error(
            _ex.getMessage(),
            filename,
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
}
