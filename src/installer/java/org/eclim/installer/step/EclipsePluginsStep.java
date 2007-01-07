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
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;

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
    InstallProcess process = new InstallProcess();
    try{
      process.start();
      process.join();
      if(process.getReturnCode() != 0){
        throw new RuntimeException(process.getErrorMessage());
      }
    }finally{
      process.destroy();
    }
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

    public void run ()
    {
      try{
        Runtime runtime = Runtime.getRuntime();
        process = runtime.exec("cat /home/ervandew/output.txt");

        final ByteArrayOutputStream err = new ByteArrayOutputStream();

        Thread outThread = new Thread(){
          public void run (){
            try{
              BufferedReader reader = new BufferedReader(
                  new InputStreamReader(process.getInputStream()));
              String line = null;
              while((line = reader.readLine()) != null){
                processLine(line);
Thread.sleep(100);
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
