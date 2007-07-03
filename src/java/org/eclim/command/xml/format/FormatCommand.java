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
package org.eclim.command.xml.format;

import java.io.FileInputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.sax.SAXSource;

import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.xml.sax.InputSource;

/**
 * Command to format an xml file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class FormatCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    FileInputStream in = null;
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      int lineWidth = _commandLine.getIntValue(Options.LINE_WIDTH_OPTION);
      int indent = _commandLine.getIntValue(Options.INDENT_OPTION);

      // javax.xml.transform (indentation issues)
      TransformerFactory factory =TransformerFactory.newInstance();
      factory.setAttribute("indent-number", Integer.valueOf(indent));
      Transformer serializer = factory.newTransformer();
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      // broken in 1.5
      /*serializer.setOutputProperty(
          "{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
      in = new FileInputStream(file);
      serializer.transform(new SAXSource(new InputSource(in)),
          new StreamResult(System.out));*/
      in = new FileInputStream(file);
      serializer.transform(new SAXSource(new InputSource(in)),
          new StreamResult(new OutputStreamWriter(System.out, "utf-8")));

      return StringUtils.EMPTY;
    }catch(Throwable t){
      return t;
    }finally{
      IOUtils.closeQuietly(in);
    }
  }
}
