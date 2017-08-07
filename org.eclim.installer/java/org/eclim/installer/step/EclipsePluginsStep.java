/**
 * Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.InstallCommand;
import org.eclim.installer.step.command.ListCommand;
import org.eclim.installer.step.command.OutputHandler;

import org.eclim.installer.theme.DesertBlue;

import org.formic.Installer;

import org.formic.util.dialog.gui.GuiDialogs;

import org.formic.wizard.step.gui.InstallStep;

import com.jgoodies.looks.plastic.PlasticTheme;

import foxtrot.Worker;

/**
 * Step which installs necessary third party eclipse plugins.
 *
 * @author Eric Van Dewoestine
 */
public class EclipsePluginsStep
  extends InstallStep
  implements OutputHandler
{
  private static final String BEGIN_TASK = "beginTask";
  private static final String PREPARE_TASK = "prepare";
  private static final String SUB_TASK = "subTask";
  private static final String INTERNAL_WORKED = "worked";
  private static final String SET_TASK_NAME = "setTaskName";

  private static final Color ERROR_COLOR = new Color(255, 201, 201);

  private String taskName = "";

  private JPanel stepPanel;
  private JPanel featuresPanel;
  private JLabel messageLabel;
  private ImageIcon errorIcon;
  private DefaultTableModel tableModel;
  private List<Dependency> dependencies;
  private Map<String,String> availableFeatures;
  private PlasticTheme theme;
  private int overallProgressStep;

  /**
   * Constructs this step.
   */
  public EclipsePluginsStep(String name, Properties properties)
  {
    super(name, properties);
  }

  @Override
  public Component init()
  {
    theme = new DesertBlue();
    stepPanel = (JPanel)super.init();
    stepPanel.setBorder(null);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    messageLabel = new JLabel();
    messageLabel.setPreferredSize(new Dimension(25, 25));
    panel.add(messageLabel);
    panel.add(stepPanel);
    panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 10, 25));

    return panel;
  }

  private void setMessage(String message)
  {
    if(errorIcon == null){
      errorIcon = new ImageIcon(Installer.getImage("form.error.icon"));
    }
    if(message != null){
      messageLabel.setIcon(errorIcon);
      messageLabel.setText(message);
    }else{
      messageLabel.setIcon(null);
      messageLabel.setText(null);
    }
  }

  @Override
  public void displayed()
  {
    setBusy(true);
    setPreviousEnabled(false);

    try{
      overallLabel.setText("");
      overallProgress.setValue(0);
      taskLabel.setText("");
      taskProgress.setValue(0);
      taskProgress.setIndeterminate(true);

      // handle step re-entry.
      if (featuresPanel != null){
        stepPanel.remove(featuresPanel);
      }

      EclipseInfo info = (EclipseInfo)
        Installer.getContext().getValue("eclipse.info");

      // find chosen features dependencies which need to be installed/upgraded.
      dependencies = unsatisfiedDependencies(info);

      if(dependencies.size() == 0){
        overallProgress.setMaximum(1);
        overallProgress.setValue(1);
        overallLabel.setText("All third party plugins are up to date.");
        taskProgress.setMaximum(1);
        taskProgress.setValue(1);
        taskLabel.setText("");
      }else{
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Feature");
        tableModel.addColumn("Version");
        tableModel.addColumn("Install / Upgrade");
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new DependencyCellRenderer());
        table.getSelectionModel().addListSelectionListener(
            new DependencySelectionListener());

        featuresPanel = new JPanel(new BorderLayout());
        featuresPanel.setAlignmentX(0.0f);

        JPanel container = new JPanel(new BorderLayout());
        container.add(table, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setAlignmentX(0.0f);

        availableFeatures = loadAvailableFeatures();
        for (Dependency dependency : dependencies){
          String version = availableFeatures.get(dependency.getId());
          String manual = "";
          if (version == null){
            manual = " (Manual)";
            version = dependency.getRequiredVersion();
          }
          tableModel.addRow(new Object[]{
            dependency.getId(),
            version,
            (dependency.isUpgrade() ? "Upgrade" : "Install") + manual,
          });
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setAlignmentX(0.0f);

        JButton skipButton = new JButton(new SkipPluginsAction());
        JButton installButton =
          new JButton(new InstallPluginsAction(skipButton));

        buttons.add(installButton);
        buttons.add(skipButton);

        featuresPanel.add(scrollPane, BorderLayout.CENTER);
        featuresPanel.add(buttons, BorderLayout.SOUTH);

        stepPanel.add(featuresPanel);
        overallProgress.setValue(0);
        overallLabel.setText("");
        taskProgress.setValue(0);
        taskLabel.setText("");
      }
    }catch(Exception e){
      setError(e);
    }finally{
      setValid(dependencies != null && dependencies.size() == 0);
      setBusy(false);
      setPreviousEnabled(true);
      taskProgress.setIndeterminate(false);
    }
  }

  public void process(final String line)
  {
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        if (line.startsWith(BEGIN_TASK)){
          String l = line.substring(BEGIN_TASK.length() + 2);
          double work = Double.parseDouble(
              l.substring(l.indexOf('=') + 1, l.indexOf(' ')));
          taskProgress.setIndeterminate(false);
          taskProgress.setMaximum((int)work);
          taskProgress.setValue(0);
        }else if(line.startsWith(PREPARE_TASK)){
          taskLabel.setText(line.substring(PREPARE_TASK.length() + 1).trim());
        }else if(line.startsWith(SUB_TASK)){
          taskLabel.setText(
              taskName + line.substring(SUB_TASK.length() + 1).trim());
        }else if(line.startsWith(INTERNAL_WORKED)){
          double worked = Double.parseDouble(
              line.substring(INTERNAL_WORKED.length() + 2));
          taskProgress.setValue((int)worked);
          overallProgress.setValue(
            overallProgressStep + (int)(taskProgress.getPercentComplete() * 100));
        }else if(line.startsWith(SET_TASK_NAME)){
          taskName = line.substring(SET_TASK_NAME.length() + 1).trim() + ' ';
        }
      }
    });
  }

  private List<Dependency> unsatisfiedDependencies(EclipseInfo info)
  {
    List<Dependency> deps = new ArrayList<Dependency>();
    String[] features = Installer.getContext().getKeysByPrefix("featureList");
    Arrays.sort(features, new FeatureNameComparator());
    for (int ii = 0; ii < features.length; ii++){
      Boolean enabled = (Boolean)Installer.getContext().getValue(features[ii]);
      if (enabled.booleanValue()){
        String name = features[ii].substring(features[ii].indexOf('.') + 1);
        deps.addAll(info.getUnsatisfiedDependencies(name));
      }
    }
    return deps;
  }

  @SuppressWarnings("unchecked")
  private Map<String,String> loadAvailableFeatures()
    throws Exception
  {
    // load up available features from update sites.
    overallProgress.setMaximum(1);
    overallLabel.setText("Loading available features from update sites...");

    return (Map<String,String>)Worker.post(new foxtrot.Task(){
      public Object run()
        throws Exception
      {
        final Map<String,String> availableFeatures = new HashMap<String,String>();

        ArrayList<String> sites = new ArrayList<String>();
        for (Dependency dependency : dependencies){
          String site = dependency.getSite();
          if (site == null){
            availableFeatures.put(
              dependency.getId(), dependency.getRequiredVersion());
          }else if (!sites.contains(site)){
            sites.add(site);
          }
        }

        OutputHandler handler = new OutputHandler(){
          public void process(String line){
            String[] parts = StringUtils.split(line, "=");
            if (parts.length == 2 && parts[0].endsWith(".feature.group")){
              availableFeatures.put(
                parts[0].replace(".feature.group", ""), parts[1]);
            }
          }
        };
        if (sites.size() > 0){
          Command command = new ListCommand(
            handler, sites.toArray(new String[sites.size()]));
          try{
            command.start();
            command.join();
            if(command.getReturnCode() != 0){
              throw new RuntimeException(
                  "error: " + command.getErrorMessage() +
                  " out: " + command.getResult());
            }
          }finally{
            command.destroy();
          }
        }
        return availableFeatures;
      }
    });
  }

  private class InstallPluginsAction
    extends AbstractAction
  {
    private static final long serialVersionUID = 1L;

    private JButton skipButton;

    public InstallPluginsAction(JButton skipButton)
    {
      super("Install Features");
      this.skipButton = skipButton;
    }

    public void actionPerformed(ActionEvent e)
    {
      ((JButton)e.getSource()).setEnabled(false);
      setPreviousEnabled(true);
      try{
        Boolean successful = (Boolean)Worker.post(new foxtrot.Task(){
          public Object run()
            throws Exception
          {
            // check if any of the features cannot be installed.
            for (Dependency dependency : dependencies){
              Feature feature = dependency.getFeature();
              if (feature != null) {
                if (feature.getSite() == null){
                  String installLog = FilenameUtils.concat(
                    SystemUtils.JAVA_IO_TMPDIR, "install.log");
                  GuiDialogs.showWarning(Installer.getString(
                      "eclipsePlugins.install.features.site.not.found", installLog));
                  return Boolean.FALSE;
                }else if (!feature.getSite().canWrite()){
                  GuiDialogs.showWarning(Installer.getString(
                      "eclipsePlugins.install.features.permission.denied"));
                  return Boolean.FALSE;
                }
              }
            }

            int installCount = 0;
            for (Iterator<Dependency> ii = dependencies.iterator(); ii.hasNext();){
              Dependency dependency = ii.next();
              if (dependency.getSite() != null){
                installCount++;
              }
            }

            overallProgress.setMaximum(installCount * 100);
            overallProgress.setValue(0);

            int removeIndex = 0;
            for (Iterator<Dependency> ii = dependencies.iterator(); ii.hasNext();){
              Dependency dependency = ii.next();
              String site = dependency.getSite();
              String version = availableFeatures.get(dependency.getId());
              if (site != null && version != null){
                if(!dependency.isUpgrade()){
                  overallLabel.setText("Installing feature: " +
                      dependency.getId() + '-' + version);
                }else{
                  overallLabel.setText("Updating feature: " +
                      dependency.getId() + '-' + version);
                }

                taskProgress.setValue(0);
                taskProgress.setIndeterminate(true);

                Command command = new InstallCommand(
                    EclipsePluginsStep.this,
                    dependency.getSite(),
                    dependency.getId());
                try{
                  command.start();
                  command.join();
                  if(command.getReturnCode() != 0){
                    if (command.isShutdown()){
                      return Boolean.TRUE;
                    }
                    throw new RuntimeException(
                        "error: " + command.getErrorMessage() +
                        " out: " + command.getResult());
                  }
                }finally{
                  command.destroy();
                }

                ii.remove();
                tableModel.removeRow(removeIndex);
              }else{
                removeIndex++;
              }
              overallProgressStep += 100;
              overallProgress.setValue(overallProgressStep);
            }
            taskLabel.setText("");
            taskProgress.setValue(taskProgress.getMaximum());
            overallProgress.setValue(overallProgress.getMaximum());

            if(dependencies.size() > 0){
              GuiDialogs.showWarning(Installer.getString("eclipsePlugins.manual"));
            }

            return Boolean.TRUE;
          }
        });

        if(successful.booleanValue()){
          overallProgress.setValue(overallProgress.getMaximum());
          overallLabel.setText(Installer.getString("install.done"));

          taskProgress.setValue(taskProgress.getMaximum());
          taskLabel.setText(Installer.getString("install.done"));

          setBusy(false);
          setValid(true);
          taskProgress.setIndeterminate(false);

          skipButton.setEnabled(false);
        }
      }catch(Exception ex){
        setError(ex);
      }
    }
  }

  private class SkipPluginsAction
    extends AbstractAction
  {
    private static final long serialVersionUID = 1L;

    public SkipPluginsAction()
    {
      super("Skip");
    }

    public void actionPerformed(ActionEvent e)
    {
      if(GuiDialogs.showConfirm(Installer.getString("eclipsePlugins.skip"))){
        setValid(true);
      }
    }
  }

  private class DependencyCellRenderer
    extends DefaultTableCellRenderer
  {
    private static final long serialVersionUID = 1L;

    public Component getTableCellRendererComponent(
        JTable table, Object value,
        boolean isSelected, boolean hasFocus,
        int row, int column)
    {
      Component component = super.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
      Feature feature = ((Dependency)dependencies.get(row)).getFeature();
      if (feature != null &&
          (feature.getSite() == null || !feature.getSite().canWrite()))
      {
        component.setBackground(isSelected ?
            theme.getMenuItemSelectedBackground() : ERROR_COLOR);
        component.setForeground(isSelected ? ERROR_COLOR : Color.BLACK);
      }else{
        component.setBackground(isSelected ?
            theme.getMenuItemSelectedBackground() : Color.WHITE);
        component.setForeground(isSelected ?
            theme.getMenuItemSelectedForeground() : theme.getMenuForeground());
      }
      return component;
    }
  }

  /**
   * Mouse listener for the feature list.
   */
  private class DependencySelectionListener
    implements ListSelectionListener
  {
    @Override
    public void valueChanged(ListSelectionEvent e)
    {
      ListSelectionModel model = (ListSelectionModel)e.getSource();
      int index = model.getMinSelectionIndex();
      Feature feature = index >= 0 ?
        ((Dependency)dependencies.get(index)).getFeature() : null;

      if (feature != null) {
        if (feature.getSite() == null){
          setMessage(Installer.getString("eclipsePlugins.upgrade.site.not.found"));
        }else if (!feature.getSite().canWrite()){
          setMessage(Installer.getString(
                "eclipsePlugins.upgrade.permission.denied"));
        }
      }else{
        setMessage(null);
      }
    }
  }

  private static class FeatureNameComparator
    implements Comparator<String>
  {
    private static ArrayList<String> NAMES = new ArrayList<String>();
    static{
      NAMES.add("featureList.jdt");
      NAMES.add("featureList.wst");
      NAMES.add("featureList.adt");
      NAMES.add("featureList.cdt");
      NAMES.add("featureList.pdt");
      NAMES.add("featureList.pydev");
      NAMES.add("featureList.sdt210");
      NAMES.add("featureList.groovy");
      NAMES.add("featureList.kotlin");
    }

    public int compare(String ob1, String ob2)
    {
      return NAMES.indexOf(ob1) - NAMES.indexOf(ob2);
    }
  }
}
