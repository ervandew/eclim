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
package org.eclim.plugin.jdt.command.webxml;

import java.util.ArrayList;
import java.util.List;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.ProjectUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Command to validate a web.xml file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ValidateCommand
  extends org.eclim.command.xml.validate.ValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String project = _commandLine.getValue(Options.PROJECT_OPTION);
    String file = _commandLine.getValue(Options.FILE_OPTION);

    WebXmlHandler handler = new WebXmlHandler(
        JavaUtils.getJavaProject(project), file);

    List<Error> errors = super.validate(project, file, false, handler);
    errors.addAll(handler.getErrors());

    return ErrorFilter.instance.filter(_commandLine, errors);
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

    /**
     * {@inheritDoc}
     * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator (Locator locator)
    {
      super.setDocumentLocator(locator);
      this.locator = locator;
    }

    /**
     * {@inheritDoc}
     * @see org.xml.sax.helpers.DefaultHandler#startElement(String,String,String,Attributes)
     */
    public void startElement (
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

    /**
     * {@inheritDoc}
     * @see org.xml.sax.helpers.DefaultHandler#endElement(String,String,String)
     */
    public void endElement (String uri, String localName, String qName)
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

    /**
     * {@inheritDoc}
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[],int,int)
     */
    public void characters (char[] ch, int start, int length)
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
    public List<Error> getErrors ()
    {
      return errors;
    }
  }
}
