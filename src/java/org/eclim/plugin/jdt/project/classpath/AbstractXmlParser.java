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
package org.eclim.plugin.jdt.project.classpath;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Abstract base class for parsers that work with xml files.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public abstract class AbstractXmlParser
  implements Parser
{
  private static DocumentBuilderFactory factory;

  /**
   * {@inheritDoc}
   */
  public Dependency[] parse (String _filename)
    throws IOException
  {
    if(factory == null){
      factory = DocumentBuilderFactory.newInstance();
    }
    try{
      Document document = factory.newDocumentBuilder().parse(new File(_filename));

      return parse(document);
    }catch(IOException ioe){
      throw ioe;
    }catch(Exception e){
      throw (IOException)new IOException().initCause(e);
    }
  }

  /**
   * Parse the dependencies from the supplied document.
   *
   * @param _document The document.
   * @return The array of Dependency.
   */
  public abstract Dependency[] parse (Document _document)
    throws IOException;
}
