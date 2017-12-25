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
package org.eclim.installer.step;

import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.BuildException;

import org.apache.tools.ant.taskdefs.Delete;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.apache.tools.ant.types.FileSet;

import org.formic.InstallContext;
import org.formic.Installer;

import org.formic.util.CommandExecutor;
import org.formic.util.File;

import org.formic.util.dialog.gui.GuiDialogs;

import org.formic.wizard.form.GuiForm;
import org.formic.wizard.form.Validator;

import org.formic.wizard.form.gui.component.FileChooser;

import org.formic.wizard.form.validator.ValidatorBuilder;

import org.formic.wizard.step.AbstractGuiStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import foxtrot.Task;
import foxtrot.Worker;

import net.miginfocom.swing.MigLayout;

/**
 * Step for choosing the vimfiles directory to install vim scripts in.
 *
 * @author Eric Van Dewoestine
 */
public class VimStep
  extends AbstractGuiStep
{
  private static final Logger logger = LoggerFactory.getLogger(VimStep.class);

  private static final String[] WINDOWS_VIMS = {
    "vim.bat",
    "gvim.bat",
    "vim.exe",
    "gvim.exe",
    "C:/Program Files (x86)/Vim/vim73/vim.exe",
    "C:/Program Files (x86)/Vim/vim73/gvim.exe",
    "C:/Program Files (x86)/Vim/vim72/vim.exe",
    "C:/Program Files (x86)/Vim/vim72/gvim.exe",
    "C:/Program Files/Vim/vim73/vim.exe",
    "C:/Program Files/Vim/vim73/gvim.exe",
    "C:/Program Files/Vim/vim72/vim.exe",
    "C:/Program Files/Vim/vim72/gvim.exe",
    "C:/Program Files/Vim/vim71/vim.exe",
    "C:/Program Files/Vim/vim71/gvim.exe",
    "C:/Program Files/Vim/vim70/vim.exe",
    "C:/Program Files/Vim/vim70/gvim.exe",
  };

  private static final String[] WINDOWS_GVIMS = {
    "C:/Program Files (x86)/Vim/vim73/gvim.exe",
    "C:/Program Files (x86)/Vim/vim72/gvim.exe",
    "C:/Program Files/Vim/vim73/gvim.exe",
    "C:/Program Files/Vim/vim72/gvim.exe",
    "C:/Program Files/Vim/vim71/gvim.exe",
    "C:/Program Files/Vim/vim70/gvim.exe",
  };

  private static final String[] UNIX_VIMS = {"vim", "gvim"};

  private static final String COMMAND =
    "redir! > <file> | silent! echo &rtp | quit";

  private JPanel panel;
  private FileChooser fileChooser;
  private JList dirList;
  private JCheckBox skipCheckBox;
  private boolean rtpAttempted;
  private boolean homeVimCreatePrompted;
  private String[] runtimePath;

  /**
   * Constructs the step.
   */
  public VimStep(String name, Properties properties)
  {
    super(name, properties);
  }

  @Override
  public Component init()
  {
    GuiForm form = createForm();

    String files = fieldName("files");
    fileChooser = new FileChooser(JFileChooser.DIRECTORIES_ONLY);

    // allow just .vim dirs to not be hidden
    fileChooser.getFileChooser().setFileHidingEnabled(false);
    fileChooser.getFileChooser().addChoosableFileFilter(new FileFilter(){
      public boolean accept(java.io.File f) {
        String path = f.getAbsolutePath();
        return f.isDirectory() && (
          path.matches(".*/\\.vim(/.*|$)") ||
          !path.matches(".*/\\..*"));
      }
      public String getDescription() {
        return null;
      }
    });

    String skip = fieldName("skip");
    skipCheckBox = new JCheckBox(Installer.getString(skip));
    skipCheckBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        boolean selected = ((JCheckBox)e.getSource()).isSelected();
        JTextField fileField = fileChooser.getTextField();
        fileField.setEnabled(!selected);
        fileChooser.getButton().setEnabled(!selected);

        if (dirList != null){
          dirList.setEnabled(!selected);
        }

        // hacky
        Validator validator = (Validator)
          fileField.getClientProperty("validator");
        setValid(selected || validator.isValid(fileField.getText()));
      }
    });

    panel = new JPanel(new MigLayout(
          "wrap 2", "[fill]", "[] [] [] [fill, grow]"));
    panel.add(form.createMessagePanel(), "span");
    panel.add(new JLabel(Installer.getString(files)), "split");
    panel.add(fileChooser, "skip");
    panel.add(skipCheckBox, "span");

    form.bind(files,
        fileChooser.getTextField(),
        new ValidatorBuilder()
          .required()
          .isDirectory()
          .fileExists()
          .isWritable()
          .validator());

    return panel;
  }

  @Override
  public void displayed()
  {
    if(!rtpAttempted){
      rtpAttempted = true;

      setBusy(true);
      try{
        runtimePath = (String[])Worker.post(new Task(){
          public Object run() throws Exception {
            setGvimProperty();
            return getVimRuntimePath();
          }
        });

        // filter out dirs the user doesn't have permission write to.
        ArrayList<String> filtered = new ArrayList<String>();
        if (runtimePath != null){
          for (String path : runtimePath){
            if (new File(path).canWrite()){
              if (Installer.isUninstall()){
                File eclimDir = new File(path + "/eclim");
                if (eclimDir.exists()){
                  if (eclimDir.canWrite()){
                    filtered.add(path);
                  }else{
                    logger.warn(
                        path + "/eclim is not writable by the current user");
                  }
                }
              }else{
                filtered.add(path);
              }
            }
          }
        }
        String[] rtp = filtered.toArray(new String[filtered.size()]);

        if(rtp == null || rtp.length == 0){
          if(!Installer.isUninstall()){
            if(!homeVimCreatePrompted){
              createUserVimFiles("No suitable vim files directory found.");
            }else{
              GuiDialogs.showWarning(
                  "Your vim install is still reporting no\n" +
                  "suitable vim files directories.\n" +
                  "You will need to manually specify one.");
            }
          }
        }else{
          if(rtp.length == 1){
            fileChooser.getTextField().setText(rtp[0]);

            // try to discourage windows users from installing eclim files in
            // their vim installation.
            if(new File(rtp[0] + "/gvim.exe").exists()){
              createUserVimFiles("No user vim files directory found.");
            }
          }else{
            dirList = new JList(rtp);
            dirList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(dirList);
            panel.add(scrollPane, "span, grow");

            dirList.addListSelectionListener(new ListSelectionListener(){
              public void valueChanged(ListSelectionEvent event){
                if(!event.getValueIsAdjusting()){
                  fileChooser.getTextField()
                    .setText((String)dirList.getSelectedValue());
                }
              }
            });

            dirList.setSelectedIndex(0);
          }
        }
      }catch(Exception e){
        e.printStackTrace();
      }
      setBusy(false);
      fileChooser.getTextField().grabFocus();
    }
  }

  @Override
  public boolean proceed()
  {
    boolean proceed = super.proceed();
    if (proceed){
      InstallContext context = Installer.getContext();
      context.setValue("vim.skip", Boolean.valueOf(skipCheckBox.isSelected()));
      String vimfiles = (String)context.getValue("vim.files");

      if (vimfiles != null){
        vimfiles = vimfiles.replace('\\', '/');
        context.setValue("vim.files", vimfiles);

        // Check if the user has the eclim vim files already installed in
        // another directory in their vim's runtime path.

        // on windows, since case is insensitive, lower the path.
        if (Os.isFamily(Os.FAMILY_WINDOWS)){
          vimfiles = vimfiles.toLowerCase();
        }

        if(runtimePath != null && runtimePath.length > 0){
          for (String rpath : runtimePath){
            String path = rpath;
            if (Os.isFamily(Os.FAMILY_WINDOWS)){
              path = path.toLowerCase();
            }
            if (vimfiles.equals(path)){
              continue;
            }

            File fpath = new File(path + "/plugin/eclim.vim");
            if (!fpath.exists()){
              continue;
            }

            if (fpath.canWrite()){
              boolean remove = GuiDialogs.showConfirm(
                  "You appear to have one or more of the eclim vim files\n" +
                  "installed in another directory:\n" +
                  "  " + rpath + "\n" +
                  "Would you like the installer to remove those files now?");
              if (remove){
                Delete delete = new Delete();
                delete.setProject(Installer.getProject());
                delete.setTaskName("delete");
                delete.setIncludeEmptyDirs(true);
                delete.setFailOnError(true);

                FileSet set = new FileSet();
                set.setDir(new File(path + "/eclim"));
                set.createInclude().setName("**/*");
                set.createExclude().setName("after/**/*");
                set.createExclude().setName("resources/**/*");
                delete.addFileset(set);

                try{
                  boolean deleted = fpath.delete();
                  if (!deleted){
                    throw new BuildException(
                        "Failed to delete file: plugin/eclim.vim");
                  }
                  delete.execute();
                }catch(BuildException be){
                  GuiDialogs.showError(
                      "Failed to delete old eclim vim files:\n" +
                      "  " + be.getMessage() + "\n" +
                      "You may continue with the installation, but if old eclim\n" +
                      "vim files remain, chances are that you will receive\n" +
                      "errors upon starting (g)vim and the older version of\n" +
                      "the files may take precedence over the ones you are\n" +
                      "installing now, leading to indeterminate behavior.");
                }
              }
              proceed = remove;
            }else{
              GuiDialogs.showWarning(
                  "You appear to have one or more of the eclim vim files\n" +
                  "installed in another directory:\n" +
                  "  " + rpath + "\n" +
                  "Unfortunately it seems you do not have write access to\n" +
                  "that directory. You may continue with the installation,\n" +
                  "but chances are that you will receive errors upon starting\n" +
                  "(g)vim and the older version of the files may take precedence\n" +
                  "over the ones you are installing now, leading to indeterminate\n" +
                  "behavior.");
            }
          }
        }
      }
    }
    return proceed;
  }

  /**
   * Attempt to find where gvim is installed.
   */
  private void setGvimProperty()
  {
    try{
      String[] gvims = null;
      if(Os.isFamily(Os.FAMILY_WINDOWS)){
        gvims = WINDOWS_GVIMS;
        for (String gvim : gvims){
          if (new File(gvim).isFile()){
            Installer.getProject().setProperty("eclim.gvim", gvim);
            break;
          }
        }
      }else{
        String vim = Os.isFamily(Os.FAMILY_MAC) ? "mvim" : "gvim";
        CommandExecutor executor =
          CommandExecutor.execute(new String[]{"which", vim}, 1000);
        if(executor.getReturnCode() == 0){
          String result = executor.getResult().trim();
          logger.info("which " + vim + ": " + result);
          Installer.getProject().setProperty("eclim.gvim", result);
        }else{
          logger.info("which " + vim + ':' +
              " out=" + executor.getResult() +
              " err=" + executor.getErrorMessage());
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Prompt the user to create the standard user local vim files directory.
   *
   * @param message The message indicating the primary reason we're asking them
   * if they want to create the user local directory.
   */
  private void createUserVimFiles(String message)
  {
    homeVimCreatePrompted = true;
    File vimfiles = new File(
        System.getProperty("user.home") + '/' +
        (Os.isFamily(Os.FAMILY_WINDOWS) ? "vimfiles" : ".vim"));
    System.out.println(
        "Checking for user vim files directory: " + vimfiles);
    if(!vimfiles.exists()){
      boolean create = GuiDialogs.showConfirm(
          message + "\n" +
          "Would you like to create the standard\n" +
          "directory for your system?\n" +
          vimfiles);
      if(create){
        boolean created = vimfiles.mkdir();
        if(created){
          rtpAttempted = false;
          displayed();
        }else{
          GuiDialogs.showError("Unable to create directory: " + vimfiles);
        }
      }
    }else{
      fileChooser.getTextField().setText(
          vimfiles.getAbsolutePath().replace('\\', '/'));
    }
  }

  /**
   * Attempts to determine available paths in vim's runtime path.
   *
   * @return Array of paths or null if unable to determine any.
   */
  private String[] getVimRuntimePath()
  {
    try{
      java.io.File tempFile = File.createTempFile("eclim_installer", null);
      String command = COMMAND.replaceFirst("<file>",
          tempFile.getAbsolutePath().replace('\\', '/').replaceAll(" ", "\\ "));

      String[] vims = null;
      if(Os.isFamily(Os.FAMILY_WINDOWS)){
        vims = WINDOWS_VIMS;
      }else{
        vims = UNIX_VIMS;
      }

      String[] args = {
        null, "-f", "-X",
        "-u", "NONE", "-U", "NONE",
        "--cmd", command,
      };
      for (int ii = 0; ii < vims.length; ii++){
        args[0] = vims[ii];
        CommandExecutor executor = CommandExecutor.execute(args, 5000);
        if(executor.getReturnCode() == 0){
          return parseVimRuntimePathResults(tempFile);
        }
        if (executor.isShutdown()){
          return null;
        }
        executor.destroy();
      }
    }catch(Exception e){
      GuiDialogs.showError("Error determining your vim runtime path.", e);
    }

    return null;
  }

  /**
   * Parses the results of echoing vim runtime path to a file.
   *
   * @param file The file containing the results.
   * @return The results.
   */
  private String[] parseVimRuntimePathResults(java.io.File file)
  {
    FileInputStream in = null;
    try{
      String contents = IOUtils.toString(in = new FileInputStream(file));
      String[] paths = StringUtils.stripAll(StringUtils.split(contents, ','));
      ArrayList<String> results = new ArrayList<String>();
      for (String path : paths){
        if(new File(path).isDirectory()){
          results.add(path.replace('\\', '/'));
        }
      }
      return results.toArray(new String[results.size()]);
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      IOUtils.closeQuietly(in);
      file.deleteOnExit();
      file.delete();
    }
    return null;
  }
}
