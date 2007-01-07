/**
 * Copyright (c) 2005 - 2007
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
package org.eclim.installer.step;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.formic.Installer;

import org.formic.wizard.step.InstallStep;

/**
 * Step which installs necessary third party eclipse plugins.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class EclipsePluginsStep
  extends InstallStep
{
  private static final String BEGIN_TASK = "beginTask";
  private static final String SUB_TASK = "subTask";
  private static final String INTERNAL_WORKED = "internalWorked";
  private static final String SET_TASK_NAME = "setTaskName";

  private String taskName = "";

  /**
   * Constructs this step.
   */
  public EclipsePluginsStep (String name)
  {
    super(name);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.InstallStep#execute()
   */
  protected void execute ()
    throws Exception
  {
    List dependencies = getDependencies();
    filterDependencies(dependencies, getInstalledFeatures());
    guiOverallProgress.setMaximum(dependencies.size());
    guiOverallProgress.setValue(0);
    for (Iterator ii = dependencies.iterator(); ii.hasNext();){
      String[] dependency = (String[])ii.next();
      guiOverallLabel.setText("Installing feature: " + dependency[1]);

      InstallProcess process = new InstallProcess(dependency);
      try{
        process.start();
        process.join();
        if(process.getReturnCode() != 0){
          throw new RuntimeException(process.getErrorMessage());
        }
      }finally{
        process.destroy();
      }
      guiOverallProgress.setValue(guiOverallProgress.getValue() + 1);
    }
    guiTaskLabel.setText("");
  }

  private List getDependencies ()
    throws Exception
  {
    ArrayList dependencies = new ArrayList();
    Properties properties = new Properties();
    properties.load(EclipsePluginsStep.class.getResourceAsStream(
          "/resources/dependencies.properties"));
    String[] features = Installer.getContext().getKeysByPrefix("featureList");
    for (int ii = 0; ii < features.length; ii++){
      Boolean enabled = (Boolean)Installer.getContext().getValue(features[ii]);
      String name = features[ii].substring(features[ii].indexOf('.') + 1);
      if(enabled.booleanValue() && properties.containsKey(name)){
        String[] depends = StringUtils.split(properties.getProperty(name), ',');
        for (int jj = 0; jj < depends.length; jj++){
          String[] dependency = StringUtils.split(depends[jj]);
          dependencies.add(dependency);
        }
      }
    }
    return dependencies;
  }

  private void filterDependencies (List dependencies, List features)
  {
    // TODO: check if newer version is installed.
    ArrayList copy = new ArrayList(dependencies);
    for (Iterator ii = copy.iterator(); ii.hasNext();){
      String[] dependency = (String[])ii.next();
      if(features.contains(dependency[1] + '_' + dependency[2])){
        dependencies.remove(dependency);
      }
    }
  }

  private List getInstalledFeatures ()
  {
    String eclipseHome = (String)
      Installer.getContext().getValue("eclipse.home");
    String features = FilenameUtils.concat(eclipseHome, "features");

    String[] results = new File(features).list(new FilenameFilter(){
      public boolean accept (File file, String name){
        return file.isDirectory();
      }
    });

    return Arrays.asList(results);
  }

  private void processLine (final String line)
  {
    SwingUtilities.invokeLater(new Runnable(){
      public void run (){
        if (line.startsWith(BEGIN_TASK)){
          String l = line.substring(BEGIN_TASK.length() + 2);
          double work = Double.parseDouble(
              l.substring(l.indexOf('=') + 1, l.indexOf(' ')));
          guiTaskProgress.setIndeterminate(false);
          guiTaskProgress.setMaximum((int)(work * 100d));
          guiTaskProgress.setValue(0);
        }else if(line.startsWith(SUB_TASK)){
          guiTaskLabel.setText(
              taskName + line.substring(SUB_TASK.length() + 1).trim());
        }else if(line.startsWith(INTERNAL_WORKED)){
          double worked = Double.parseDouble(
              line.substring(INTERNAL_WORKED.length() + 2));
          guiTaskProgress.setValue((int)(worked * 100d));
        }else if(line.startsWith(SET_TASK_NAME)){
          taskName = line.substring(SET_TASK_NAME.length() + 1).trim() + ' ';
        }
      }
    });
  }

  private class InstallProcess
    extends Thread
  {
    private Process process;
    private int returnCode;
    private String errorMessage;
    private String[] cmd;

    public InstallProcess (String[] dependency)
    {
      String eclipseHome = (String)
        Installer.getContext().getValue("eclipse.home");
      cmd = new String[12];
      cmd[0] = FilenameUtils.concat(eclipseHome, "eclipse");
      cmd[1] = "-nosplash";
      cmd[2] = "-application";
      cmd[3] = "org.eclim.installer.application";
      cmd[4] = "-command";
      cmd[5] = "install";
      cmd[6] = "-from";
      cmd[7] = dependency[0];
      cmd[8] = "-featureId";
      cmd[9] = dependency[1];
      cmd[10] = "-version";
      cmd[11] = dependency[2];
    }

    public void run ()
    {
      try{
        Runtime runtime = Runtime.getRuntime();
        process = runtime.exec(cmd);

        final ByteArrayOutputStream err = new ByteArrayOutputStream();

        Thread outThread = new Thread(){
          public void run (){
            try{
              BufferedReader reader = new BufferedReader(
                  new InputStreamReader(process.getInputStream()));
              String line = null;
              while((line = reader.readLine()) != null){
                processLine(line);
              }
            }catch(Exception e){
              e.printStackTrace();
              errorMessage = e.getMessage();
              returnCode = 1000;
              process.destroy();
            }
          }
        };
        outThread.start();

        Thread errThread = new Thread(){
          public void run (){
            try{
              IOUtils.copy(process.getErrorStream(), err);
            }catch(Exception e){
              e.printStackTrace();
            }
          }
        };
        errThread.start();

        returnCode = process.waitFor();
        outThread.join();
        errThread.join();

        if(errorMessage == null){
          errorMessage = err.toString();
        }
      }catch(Exception e){
        returnCode = 12;
        errorMessage = e.getMessage();
        e.printStackTrace();
      }
    }

    /**
     * Gets the returnCode for this instance.
     *
     * @return The returnCode.
     */
    public int getReturnCode ()
    {
      return this.returnCode;
    }

    /**
     * Gets the errorMessage for this instance.
     *
     * @return The errorMessage.
     */
    public String getErrorMessage ()
    {
      return this.errorMessage;
    }

    /**
     * Destroy this process.
     */
    public void destroy ()
    {
      if(process != null){
        process.destroy();
      }
    }
  }
}
