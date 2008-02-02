/**
 * Copyright (c) 2005 - 2008
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

import java.awt.BorderLayout;
import java.awt.Component;

import java.io.File;

import java.util.ArrayList;
import java.util.Properties;

import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import foxtrot.Task;
import foxtrot.Worker;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Untar;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.util.CommandExecutor;

import org.formic.wizard.form.GuiForm;

import org.formic.wizard.form.gui.component.FileChooser;

import org.formic.wizard.form.validator.ValidatorBuilder;

import org.formic.wizard.step.AbstractGuiStep;

/**
 * Step for choosing the location of the python interpreter.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class PythonInterpreterStep
  extends AbstractGuiStep
{
  private static final String[] WINDOWS_INTERPRETERS = {
    "C:/Program Files/Python25/python.exe",
    "C:/Program Files/Python24/python.exe",
    "C:/Program Files/Python/Python25/python.exe",
    "C:/Program Files/Python/Python24/python.exe"
  };

  private static final String[] UNIX_INTERPRETERS = {"python"};

  private JPanel panel;
  private FileChooser fileChooser;
  private boolean firstDisplay = true;
  private String[] pydevInterpreters;

  /**
   * Constructs the step.
   */
  public PythonInterpreterStep (String name, Properties properties)
  {
    super(name, properties);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.step.GuiStep#init()
   */
  public Component init ()
  {
    GuiForm form = createForm();
    String interpreter = fieldName("interpreter");
    fileChooser = new FileChooser(JFileChooser.FILES_ONLY);

    panel = new JPanel();
    panel.setLayout(new MigLayout("wrap 2"));
    panel.add(form.createMessagePanel(), "span");
    panel.add(new JLabel(Installer.getString(interpreter)));
    panel.add(fileChooser, "width 300!");

    form.bind(interpreter, fileChooser.getTextField(),
        new ValidatorBuilder().required().isFile().fileExists().validator());

    return panel;
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#displayed()
   */
  public void displayed ()
  {
    if(firstDisplay){
      firstDisplay = false;
      setBusy(true);
      try{
        Worker.post(new Task(){
          public Object run ()
            throws Exception
          {
            extractInstallerPlugin();
            String interpreter = null;

            // see if pydev already has an interpreter already set.
            pydevInterpreters = null; //listInterpreters();
            if(pydevInterpreters != null && pydevInterpreters.length > 0){
              interpreter = pydevInterpreters[0];
              if(pydevInterpreters.length > 1){
                JPanel interpreters = new JPanel(new BorderLayout());
                interpreters.add(new JLabel("All PyDev configured interpreters:"),
                  BorderLayout.NORTH);
                JList list = new JList(pydevInterpreters);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                JScrollPane scrollPane = new JScrollPane(list);
                interpreters.add(scrollPane, BorderLayout.CENTER);
                panel.add(interpreters, BorderLayout.CENTER);
              }
            }else{
              // pydev interpreter not yet set, so attempt to find one at common
              // locations.
              if(Os.isFamily("windows")){
                for (int ii = 0; ii < WINDOWS_INTERPRETERS.length; ii++){
                  if(new File(WINDOWS_INTERPRETERS[ii]).exists()){
                    interpreter = WINDOWS_INTERPRETERS[ii];
                    break;
                  }
                }
              }else{
                for (int ii = 0; ii < UNIX_INTERPRETERS.length; ii++){
                  String path = which(UNIX_INTERPRETERS[ii]);
                  if(path != null){
                    interpreter = path;
                    break;
                  }
                }
              }
            }

            if(interpreter != null) {
              fileChooser.getTextField().setText(interpreter);
            }
            return null;
          }
        });
      }catch(Exception e){
        e.printStackTrace();
      }
      setBusy(false);
      fileChooser.getTextField().grabFocus();
    }
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#displayed()
   */
  public void proceed ()
  {
    try{
      setBusy(true);
      Worker.post(new Task(){
        public Object run ()
          throws Exception
        {
          String chosen = fileChooser.getTextField().getText();
          boolean set = true;
          if(pydevInterpreters != null){
            for (int ii = 0; ii < pydevInterpreters.length; ii++){
              if(chosen.equals(pydevInterpreters[ii])){
                set = false;
                break;
              }
            }
          }

          // set user chosen interpreter.
          if(set){
            setInterpreter(chosen);
          }
          return null;
        }
      });
    }catch(Exception e){
      e.printStackTrace();
    }
    setBusy(false);
  }

  private void extractInstallerPlugin ()
  {
    // extract eclipse installer plugin.
    String eclipseHome = (String)
      Installer.getContext().getValue("eclipse.home");
    String plugins = FilenameUtils.concat(eclipseHome, "plugins");
    String tar = Installer.getProject().replaceProperties(
        "${basedir}/org.eclim.installer.pydev.tar.gz");

    Untar untar = new Untar();
    untar.setTaskName("untar");
    untar.setDest(new File(plugins));
    untar.setSrc(new File(tar));
    Untar.UntarCompressionMethod compression =
      new Untar.UntarCompressionMethod();
    compression.setValue("gzip");
    untar.setCompression(compression);
    untar.setProject(Installer.getProject());
    untar.execute();

    // on unix based systems, chmod the install sh file.
    if (Os.isFamily("unix")){
      Chmod chmod = new Chmod();
      chmod.setTaskName("chmod");
      chmod.setFile(new File(Installer.getProject().replaceProperties(
        "${eclipse.home}/plugins/org.eclim.installer.pydev/bin/install")));
      chmod.setPerm("755");
      chmod.setProject(Installer.getProject());
      chmod.execute();
    }
  }

  /**
   * Get array of pydev configured interpreters.
   *
   * @return Array of interpreters.
   */
  private String[] listInterpreters ()
  {
    try{
      String[] cmd = new String[2];
      cmd[0] = Installer.getProject().replaceProperties(
          "${eclipse.home}/plugins/org.eclim.installer.pydev/bin/install");
      if (Os.isFamily("windows")){
        cmd[0] += ".bat";
      }
      cmd[1] = "list";

      CommandExecutor executor = CommandExecutor.execute(cmd, 10 * 1000);
      ArrayList interpreters = new ArrayList();
      Pattern pattern = Pattern.compile("^(/|\\w:).*");
      if(executor.getReturnCode() == 0){
        String[] lines = StringUtils.split(executor.getResult().trim(), '\n');
        // this block is used to filter out any errors that may occur when
        // trying to list interpreters in pydev.
        for (int ii = 0; ii < lines.length; ii++){
          String line = lines[ii];
          if (pattern.matcher(line).matches()){
            interpreters.add(line);
          }
        }
      }
      executor.destroy();
      return (String[])interpreters.toArray(new String[interpreters.size()]);
    }catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Sets the pydev interpreter.
   *
   * @return Array of interpreters.
   */
  private void setInterpreter (String interpreter)
  {
    try{
      String[] cmd = new String[3];
      cmd[0] = Installer.getProject().replaceProperties(
          "${eclipse.home}/plugins/org.eclim.installer.pydev/bin/install");
      if (Os.isFamily("windows")){
        cmd[0] += ".bat";
      }
      cmd[1] = "set";
      cmd[2] = interpreter;

      CommandExecutor executor = CommandExecutor.execute(cmd);
      executor.destroy();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Performs a 'which' on the supplied command and returns the resulting path
   * or null if not found on the path.
   *
   * @param cmd The command.
   * @return The path or null if not found.
   */
  private String which (String cmd)
  {
    try{
      CommandExecutor executor = CommandExecutor.execute(
          new String[]{"which", cmd}, 2000);
      if(executor.getReturnCode() == 0){
        return executor.getResult().trim();
      }
      executor.destroy();
    }catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }
}
