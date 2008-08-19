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
package org.eclim.plugin.jdt.command.checkstyle;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.util.IOUtils;
import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

/**
 * Command which invokes checkstyle on the specified file.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class CheckstyleCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String name = _commandLine.getValue(Options.PROJECT_OPTION);
    String file = _commandLine.getValue(Options.FILE_OPTION);

    IProject project = ProjectUtils.getProject(name, true);
    Map<String,String> options = getPreferences().getOptionsAsMap(project);

    String configFile = options.get("org.eclim.java.checkstyle.config");
    String propsFile = options.get("org.eclim.java.checkstyle.properties");
    Properties properties = System.getProperties();
    if (propsFile != null && !propsFile.equals(StringUtils.EMPTY)){
      FileInputStream fis = null;
      try{
        fis = new FileInputStream(ProjectUtils.getFilePath(project, propsFile));
        properties = new Properties();
        properties.load(fis);
      }finally{
        IOUtils.closeQuietly(fis);
      }
    }
    Configuration config = ConfigurationLoader.loadConfiguration(
        ProjectUtils.getFilePath(project, configFile),
        new PropertiesExpander(properties));

    CheckstyleListener listener = new CheckstyleListener();

    List<File> files = new ArrayList<File>();
    files.add(new File(ProjectUtils.getFilePath(project, file)));

    Checker checker = new Checker();
    ClassLoader cl = Checker.class.getClassLoader();
    checker.setModuleClassLoader(cl);
    checker.configure(config);
    checker.addListener(listener);
    checker.process(files);

    return ErrorFilter.instance.filter(_commandLine, listener.getErrors());
  }

  private class CheckstyleListener
    implements AuditListener
  {
    private ArrayList<Error> errors;

    public CheckstyleListener ()
    {
      errors = new ArrayList<Error>();
    }

    /**
     * {@inheritDoc}
     * @see AuditListener#auditStarted(AuditEvent)
     */
    public void auditStarted (AuditEvent event)
    {
      // ignore
    }

    /**
     * {@inheritDoc}
     * @see AuditListener#auditFinished(AuditEvent)
     */
    public void auditFinished (AuditEvent event)
    {
      // ignore
    }

    /**
     * {@inheritDoc}
     * @see AuditListener#fileStarted(AuditEvent)
     */
    public void fileStarted (AuditEvent event)
    {
      // ignore
    }

    /**
     * {@inheritDoc}
     * @see AuditListener#fileFinished(AuditEvent)
     */
    public void fileFinished (AuditEvent event)
    {
      // ignore
    }

    /**
     * {@inheritDoc}
     * @see AuditListener#addError(AuditEvent)
     */
    public void addError (AuditEvent event)
    {
      errors.add(
          new Error(
            event.getMessage(),
            event.getFileName(),
            event.getLine(),
            event.getColumn(),
            event.getSeverityLevel() != SeverityLevel.ERROR));

    }

    /**
     * {@inheritDoc}
     * @see AuditListener#addException(AuditEvent,Throwable)
     */
    public void addException (AuditEvent event, Throwable throwable)
    {
      throw new RuntimeException(throwable);
    }

    /**
     * Gets the errors for this instance.
     *
     * @return The errors.
     */
    public ArrayList<Error> getErrors ()
    {
      return this.errors;
    }
  }
}
