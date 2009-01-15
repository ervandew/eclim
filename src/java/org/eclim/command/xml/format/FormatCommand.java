/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.command.xml.format;

import java.io.FileInputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.sax.SAXSource;

import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;

import org.xml.sax.InputSource;

/**
 * Command to format an xml file.
 *
 * @author Eric Van Dewoestine
 */
public class FormatCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    FileInputStream in = null;
    try{
      String file = commandLine.getValue(Options.FILE_OPTION);
      //int lineWidth = commandLine.getIntValue(Options.LINE_WIDTH_OPTION);
      int indent = commandLine.getIntValue(Options.INDENT_OPTION);

      // javax.xml.transform (indentation issues)
      TransformerFactory factory = TransformerFactory.newInstance();
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
    }finally{
      IOUtils.closeQuietly(in);
    }
  }
}
