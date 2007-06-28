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

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;

import java.io.File;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import javax.swing.table.DefaultTableModel;

import foxtrot.Worker;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Untar;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.eclim.installer.EclipseFeatures;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.EnableCommand;
import org.eclim.installer.step.command.InstallCommand;
import org.eclim.installer.step.command.ListCommand;
import org.eclim.installer.step.command.OutputHandler;
import org.eclim.installer.step.command.UninstallCommand;
import org.eclim.installer.step.command.UpdateCommand;

import org.formic.Installer;

import org.formic.dialog.gui.GuiDialogs;

import org.formic.wizard.step.InstallStep;

/**
 * Step which installs necessary third party eclipse plugins.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class EclipsePluginsStep
  extends InstallStep
  implements OutputHandler
{
  private static final String BEGIN_TASK = "beginTask";
  private static final String SUB_TASK = "subTask";
  private static final String INTERNAL_WORKED = "internalWorked";
  private static final String SET_TASK_NAME = "setTaskName";
  private static final String FEATURE = "  Feature";
  private static final String ENABLED = "enabled";

  private static final String SKIP_FEATURES =
    "If you skip installing of the required features some " +
    "features of eclim may not work, or eclim may not start at all.\n" +
    "Are you sure you want to skip this step?";

  private String taskName = "";

  private JPanel stepPanel;
  private DefaultTableModel tableModel = new DefaultTableModel();

  /**
   * Constructs this step.
   */
  public EclipsePluginsStep (String name)
  {
    super(name);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#initGui()
   */
  public JComponent initGui ()
  {
    stepPanel = (JPanel)super.initGui();
    stepPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
    return stepPanel;
  }

  /**
   * Invoked when this step is displayed in the gui.
   */
  protected void displayedGui ()
  {
    setBusy(true);
    try{
      guiOverallLabel.setText("Analyzing installed features...");
      List dependencies = (List)Worker.post(new foxtrot.Task(){
        public Object run ()
          throws Exception
        {
          extractInstallerPlugin();
          List dependencies = getDependencies();
          filterDependencies(dependencies, getFeatures());
          return dependencies;
        }
      });

      if(dependencies.size() == 0){
        guiOverallProgress.setMaximum(1);
        guiOverallProgress.setValue(1);
        guiTaskProgress.setMaximum(1);
        guiTaskProgress.setValue(1);
      }else{
        tableModel.addColumn("Feature");
        tableModel.addColumn("Version");
        tableModel.addColumn("Install / Upgrade");
        JTable table = new JTable(tableModel);
        table.setRowSelectionAllowed(false);
        final JPanel featuresPanel = new JPanel(new BorderLayout());
        featuresPanel.setAlignmentX(0.0f);
        JPanel container = new JPanel(new BorderLayout());
        container.add(table, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setAlignmentX(0.0f);

        for (int ii = 0; ii < dependencies.size(); ii++){
          Dependency dependency = (Dependency)dependencies.get(ii);
          tableModel.addRow(new Object[]{
            dependency.getId(),
            dependency.getVersion(),
            dependency.isUpgrade() ? "Upgrade" : "Install"
          });
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setAlignmentX(0.0f);

        JButton skipButton = new JButton(new AbstractAction("Skip"){
          public void actionPerformed (ActionEvent e){
            if(GuiDialogs.showConfirm(SKIP_FEATURES)){
              setValid(true);
            }
          }
        });
        final JButton installButton =
          new JButton(new InstallPluginsAction(dependencies, skipButton));

        buttons.add(installButton);
        buttons.add(skipButton);

        featuresPanel.add(buttons, BorderLayout.NORTH);
        featuresPanel.add(scrollPane, BorderLayout.CENTER);

        stepPanel.add(featuresPanel);
      }
      guiOverallLabel.setText("");
    }catch(Exception e){
      setError(e);
    }finally{
      setValid(false);
      setBusy(false);
      guiTaskProgress.setIndeterminate(false);
      Installer.getProject().removeBuildListener(this);
    }
  }

  /**
   * Extracts the eclipse plugin used to install eclipse features.
   */
  private void extractInstallerPlugin ()
    throws Exception
  {
    // extract eclipse installer plugin.
    String eclipseHome = (String)
      Installer.getContext().getValue("eclipse.home");
    String plugins = FilenameUtils.concat(eclipseHome, "plugins");
    String tar = Installer.getProject().replaceProperties(
        "${basedir}/org.eclim.installer.tar.gz");

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
        "${eclipse.home}/plugins/org.eclim.installer/bin/install")));
      chmod.setPerm("755");
      chmod.setProject(Installer.getProject());
      chmod.execute();
    }
  }

  /**
   * Gets a list of commands which need to be executed to install or upgrade the
   * supplied dependency.
   *
   * @param dependency The dependency to install or upgrade.
   * @return List of commands.
   */
  private List getCommands (Dependency dependency)
  {
    ArrayList list = new ArrayList();
    if(!dependency.isUpgrade()){
      list.add(new InstallCommand(this,
          dependency.getUrl(), dependency.getId(), dependency.getVersion()));
    }else{
      if(!dependency.getFeature().isEnabled()){
        list.add(new EnableCommand(this,
            dependency.getId(), dependency.getFeature().getVersion()));
      }
      list.add(new UpdateCommand(this,
          dependency.getId(), dependency.getVersion()));
      list.add(new UninstallCommand(this,
          dependency.getId(), dependency.getFeature().getVersion()));
    }
    return list;
  }

  /**
   * Gets a list of required dependencies based on the chosen set of eclim
   * features to be installed.
   *
   * @return List of dependencies.
   */
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

      // check if the enabled eclim feature has any dependencies.
      if(enabled.booleanValue() && properties.containsKey(name + ".features")){
        // check if dependencies are eclipse based
        if(properties.containsKey(name + ".eclipse")){
          EclipseFeatures eclipseFeatures = EclipseFeatures.getInstance(properties);
          String[] depends = StringUtils.split(
              properties.getProperty(name + ".features"), ',');
          String site = properties.getProperty(name + ".site");
          for (int jj = 0; jj < depends.length; jj++){
            dependencies.add(new Dependency(new String[]{
              site, depends[jj].trim(),
              eclipseFeatures.getVersion(depends[jj].trim())}));
          }

        }else{  // third party features.
          String[] depends = StringUtils.split(
              properties.getProperty(name + ".features"), ',');
          for (int jj = 0; jj < depends.length; jj++){
            String[] dependency = StringUtils.split(depends[jj]);
            dependencies.add(new Dependency(dependency));
          }
        }
      }
    }
    return dependencies;
  }

  /**
   * Gets a list of already installed features.
   *
   * @return List of features.
   */
  private List getFeatures ()
    throws Exception
  {
    final ArrayList features = new ArrayList();
    Command command = new ListCommand(new OutputHandler(){
      public void process (String line){
        if(line.startsWith(FEATURE)){
          String[] attrs = StringUtils.split(
            line.substring(FEATURE.length() + 2));
          features.add(new Feature(attrs[0], attrs[1], attrs[2].equals(ENABLED)));
        }
      }
    });
    try{
      command.start();
      command.join();
    }finally{
      command.destroy();
    }
    return features;
  }

  /**
   * Filters the supplied list of dependencies to determine which are already
   * installed or need to be upgraded.
   *
   * @param dependencies The original list of dependencies.
   * @param features The list of already installed features.
   */
  private void filterDependencies (final List dependencies, final List features)
    throws Exception
  {
    Collator collator = Collator.getInstance();

    for (Iterator ii = features.iterator(); ii.hasNext();){
      Feature feature = (Feature)ii.next();
      boolean installed = false;
      Dependency dependency = null;
      for (Iterator jj = dependencies.iterator(); jj.hasNext();){
        dependency = (Dependency)jj.next();
        if(feature.getId().equals(dependency.getId())){
          installed = true;
          break;
        }
      }

      // compare installed in dependency
      if (installed){
        int order = collator.compare(
            feature.getVersion(), dependency.getVersion());

        // if required or newer version installed, remove dependency.
        if(order >= 0){
          dependencies.remove(dependency);

        // need to upgrade the dependency
        }else{
          dependency.setUpgrade(true);
          dependency.setFeature(feature);
        }
      }
    }
  }

  public void process (final String line)
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

  private class InstallPluginsAction
    extends AbstractAction
  {
    private List dependencies;
    private JButton skipButton;

    public InstallPluginsAction (List dependencies, JButton skipButton)
    {
      super("Install Features");
      this.dependencies = dependencies;
      this.skipButton = skipButton;
    }

    public void actionPerformed (ActionEvent e)
    {
      ((JButton)e.getSource()).setEnabled(false);
      final List dependencies = this.dependencies;
      try{
        Worker.post(new foxtrot.Task(){
          public Object run ()
            throws Exception
          {
            guiOverallProgress.setMaximum(dependencies.size());
            guiOverallProgress.setValue(0);
            int index = 0;
            for (Iterator ii = dependencies.iterator(); ii.hasNext(); index++){
              Dependency dependency = (Dependency)ii.next();
              if(!dependency.isUpgrade()){
                guiOverallLabel.setText("Installing feature: " +
                    dependency.getId() + '-' + dependency.getVersion());
              }else{
                guiOverallLabel.setText("Updating feature: " +
                    dependency.getId() + '-' + dependency.getVersion());
              }

              List commands = getCommands(dependency);
              for (Iterator jj = commands.iterator(); jj.hasNext();){
                Command command = (Command)jj.next();
                try{
                  command.start();
                  command.join();
                  if(command.getReturnCode() != 0){
                    throw new RuntimeException(command.getErrorMessage());
                  }
                }finally{
                  command.destroy();
                }
              }

              try{
                Thread.sleep(1000);
              }catch(Exception ex){
              }
              tableModel.removeRow(0);
              guiOverallProgress.setValue(guiOverallProgress.getValue() + 1);
            }
            guiTaskLabel.setText("");
            guiTaskProgress.setValue(guiTaskProgress.getMaximum());
            guiOverallProgress.setValue(guiOverallProgress.getMaximum());

            return null;
          }
        });
        guiOverallProgress.setValue(guiOverallProgress.getMaximum());
        guiOverallLabel.setText(Installer.getString("install.done"));

        guiTaskProgress.setValue(guiTaskProgress.getMaximum());
        guiTaskLabel.setText(Installer.getString("install.done"));

        setBusy(false);
        setValid(true);
        guiTaskProgress.setIndeterminate(false);

        skipButton.setEnabled(false);
      }catch(Exception ex){
        setError(ex);
      }
    }
  }

  private static class Dependency
  {
    private String url;
    private String id;
    private String version;
    private String previousVersion;
    private boolean upgrade;
    private Feature feature;

    public Dependency (String[] attrs)
    {
      url = attrs[0];
      id = attrs[1];
      version = attrs[2];
    }

    public String getUrl () {
      return url;
    }

    public String getId () {
      return id;
    }

    public String getVersion () {
      return version;
    }

    public boolean isUpgrade () {
      return upgrade;
    }

    public void setUpgrade (boolean upgrade) {
      this.upgrade = upgrade;
    }

    public Feature getFeature () {
      return feature;
    }

    public void setFeature (Feature feature) {
      this.feature = feature;
    }
  }

  private static class Feature
  {
    private String id;
    private String version;
    private boolean enabled;

    public Feature (String id, String version, boolean enabled)
    {
      this.id = id;
      this.version = version;
      this.enabled = enabled;
    }

    public String getId () {
      return id;
    }

    public String getVersion () {
      return version;
    }

    public boolean isEnabled () {
      return enabled;
    }
  }
}
