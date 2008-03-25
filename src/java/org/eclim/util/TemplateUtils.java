/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import java.util.HashMap;
import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Writable;

import groovy.text.Template;
import groovy.text.TemplateEngine;

import org.codehaus.groovy.control.CompilationFailedException;

import org.codehaus.groovy.runtime.InvokerHelper;

import org.eclim.Services;

import org.eclim.logging.Logger;

import org.eclim.plugin.PluginResources;

/**
 * Utility class for evaluating templates.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class TemplateUtils
{
  private static final Logger logger = Logger.getLogger(TemplateUtils.class);

  private static final SimpleTemplateEngine TEMPLATE_ENGINE =
    new SimpleTemplateEngine();

  private static final String TEMPLATE_ROOT = "/resources/templates/";
  private static final HashMap<String,Template> TEMPLATE_CACHE =
    new HashMap<String,Template>();

  /**
   * Evaluates the template supplied via the specfied reader into the supplied
   * writer w/ the specified values.
   *
   * @param _resources The plugin resources.
   * @param _template The template file name.
   * @param _values The template values.
   * @return The evaluation result.
   */
  public static String evaluate (
      PluginResources _resources, String _template, Map<String,Object> _values)
    throws Exception
  {
    String key = _resources.getName() + '_' + _template;
    Template template = TEMPLATE_CACHE.get(key);
    if(template == null){
      _template = TEMPLATE_ROOT + _template;
      BufferedReader reader = null;
      try{
        reader = new BufferedReader(new InputStreamReader(
              _resources.getResourceAsStream(_template)));
      }catch(NullPointerException npe){
        IllegalArgumentException iae = new IllegalArgumentException(
            Services.getMessage("template.not.found", _template));
        iae.initCause(npe);
        throw iae;
      }
      template = TEMPLATE_ENGINE.createTemplate(reader);
      TEMPLATE_CACHE.put(key, template);
    }

    return template.make(_values).toString();
  }


  /**
   * A direct copy of groovy's SimpleTemplateEngine modified to avoid the blank
   * line syndrome induced for every line of dynamic groovy code in the
   * template.
   */
  public static class SimpleTemplateEngine
    extends TemplateEngine
  {
    public Template createTemplate(Reader reader)
      throws CompilationFailedException, IOException
    {
      SimpleTemplate template = new SimpleTemplate();
      GroovyShell shell = new GroovyShell();
      String script = template.parse(reader);
      try{
        template.script = shell.parse(script);
      }catch(RuntimeException e){
        System.out.println("Error running template script:");
        int lnum = 1;
        for(String line : StringUtils.split(script, '\n')){
          System.out.println(String.valueOf(lnum++) + ' ' + line);
        }
        throw e;
      }
      return template;
    }

    private static class SimpleTemplate
      implements Template
    {
      protected Script script;

      public Writable make() {
        return make(null);
      }

      public Writable make (final Map map) {
        return new Writable() {
          /**
           * Write the template document with the set binding applied to the writer.
           *
           * @see groovy.lang.Writable#writeTo(java.io.Writer)
           */
          public Writer writeTo (Writer writer) {
            Binding binding;
            if (map == null)
              binding = new Binding();
            else
              binding = new Binding(map);
            Script scriptObject =
              InvokerHelper.createScript(script.getClass(), binding);
            PrintWriter pw = new PrintWriter(writer);
            scriptObject.setProperty("out", pw);
            scriptObject.run();
            pw.flush();
            return writer;
          }

          /**
           * Convert the template and binding into a result String.
           *
           * @see java.lang.Object#toString()
           */
          public String toString() {
            try {
              StringWriter sw = new StringWriter();
              writeTo(sw);
              return sw.toString();
            } catch (Exception e) {
              return e.toString();
            }
          }
        };
      }

      /**
       * Parse the text document looking for <% or <%= and then call out to the
       * appropriate handler, otherwise copy the text directly into the script
       * while escaping quotes.
       *
       * @param reader
       * @throws IOException
       */
      protected String parse(Reader reader)
        throws IOException
      {
        if (!reader.markSupported()) {
          reader = new BufferedReader(reader);
        }
        StringWriter sw = new StringWriter();
        startScript(sw);
        boolean newline = true;
        int c;
        while ((c = reader.read()) != -1) {
          if (c == '<') {
            reader.mark(1);
            c = reader.read();
            if (c != '%') {
              sw.write('<');
              reader.reset();
            } else {
              reader.mark(1);
              c = reader.read();
              if (c == '=') {
                groovyExpression(reader, sw);
              } else {
                reader.reset();
                groovySection(reader, sw);
                newline = false;
              }
            }
            continue; // at least '<' is consumed ... read next chars.
          }
          if (c == '\"') {
            sw.write('\\');
          }
          /*
           * Handle raw new line characters.
           */
          if (c == '\n' || c == '\r') {
            if (c == '\r') { // on Windows, "\r\n" is a new line.
              reader.mark(1);
              c = reader.read();
              if (c != '\n') {
                reader.reset();
              }
            }
            if(newline){
              sw.write("\\n\");\nout.print(\"");
            }else{
              newline = true;
            }
            continue;
          }
          sw.write(c);
        }
        endScript(sw);
        String result = sw.toString();
        return result;
      }

      private void startScript (StringWriter sw)
      {
        sw.write("/* Generated by SimpleTemplateEngine */\n");
        sw.write("out.print(\"");
      }

      private void endScript (StringWriter sw)
      {
        sw.write("\");\n");
      }

      /**
       * Closes the currently open write and writes out the following text as a
       * GString expression until it reaches an end %>.
       *
       * @param reader
       * @param sw
       * @throws IOException
       */
      private void groovyExpression (Reader reader, StringWriter sw)
        throws IOException
      {
        sw.write("\");out.print(\"${");
        int c;
        while ((c = reader.read()) != -1) {
          if (c == '%') {
            c = reader.read();
            if (c != '>') {
              sw.write('%');
            } else {
              break;
            }
          }
          if (c != '\n' && c != '\r') {
            sw.write(c);
          }
        }
        sw.write("}\");\nout.print(\"");
      }

      /**
       * Closes the currently open write and writes the following text as normal
       * Groovy script code until it reaches an end %>.
       *
       * @param reader
       * @param sw
       * @throws IOException
       */
      private void groovySection (Reader reader, StringWriter sw)
        throws IOException
      {
        sw.write("\");");
        int c;
        while ((c = reader.read()) != -1) {
          if (c == '%') {
            c = reader.read();
            if (c != '>') {
              sw.write('%');
            } else {
              break;
            }
          }
          /* Don't eat EOL chars in sections - as they are valid instruction separators.
           * See http://jira.codehaus.org/browse/GROOVY-980
           */
          // if (c != '\n' && c != '\r') {
          sw.write(c);
          //}
        }
        sw.write(";\nout.print(\"");
      }

    }
  }
}
