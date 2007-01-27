package org.eclim.installer.step;

import java.awt.BorderLayout;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import foxtrot.Task;
import foxtrot.Worker;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.form.Validator;

import org.formic.form.validator.ValidatorBuilder;

import org.formic.util.CommandExecutor;

import org.formic.wizard.step.FileChooserStep;

/**
 * Step for choosing the vimfiles directory to install vim scripts in.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class VimStep
  extends FileChooserStep
{
  private static final String[] WINDOWS_VIMS = {
    "C:/Program Files/Vim/vim70/vim.exe",
    "C:/Program Files/Vim/vim70/gvim.exe"
  };

  private static final String[] UNIX_VIMS = {"vim", "gvim"};

  private static final String COMMAND =
    "redir! > <file> | silent! echo &rtp | quit";

  private static final String ICON = "/resources/images/vim.png";

  private JPanel panel;
  private boolean rtpAttempted;

  /**
   * Constructs the step.
   */
  public VimStep (String name)
  {
    super(name);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#initProperties(Properties)
   */
  public void initProperties (Properties properties)
  {
    properties.put(PROPERTY, "files");
    properties.put("selectionMode", "directories");
    super.initProperties(properties);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#initGui()
   */
  public JComponent initGui ()
  {
    panel = new JPanel(new BorderLayout());
    panel.add(super.initGui(), BorderLayout.NORTH);

    return panel;
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#displayed()
   */
  public void displayed ()
  {
    if(!rtpAttempted){
      rtpAttempted = true;

      setBusy(true);
      try{
        Worker.post(new Task(){
          public Object run ()
            throws Exception
          {
            String[] rtp = getVimRuntimePath();
            if(rtp != null && rtp.length > 0){
              if(rtp.length == 1){
                getGuiFileChooser().getTextField().setText(rtp[0]);
              }else{
                final JList list = new JList(rtp);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                JScrollPane scrollPane = new JScrollPane(list);
                panel.add(scrollPane, BorderLayout.CENTER);

                list.addListSelectionListener(new ListSelectionListener(){
                  public void valueChanged (ListSelectionEvent event){
                    if(!event.getValueIsAdjusting()){
                      getGuiFileChooser().getTextField()
                        .setText((String)list.getSelectedValue());
                    }
                  }
                });

                list.setSelectedIndex(0);
              }
            }
            return null;
          }
        });
      }catch(Exception e){
        e.printStackTrace();
      }
      setBusy(false);
      getGuiFileChooser().getTextField().grabFocus();
    }
  }

  /**
   * {@inheritDoc}
   * @see AbstractStep#getIconPath()
   */
  protected String getIconPath ()
  {
    return ICON;
  }

  /**
   * {@inheritDoc}
   * @see FileChooserStep#getValidator()
   */
  protected Validator getValidator ()
  {
    ValidatorBuilder builder = new ValidatorBuilder();
    builder.validator(super.getValidator()).isDirectory().fileExists();
    return builder.validator();
  }

  /**
   * Attempts to determine available paths in vim's runtime path.
   *
   * @return Array of paths or null if unable to determine any.
   */
  private String[] getVimRuntimePath ()
  {
    try{
      File tempFile = File.createTempFile("eclim_installer", null);
      String command = COMMAND.replaceFirst("<file>",
          tempFile.getAbsolutePath().replace('\\', '/').replaceAll(" ", "\\ "));

      String[] vims = null;
      if(Os.isFamily("windows")){
        vims = WINDOWS_VIMS;
      }else{
        vims = UNIX_VIMS;
      }

      String[] args = {null, "-X", "-u", "NONE", "-U", "NONE", "--cmd", command};
      for (int ii = 0; ii < vims.length; ii++){
        args[0] = vims[ii];
        CommandExecutor executor = CommandExecutor.execute(args, 5000);
        if(executor.getReturnCode() == 0){
          return parseVimRuntimePathResults(tempFile);
        }
        executor.destroy();
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Parses the results of echoing vim runtime path to a file.
   *
   * @param file The file containing the results.
   * @return The results.
   */
  private String[] parseVimRuntimePathResults (File file)
  {
    FileInputStream in = null;
    try{
      String contents = IOUtils.toString(in = new FileInputStream(file));
      String[] paths = StringUtils.stripAll(StringUtils.split(contents, ','));
      ArrayList results = new ArrayList();
      for (int ii = 0; ii < paths.length; ii++){
        File path = new File(paths[ii]);
        if(path.isDirectory() && path.canWrite()){
          results.add(paths[ii]);
        }
      }

      return (String[])results.toArray(new String[results.size()]);
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
