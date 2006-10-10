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
package org.eclim.misc.ant;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.taskdefs.Property;

import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;

import org.eclim.util.CommandExecutor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Ant task for executing vunit test cases.
 * <p/>
 * Currently only runs on unix systems.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class VUnitTask
  extends Task
{
  private static final String TESTSUITE = "testsuite";
  private static final String PLUGIN = "\"source <plugin>\"";
  private static final String OUTPUT = "\"let g:vimUnitOutputDir='<todir>'\"";
  private static final String TESTCASE =
    "\"silent! call VURunnerRunTests('<basedir>', '<testcase>')\"";
  private static final String[] VUNIT = {
    "vim", "--cmd", "", "--cmd", "", "--cmd", "", "-c", "", "-c", "\"qa!\""
  };

  private File plugin;
  private File todir;
  private List filesets = new ArrayList();
  private List properties = new ArrayList();
  private String failureProperty;
  private boolean haltOnFailure;
  private boolean failed;

  /**
   * Executes this task.
   */
  public void execute ()
    throws BuildException
  {
    validateAttributes();

    try{
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      DefaultHandler handler = new ResultHandler();

      String vunit = PLUGIN.replaceFirst("<plugin>", plugin.getAbsolutePath());
      String output = OUTPUT.replaceFirst("<todir>", todir.getAbsolutePath());

      // build properties string.
      StringBuffer propertiesBuffer = new StringBuffer();
      for (Iterator ii = properties.iterator(); ii.hasNext();){
        Environment.Variable var = (Environment.Variable)ii.next();
        if(propertiesBuffer.length() > 0){
          propertiesBuffer.append(" | ");
        }
        propertiesBuffer.append("let ")
          .append(var.getKey())
          .append("='")
          .append(var.getValue()).append("'");
      }
      String setproperties = "\"" + propertiesBuffer.append('"').toString();

      for (Iterator it = filesets.iterator(); it.hasNext();){
        FileSet set = (FileSet)it.next();
        DirectoryScanner scanner = set.getDirectoryScanner(getProject());
        File basedir = scanner.getBasedir();
        String[] files = scanner.getIncludedFiles();

        String run = TESTCASE.replaceFirst("<basedir>", basedir.getAbsolutePath());

        for (int ii = 0; ii < files.length; ii++){
          log("Running: " + files[ii]);

          String[] command = new String[VUNIT.length];
          System.arraycopy(VUNIT, 0, command, 0, VUNIT.length);

          command[2] = setproperties;
          command[4] = vunit;
          command[6] = output;
          command[8] = run.replaceFirst("<testcase>", files[ii]);

          // ncurses and Runtime.exec don't play well together, so execute via sh.
          log("sh -c " + StringUtils.join(command, ' ') + " exit",
              Project.MSG_DEBUG);
          command = new String[]{
            "sh", "-c", StringUtils.join(command, ' '), "exit"
          };

          try{
            CommandExecutor executor = CommandExecutor.execute(command);

            if(executor.getResult().trim().length() > 0){
              log(executor.getResult());
            }

            if(executor.getReturnCode() != 0){
              throw new BuildException(
                  "Failed to run command: " + executor.getErrorMessage());
            }
          }finally{
            // some aspect of the external execution can screw up the terminal,
            // but 'resize' can fix it.
            try{
              Runtime.getRuntime().exec("resize");
            }catch(Exception ignore){
            }
          }

          StringBuffer file = new StringBuffer()
            .append("TEST-")
            .append(FilenameUtils.getPath(files[ii]).replace('/', '.'))
            .append(FilenameUtils.getBaseName(files[ii]))
            .append(".xml");
          File resultFile = new File(FilenameUtils.concat(
              todir.getAbsolutePath(), file.toString()));

          try{
            parser.parse(resultFile, handler);
          }catch(SAXException se){
            if(!TESTSUITE.equals(se.getMessage())){
              throw se;
            }
          }

          if(failed){
            if (failureProperty != null &&
                getProject().getProperty(failureProperty) == null)
            {
              getProject().setNewProperty(failureProperty, "true");
            }

            if(haltOnFailure){
              throw new BuildException("Test failed: " + files[ii]);
            }
          }
        }
      }
    }catch(BuildException be){
      throw be;
    }catch(Exception e){
      throw new BuildException(e);
    }
  }

  /**
   * Validates the supplied attributes.
   */
  private void validateAttributes ()
    throws BuildException
  {
    if(plugin == null){
      throw new BuildException("Attribute 'plugin' required");
    }

    if(!plugin.exists()){
      throw new BuildException("Supplied 'plugin' file does not exist.");
    }

    if(todir == null){
      throw new BuildException("Attribute 'todir' required");
    }

    if(!todir.exists() || !todir.isDirectory()){
      throw new BuildException(
          "Supplied 'todir' is not a directory or does not exist.");
    }

    if(filesets.size() == 0){
      throw new BuildException(
          "You must supply at least one fileset of test files to execute.");
    }
  }

  /**
   * Adds a set of test files to execute.
   * @param set Set of test files.
   */
  public void addFileset (FileSet set)
  {
    filesets.add(set);
  }

  /**
   * Adds a property to be set when running the tests.
   * @param prop The property.
   */
  public void addSysproperty (Environment.Variable prop)
  {
    properties.add(prop);
  }

  /**
   * Sets the plugin for this instance.
   *
   * @param plugin The plugin.
   */
  public void setPlugin (File plugin)
  {
    this.plugin = plugin;
  }

  /**
   * Sets the todir for this instance.
   *
   * @param todir The todir.
   */
  public void setTodir (File todir)
  {
    this.todir = todir;
  }

  /**
   * Sets the name of the property to be set if a failure occurs.
   *
   * @param failureProperty The failureProperty.
   */
  public void setFailureproperty (String failureProperty)
  {
    this.failureProperty = failureProperty;
  }

  /**
   * Sets whether or not to halt on failure.
   *
   * @param haltOnFailure The haltOnFailure.
   */
  public void setHaltonfailure (boolean haltOnFailure)
  {
    this.haltOnFailure = haltOnFailure;
  }

  /**
   * SAX handler for vunit result parsing.
   */
  private class ResultHandler
    extends DefaultHandler
  {
    /**
     * {@inheritDoc}
     * @see org.xml.sax.ContentHandler#startElement(String,String,String,Attributes)
     */
    public void startElement (
        String uri, String localName, String qName, Attributes atts)
      throws SAXException
    {
      int tests = Integer.parseInt(atts.getValue("tests"));
      int failures = Integer.parseInt(atts.getValue("failures"));
      String time = atts.getValue("time");
      String name = atts.getValue("name");

      StringBuffer buffer = new StringBuffer()
        .append("Tests run: ").append(tests)
        .append(", Failures: ").append(failures)
        .append(", Time elapsed: ").append(time).append(" sec");
      log(buffer.toString());

      if(failures > 0){
        log("Test " + name + " FAILED");
        failed = true;
      }

      throw new SAXException(TESTSUITE);
    }
  }
}
