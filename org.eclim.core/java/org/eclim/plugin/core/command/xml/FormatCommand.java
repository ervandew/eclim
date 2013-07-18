/**
 * Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
import java.io.Writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.IOUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

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
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    FileInputStream in = null;
    try {
      String file = commandLine.getValue(Options.FILE_OPTION);
      String format = commandLine.getValue("m");
      int lineWidth = commandLine.getIntValue(Options.LINE_WIDTH_OPTION);
      int indent = commandLine.getIntValue(Options.INDENT_OPTION);

      in = new FileInputStream(file);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // disable validation since we only want to reformat.
      factory.setValidating(false);
      factory.setValidating(false);
      factory.setFeature("http://xml.org/sax/features/namespaces", false);
      factory.setFeature("http://xml.org/sax/features/validation", false);
      factory.setFeature(
          "http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
          false);
      factory.setFeature(
          "http://apache.org/xml/features/nonvalidating/load-external-dtd",
          false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new InputSource(in));

      DOMImplementationRegistry registry =
        DOMImplementationRegistry .newInstance();
      DOMImplementationLS impl = (DOMImplementationLS)
        registry.getDOMImplementation("LS");

      LSSerializer serializer = impl.createLSSerializer();
      serializer.getDomConfig()
        .setParameter("format-pretty-print", Boolean.TRUE);
      if (format.equals("unix")) {
        serializer.setNewLine("\n");
      } else if (format.equals("dos")) {
        serializer.setNewLine("\r\n");
      }

      // attempt to serialize with the requested indent + line width
      try{
        return serialize(document, serializer, indent, lineWidth);
      }catch(Exception e){
        // if the reflection hack fails, then just format with the default
        // indent + line width
        return serializer.writeToString(document);
      }
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  // very gross reflection, all to set the indent + line width
  private String serialize(
      Document document, LSSerializer serializer, int indent, int lineWidth)
    throws Exception
  {
    Object ser = getField(serializer, "serializer");

    invokeMethod(serializer, "prepareForSerialization",
        new Class[]{ser.getClass(), Node.class}, new Object[]{ser, document});

    StringWriter out = new StringWriter();
    invokeMethod(ser, "setOutputCharStream",
        new Class[]{Writer.class}, new Object[]{out});
    invokeMethod(ser, "reset", new Class[0], new Object[0]);
    invokeMethod(ser, "prepare", new Class[0], new Object[0]);

    Object printer = getField(ser, "_printer");

    // set the indent and line width
    Object format = getField(printer, "_format");
    invokeMethod(format, "setIndent",
        new Class[]{Integer.TYPE}, new Object[]{indent});
    invokeMethod(format, "setLineWidth",
        new Class[]{Integer.TYPE}, new Object[]{lineWidth});

    // perform the serialization (from BaseMarkupSerializer.serialize(Document))
    invokeMethod(ser, "serializeNode",
        new Class[]{Node.class}, new Object[]{document});
    invokeMethod(ser, "serializePreRoot", new Class[0], new Object[0]);
    invokeMethod(printer, "flush", new Class[0], new Object[0]);
    Exception ex = (Exception)getField(printer, "_exception");
    if (ex != null){
      throw ex;
    }

    return out.toString();
  }

  @SuppressWarnings("rawtypes")
  private Object getField(Object obj, String name)
    throws Exception
  {
    Class clazz = obj.getClass();
    Exception ex = null;
    while (clazz != null){
      try{
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
      }catch(NoSuchFieldException nsfe){
        ex = nsfe;
        clazz = clazz.getSuperclass();
      }
    }

    throw ex;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void invokeMethod(
      Object obj, String name, Class[] params, Object[] args)
    throws Exception
  {
    Class clazz = obj.getClass();
    Exception ex = null;
    while (clazz != null){
      try{
        Method method = clazz.getDeclaredMethod(name, params);
        method.setAccessible(true);
        method.invoke(obj, args);
        return;
      }catch(NoSuchMethodException nsme){
        ex = nsme;
        clazz = clazz.getSuperclass();
      }
    }

    throw ex;
  }
}
