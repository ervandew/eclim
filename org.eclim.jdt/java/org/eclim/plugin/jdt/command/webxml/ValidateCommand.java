/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.webxml;

import java.util.ArrayList;
import java.util.List;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Command to validate a web.xml file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "webxml_validate",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class ValidateCommand
  extends org.eclim.plugin.core.command.xml.ValidateCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);

    WebXmlHandler handler = new WebXmlHandler(
        JavaUtils.getJavaProject(project), file);

    List<Error> errors = super.validate(project, file, false, handler);
    errors.addAll(handler.getErrors());

    return errors;
  }

  private static class WebXmlHandler
    extends DefaultHandler
  {
    private static final String CLASS = "-class";
    private static final String FILTER = "filter";
    private static final String FILTER_MAPPING = "filter-mapping";
    private static final String FILTER_NAME = "filter-name";
    private static final String SERVLET = "servlet";
    private static final String SERVLET_MAPPING = "servlet-mapping";
    private static final String SERVLET_NAME = "servlet-name";

    private Locator locator;
    private IJavaProject project;
    private String file;
    private StringBuffer text = new StringBuffer();
    private boolean mapping = false;
    private ArrayList<String> filters = new ArrayList<String>();
    private ArrayList<String> servlets = new ArrayList<String>();
    private ArrayList<Error> errors = new ArrayList<Error>();

    //private List appenders = new ArrayList();

    /**
     * Constructs a new instance.
     *
     * @param project The project for this instance.
     * @param file The log4j xml file.
     */
    public WebXmlHandler (IJavaProject project, String file)
      throws Exception
    {
      this.project = project;
      this.file = ProjectUtils.getFilePath(project.getProject(), file);
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
      super.setDocumentLocator(locator);
      this.locator = locator;
    }

    @Override
    public void startElement(
        String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      if(SERVLET.equals(localName) || FILTER.equals(localName)){
        mapping = false;
      }else if(SERVLET_MAPPING.equals(localName) ||
          FILTER_MAPPING.equals(localName))
      {
        mapping = true;
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      try{
        if(localName.endsWith(CLASS)){
          String name = text.toString().trim();
          IType type = project.findType(name);
          if(type == null || !type.exists()){
            String message = Services.getMessage("class.not.found",
                project.getElementName(), name);
            errors.add(new Error(
                  message, file, locator.getLineNumber(), 1, false
            ));
          }
        }else if (SERVLET_NAME.equals(localName)){
          String name = text.toString().trim();
          if (mapping && !servlets.contains(name)){
            String message = Services.getMessage("servlet.not.found", name);
            errors.add(new Error(
                  message, file, locator.getLineNumber(), 1, false
            ));
          }else if (!mapping){
            servlets.add(name);
          }
        }else if (FILTER_NAME.equals(localName)){
          String name = text.toString().trim();
          if (mapping && !filters.contains(name)){
            String message = Services.getMessage("filter.not.found", name);
            errors.add(new Error(
                  message, file, locator.getLineNumber(), 1, false
            ));
          }else if (!mapping){
            filters.add(name);
          }
        }
      }catch(Exception e){
        throw new RuntimeException(e);
      }finally{
        text = new StringBuffer();
      }
    }

    @Override
    public void characters(char[] ch, int start, int length)
      throws SAXException
    {
      for (int ii = start; ii < start + length; ii++){
        text.append(ch[ii]);
      }
    }

    /**
     * Gets any errors.
     *
     * @return List of errors.
     */
    public List<Error> getErrors()
    {
      return errors;
    }
  }
}
