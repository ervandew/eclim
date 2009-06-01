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
package org.eclim.plugin.jdt.command.log4j;

import java.util.ArrayList;
import java.util.List;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.filter.ErrorFilter;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Command to validate a log4j xml file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "log4j_validate",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class ValidateCommand
  extends org.eclim.plugin.core.command.xml.ValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);

    Log4jHandler handler = new Log4jHandler(
        JavaUtils.getJavaProject(project), file);

    List<Error> errors = super.validate(project, file, false, handler);
    errors.addAll(handler.getErrors());

    return ErrorFilter.instance.filter(commandLine, errors);
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

    private static final ArrayList<String> LEVELS = new ArrayList<String>();
    static {
      LEVELS.add("debug");
      LEVELS.add("info");
      LEVELS.add("warn");
      LEVELS.add("fatal");
    }

    private Locator locator;
    private IJavaProject project;
    private String file;
    private ArrayList<Error> errors = new ArrayList<Error>();

    //private List appenders = new ArrayList();

    /**
     * Constructs a new instance.
     *
     * @param project The project for this instance.
     * @param file The log4j xml file.
     */
    public Log4jHandler (IJavaProject project, String file)
      throws Exception
    {
      this.project = project;
      this.file = ProjectUtils.getFilePath(project.getProject(), file);
    }

    /**
     * {@inheritDoc}
     * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator)
    {
      super.setDocumentLocator(locator);
      this.locator = locator;
    }

    /**
     * {@inheritDoc}
     * @see org.xml.sax.helpers.DefaultHandler#startElement(String,String,String,Attributes)
     */
    public void startElement(
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
                project.getElementName(), classname);
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
    public List<Error> getErrors()
    {
      return errors;
    }

    private IPackageFragment getPackage(String name)
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
}
