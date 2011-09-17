/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.xml;

import java.io.FileInputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.sax.SAXSource;

import javax.xml.transform.stream.StreamResult;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.IOUtils;

import org.xml.sax.InputSource;

/**
 * Command to format an xml file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "xml_format",
  options =
    "REQUIRED f file ARG," +
    "REQUIRED w linewidth ARG," +
    "REQUIRED i indent ARG," +
    "REQUIRED m fileformat ARG"
)
public class FormatCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String restoreNewline = null;
    FileInputStream in = null;
    try{
      String file = commandLine.getValue(Options.FILE_OPTION);
      //int lineWidth = commandLine.getIntValue(Options.LINE_WIDTH_OPTION);
      int indent = commandLine.getIntValue(Options.INDENT_OPTION);
      String format = commandLine.getValue("m");

      // set the line separator if necessary
      String newline = System.getProperty("line.separator");
      if (newline.equals("\r\n") && format.equals("unix")){
        restoreNewline = newline;
        System.setProperty("line.separator", "\n");
      }else if (newline.equals("\n") && format.equals("dos")){
        restoreNewline = newline;
        System.setProperty("line.separator", "\r\n");
      }

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
          new StreamResult(getContext().out));*/

      StringWriter out = new StringWriter();
      in = new FileInputStream(file);
      serializer.transform(
          new SAXSource(new InputSource(in)), new StreamResult(out));

      return out.toString();
    }finally{
      IOUtils.closeQuietly(in);
      if (restoreNewline != null){
        System.setProperty("line.separator", restoreNewline);
      }
    }
  }
}
