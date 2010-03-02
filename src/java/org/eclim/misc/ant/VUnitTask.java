/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
package org.eclim.misc.ant;

import java.io.File;

import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;

import org.eclim.util.CommandExecutor;

import org.eclim.util.file.FileUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Ant task for executing vunit test cases.
 * <p/>
 * Currently only runs on unix systems.
 *
 * @author Eric Van Dewoestine
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
    "vim",
    "-u", "NONE",
    "--cmd", "\"set nocp | sy on | filetype plugin indent on | ru plugin/eclim.vim\"",
    "--cmd", "\"set cot=menuone,longest et sw=2 ts=2\"",
    "--cmd", "",
    "--cmd", "",
    "--cmd", "",
    "-c", "",
    "-c", "\"qa!\""
  };

  private File plugin;
  private File todir;
  private ArrayList<FileSet> filesets = new ArrayList<FileSet>();
  private ArrayList<Environment.Variable> properties =
    new ArrayList<Environment.Variable>();
  private String failureProperty;
  private boolean haltOnFailure;
  private boolean failed;

  /**
   * Executes this task.
   */
  public void execute()
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
      for (Environment.Variable var : properties){
        if(propertiesBuffer.length() > 0){
          propertiesBuffer.append(" | ");
        }
        propertiesBuffer.append("let ")
          .append(var.getKey())
          .append("='")
          .append(var.getValue()).append("'");
      }
      String setproperties = "\"" + propertiesBuffer.append('"').toString();

      for (FileSet set : filesets){
        DirectoryScanner scanner = set.getDirectoryScanner(getProject());
        File basedir = scanner.getBasedir();
        String[] files = scanner.getIncludedFiles();

        String run = TESTCASE.replaceFirst("<basedir>", basedir.getAbsolutePath());

        for (int ii = 0; ii < files.length; ii++){
          log("Running: " + files[ii]);

          String[] command = new String[VUNIT.length];
          System.arraycopy(VUNIT, 0, command, 0, VUNIT.length);

          command[8] = setproperties;
          command[10] = vunit;
          command[12] = output;
          command[14] = run.replaceFirst("<testcase>", files[ii]);

          // ncurses and Runtime.exec don't play well together, so execute via sh.
          log("sh -c " + StringUtils.join(command, ' ') + " exit",
              Project.MSG_DEBUG);
          command = new String[]{
            "sh", "-c", StringUtils.join(command, ' '), "exit"
          };

          try{
            log("vunit: " + StringUtils.join(command, ' '), Project.MSG_DEBUG);
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
            .append(FileUtils.getPath(files[ii]).replace('/', '.'))
            .append(FileUtils.getFileName(files[ii]))
            .append(".xml");
          File resultFile = new File(FileUtils.concat(
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
                getProject().getProperty(failureProperty) == null){
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
  private void validateAttributes()
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
  public void addFileset(FileSet set)
  {
    filesets.add(set);
  }

  /**
   * Adds a property to be set when running the tests.
   * @param prop The property.
   */
  public void addSysproperty(Environment.Variable prop)
  {
    properties.add(prop);
  }

  /**
   * Sets the plugin for this instance.
   *
   * @param plugin The plugin.
   */
  public void setPlugin(File plugin)
  {
    this.plugin = plugin;
  }

  /**
   * Sets the todir for this instance.
   *
   * @param todir The todir.
   */
  public void setTodir(File todir)
  {
    this.todir = todir;
  }

  /**
   * Sets the name of the property to be set if a failure occurs.
   *
   * @param failureProperty The failureProperty.
   */
  public void setFailureproperty(String failureProperty)
  {
    this.failureProperty = failureProperty;
  }

  /**
   * Sets whether or not to halt on failure.
   *
   * @param haltOnFailure The haltOnFailure.
   */
  public void setHaltonfailure(boolean haltOnFailure)
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
     * @see org.xml.sax.helpers.DefaultHandler#startElement(String,String,String,Attributes)
     */
    public void startElement(
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
