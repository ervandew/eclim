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
  public static Error[] validateXml (String _filename, String _schema)
    throws Exception
  {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(true);

    ErrorAggregator handler = new ErrorAggregator(_filename);
    SAXParser parser = factory.newSAXParser();
    parser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");
    parser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaSource",
        "file://" + _schema);

    try{
      parser.parse(_filename, handler);
    }catch(SAXParseException spe){
      // handler will catch these
    }

    return handler.getErrors();
  }

  public static class ErrorAggregator
      extends DefaultHandler
    {
      private List errors = new ArrayList();
      private String filename;
      public ErrorAggregator (String _filename)
      {
        this.filename = _filename;
      }
      public void warning (SAXParseException _ex)
        throws SAXException
      {
        addError(_ex);
      }
      public void error (SAXParseException _ex)
        throws SAXException
      {
        addError(_ex);
      }
      public void fatalError (SAXParseException _ex)
        throws SAXException
      {
        addError(_ex);
      }
      private void addError (SAXParseException _ex)
      {
        errors.add(new Error(
              _ex.getMessage(),
              filename,
              _ex.getLineNumber(),
              _ex.getColumnNumber()));
      }
      public Error[] getErrors ()
      {
        return (Error[])errors.toArray(new Error[errors.size()]);
      }
    }
}
