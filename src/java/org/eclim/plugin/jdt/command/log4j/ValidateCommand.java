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
package org.eclim.plugin.jdt.command.log4j;

import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Command to validate a log4j xml file.
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
  public Object execute (CommandLine _commandLine)
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String project = _commandLine.getValue(Options.PROJECT_OPTION);

      // first validate xml.
      List list = super.validate(file, false);

      // no xml errors, validate log4j values.
      if(list.size() == 0){
        list = validate(JavaUtils.getJavaProject(project), file);
      }

      return filter(_commandLine, list.toArray(new Error[list.size()]));
    }catch(Throwable t){
      return t;
    }
  }

  /**
   * Validates log4j values.
   *
   * @param _project The project.
   * @param _file The log4j xml file.
   * @return List of errors.
   */
  private List validate (IJavaProject _project, String _file)
    throws Exception
  {
    Log4jHandler handler = new Log4jHandler(_project, _file);

    org.apache.xerces.parsers.SAXParser parser =
      new org.apache.xerces.parsers.SAXParser();
    parser.setEntityResolver(new EntityResolver());
    parser.setContentHandler(handler);
    parser.parse(_file);

    return handler.getErrors();
  }

  private static class Log4jHandler
    extends DefaultHandler
  {
    //private static final String APPENDER = "appender";
    //private static final String APPENDER_REF = "appender-ref";
    private static final String CATEGORY = "category";
    private static final String CLASS = "class";
    private static final String LEVEL = "level";
    private static final String LOGGER = "logger";
    private static final String NAME = "name";
    private static final String PRIORITY = "priority";
    //private static final String REF = "ref";
    private static final String VALUE = "value";

    private static final List LEVELS = new ArrayList();
    static {
      LEVELS.add("debug");
      LEVELS.add("info");
      LEVELS.add("warn");
      LEVELS.add("fatal");
    }

    private Locator locator;
    private IJavaProject project;
    private String file;
    private List errors = new ArrayList();

    //private List appenders = new ArrayList();

    /**
     * Constructs a new instance.
     *
     * @param project The project for this instance.
     * @param file The log4j xml file.
     */
    public Log4jHandler (IJavaProject project, String file)
    {
      this.project = project;
      this.file = file;
    }

    /**
     * {@inheritDoc}
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator (Locator locator)
    {
      super.setDocumentLocator(locator);
      this.locator = locator;
    }

    /**
     * {@inheritDoc}
     * @see org.xml.sax.ContentHandler#startElement(String,String,String,Attributes)
     */
    public void startElement (
        String uri, String localName, String qName, Attributes atts)
      throws SAXException
    {
      try{
        /*if(APPENDER.equals(localName)){
          String name = atts.getValue(NAME);
          if(name != null){
            appenders.add(name);
          }
        }else if(APPENDER_REF.equals(localName)){
          String ref = atts.getValue(REF);
          if(ref != null && !appenders.contains(ref)){
            String message =
              Services.getMessage("log4j.appender.name.invalid", ref);
            errors.add(new Error(
                  message, file, locator.getLineNumber(), 1, false
            ));
          }*/
        if(CATEGORY.equals(localName) || LOGGER.equals(localName)){
          String name = atts.getValue(NAME);
          if(name != null){
            IPackageFragment pkg = getPackage(name);
            if(pkg == null){
              IType type = project.findType(name);
              if(type == null || !type.exists()){
                String message =
                  Services.getMessage("log4j.logger.name.invalid", name);
                errors.add(new Error(
                      message, file, locator.getLineNumber(), 1, false
                ));
              }
            }
          }
        }else if(PRIORITY.equals(localName) || LEVEL.equals(localName)){
          String value = atts.getValue(VALUE);
          if(atts.getValue(CLASS) == null && value != null){
            if(!LEVELS.contains(value.trim().toLowerCase())){
              String message =
                Services.getMessage("log4j.level.name.invalid", value);
              errors.add(new Error(
                    message, file, locator.getLineNumber(), 1, false
              ));
            }
          }
        }

        // validate any class attributes.
        String classname = atts.getValue(CLASS);
        if(classname != null){
          IType type = project.findType(classname);
          if(type == null || !type.exists()){
            String message = Services.getMessage("type.not.found",
                new Object[]{project.getElementName(), classname});
            errors.add(new Error(
                  message, file, locator.getLineNumber(), 1, false
            ));
          }
        }
      }catch(Exception e){
        throw new RuntimeException(e);
      }
    }

    /**
     * Gets any errors.
     *
     * @return List of errors.
     */
    public List getErrors ()
    {
      return errors;
    }

    private IPackageFragment getPackage (String name)
      throws Exception
    {
      IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
      for (int ii = 0; ii < roots.length; ii++){
        IPackageFragment fragment = roots[ii].getPackageFragment(name);
        if(fragment != null && fragment.exists()){
          return fragment;
        }
      }
      return null;
    }
  }

  private static class EntityResolver
    implements org.xml.sax.EntityResolver
  {
    /**
     * {@inheritDoc}
     */
    public InputSource resolveEntity (String _publicId, String _systemId)
      throws SAXException, IOException
    {
      URL url = Services.getResource("/resources/dtd/log4j.dtd");
      return new InputSource(url.toString());
    }
  }
}
