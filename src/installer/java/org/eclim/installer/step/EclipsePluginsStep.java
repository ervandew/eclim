/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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

import java.io.BufferedInputStream;
import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.jgoodies.looks.plastic.PlasticTheme;

import foxtrot.Worker;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.taskdefs.Untar;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.eclim.installer.step.command.AddSiteCommand;
import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.InstallCommand;
import org.eclim.installer.step.command.ListCommand;
import org.eclim.installer.step.command.OutputHandler;

import org.eclim.installer.theme.DesertBlue;

import org.formic.Installer;

import org.formic.util.dialog.gui.GuiDialogs;

import org.formic.wizard.step.gui.InstallStep;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
  private static final String FEATURE = "  Feature";
  private static final String SITE = "Site: file:";
  private static final String ENABLED = "enabled";
  private static final String DEPENDENCIES = "/resources/dependencies.xml";

  private static final Color ERROR_COLOR = new Color(255, 201, 201);

  private String taskName = "";

  private JPanel stepPanel;
  private JPanel featuresPanel;
  private JLabel messageLabel;
  private ImageIcon errorIcon;
  private DefaultTableModel tableModel;
  private List dependencies;
  private PlasticTheme theme;

  /**
   * Constructs this step.
   */
  public EclipsePluginsStep(String name, Properties properties)
  {
    super(name, properties);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.step.GuiStep#init()
   */
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

  /**
   * Invoked when this step is displayed in the gui.
   */
  public void displayed()
  {
    setBusy(true);
    setPreviousEnabled(false);

    try{
      // reset these two to account for re-entry into this step.
      overallProgress.setValue(0);
      taskLabel.setText("");
      taskProgress.setValue(0);

      overallLabel.setText("Analyzing installed features...");

      // handle step re-entry.
      taskProgress.setIndeterminate(true);
      if (featuresPanel != null){
        stepPanel.remove(featuresPanel);
      }

      EclipseInfo info = (EclipseInfo)Worker.post(new foxtrot.Task(){
        public Object run()
          throws Exception
        {
          extractInstallerPlugin();
          EclipseInfo info = EclipseInfo.getInfo(getDependencies());
          if (!Os.isFamily("windows")){
            String home = (String)
              Installer.getContext().getValue("eclipse.home");
            String local = (String)
              Installer.getContext().getValue("eclipse.local");
            if(!home.equals(local)){
              File site = new File(local);
              if (!info.getSites().contains(site)){
                overallLabel.setText("Adding local site to eclipse sites...");
                addSite(site.getAbsolutePath());
              }
            }
          }
          return info;
        }
      });

      dependencies = info.getDependencies();
      if(dependencies.size() == 0){
        overallProgress.setMaximum(1);
        overallProgress.setValue(1);
        taskProgress.setMaximum(1);
        taskProgress.setValue(1);
        overallLabel.setText("All third party plugins are up to date.");
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

        for (int ii = 0; ii < dependencies.size(); ii++){
          Dependency dependency = (Dependency)dependencies.get(ii);
          String version = dependency.getFeatureVersion();
          String manual = "";
          if (version == null){
            manual = " (Manual)";
            version = dependency.getVersion();
          }
          tableModel.addRow(new Object[]{
            dependency.getId(),
            version,
            (dependency.isUpgrade() ? "Upgrade" : "Install") + manual
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
        overallLabel.setText("");
      }
    }catch(Exception e){
      setError(e);
    }finally{
      setValid(dependencies.size() == 0);
      setBusy(false);
      setPreviousEnabled(true);
      taskProgress.setIndeterminate(false);
    }
  }

  /**
   * Extracts the eclipse plugin used to install eclipse features.
   */
  private void extractInstallerPlugin()
    throws Exception
  {
    String plugins = (String)Installer.getContext().getValue("eclim.plugins");

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

    // on unix based systems, set the eclipse home and chmod the install sh
    // file.
    if (!Os.isFamily("windows")){
      File installScript = new File(
          Installer.getProject().replaceProperties(
            "${eclim.plugins}/org.eclim.installer_${eclim.version}/bin/install"));
      Replace replace = new Replace();
      replace.setTaskName("replace");
      replace.setFile(installScript);
      replace.setToken("${eclipse.home}");
      replace.setValue((String)Installer.getContext().getValue("eclipse.home"));
      replace.execute();

      Chmod chmod = new Chmod();
      chmod.setTaskName("chmod");
      chmod.setFile(installScript);
      chmod.setPerm("755");
      chmod.setProject(Installer.getProject());
      chmod.execute();
    }
  }

  /**
   * Gets the command which need to be executed to install or upgrade the
   * supplied dependency.
   *
   * @param dependency The dependency to install or upgrade.
   * @return Command to be run.
   */
  private Command getCommand(Dependency dependency)
  {
    String to = null;
    /*if (!Os.isFamily("windows")){
      String home = (String)Installer.getContext().getValue("eclipse.home");
      String local = (String)Installer.getContext().getValue("eclipse.local");
      if (local != null && !home.equals(local)){
        to = local;
      }
    }*/

    return new InstallCommand(this,
        dependency.getFeatureUrl(),
        dependency.getId(),
        dependency.getFeatureVersion(),
        to);
  }

  /**
   * Gets a list of required dependencies based on the chosen set of eclim
   * features to be installed.
   *
   * @return List of dependencies.
   */
  private List getDependencies()
    throws Exception
  {
    Document document = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder().parse(
          EclipsePluginsStep.class.getResource(DEPENDENCIES).toString());

    // determine which dependencies are required
    ArrayList dependencies = new ArrayList();
    String[] features = Installer.getContext().getKeysByPrefix("featureList");
    Arrays.sort(features, new FeatureNameComparator());
    for (int ii = 0; ii < features.length; ii++){
      Boolean enabled = (Boolean)Installer.getContext().getValue(features[ii]);
      String name = features[ii].substring(features[ii].indexOf('.') + 1);

      // check if the enabled eclim feature has any dependencies.
      if(enabled.booleanValue()){
        Element featureNode = document.getElementById(name);
        if(featureNode != null){
          NodeList nodes = featureNode.getElementsByTagName("dependency");

          // parse out all the possible dependencies
          for(int jj = 0; jj < nodes.getLength(); jj++){
            Element node = (Element)nodes.item(jj);
            ArrayList sites = new ArrayList();
            NodeList siteList = node.getElementsByTagName("site");
            for(int kk = 0; kk < siteList.getLength(); kk++){
              Element site = (Element)siteList.item(kk);
              sites.add(site.getAttribute("url"));
            }

            dependencies.add(new Dependency(
                  node.getAttribute("id"),
                  node.getAttribute("version"),
                  (String[])sites.toArray(new String[sites.size()])));
          }
        }
      }
    }
    return dependencies;
  }

  /**
   * Adds the supplied directory as a local eclipse site.
   */
  private void addSite(String site)
    throws Exception
  {
    Command command = new AddSiteCommand(site);
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
        }else if(line.startsWith(SET_TASK_NAME)){
          taskName = line.substring(SET_TASK_NAME.length() + 1).trim() + ' ';
        }
      }
    });
  }

  private class InstallPluginsAction
    extends AbstractAction
  {
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
            for (Iterator ii = dependencies.iterator(); ii.hasNext();){
              Feature feature = ((Dependency)ii.next()).getFeature();
              if (feature != null && !feature.getSite().canWrite()){
                GuiDialogs.showWarning(Installer.getString(
                    "eclipsePlugins.install.features.permission.denied"));
                return Boolean.FALSE;
              }
            }

            overallProgress.setMaximum(dependencies.size());
            overallProgress.setValue(0);

            int removeIndex = 0;
            for (Iterator ii = dependencies.iterator(); ii.hasNext();){
              Dependency dependency = (Dependency)ii.next();
              if (dependency.getFeatureUrl() != null){
                if(!dependency.isUpgrade()){
                  overallLabel.setText("Installing feature: " +
                      dependency.getId() + '-' + dependency.getVersion());
                }else{
                  overallLabel.setText("Updating feature: " +
                      dependency.getId() + '-' + dependency.getVersion());
                }

                taskProgress.setIndeterminate(true);

                Command command = getCommand(dependency);
                try{
                  command.start();
                  command.join();
                  if(command.getReturnCode() != 0){
                    throw new RuntimeException(command.getErrorMessage());
                  }
                }finally{
                  command.destroy();
                }

                /*try{
                  Thread.sleep(1000);
                }catch(Exception ex){
                }*/
                ii.remove();
                tableModel.removeRow(removeIndex);
              }else{
                removeIndex++;
              }
              overallProgress.setValue(overallProgress.getValue() + 1);
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
    public SkipPluginsAction()
    {
      super("Skip");
    }

    public void actionPerformed(ActionEvent e){
      if(GuiDialogs.showConfirm(Installer.getString("eclipsePlugins.skip"))){
        setValid(true);
      }
    }
  }

  private class DependencyCellRenderer
    extends DefaultTableCellRenderer
  {
    public Component getTableCellRendererComponent(
        JTable table, Object value,
        boolean isSelected, boolean hasFocus,
        int row, int column)
    {
      Component component = super.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
      Feature feature = ((Dependency)dependencies.get(row)).getFeature();
      if (feature != null && !feature.getSite().canWrite()){
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
    /**
     * {@inheritDoc}
     * @see ListSelectionListener#valueChanged(ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
      ListSelectionModel model = (ListSelectionModel)e.getSource();
      int index = model.getMinSelectionIndex();
      Feature feature = index >= 0 ?
        ((Dependency)dependencies.get(index)).getFeature() : null;

      if (feature != null && !feature.getSite().canWrite()){
        setMessage(Installer.getString("eclipsePlugins.upgrade.permission.denied"));
      }else{
        setMessage(null);
      }
    }
  }

  private static class EclipseInfo
  {
    private List sites;
    private Map features;
    private List dependencies;

    private EclipseInfo(List sites, Map features, List dependencies)
      throws Exception
    {
      this.sites = sites;
      this.features = features;
      this.dependencies = dependencies;
      filterDependencies();
    }

    /**
     * Gets EclipseInfo instance containing available local plugins sites,
     * installed features, and required dependencies.
     *
     * @return EclipseInfo
     */
    public static EclipseInfo getInfo(List dependencies)
      throws Exception
    {
      final HashMap features = new HashMap();
      final ArrayList sites = new ArrayList();
      Command command = new ListCommand(new OutputHandler(){
        File site = null;
        public void process(String line){
          if(line.startsWith(SITE)){
            site = new File(line.substring(SITE.length()));
            sites.add(site);
          }else if(line.startsWith(FEATURE)){
            String[] attrs = StringUtils.split(
              line.substring(FEATURE.length() + 2));
            features.put(attrs[0], new Feature(
                attrs[0], attrs[1], site, attrs[2].equals(ENABLED)));
          }
        }
      });
      try{
        command.start();
        command.join();
        if(command.getReturnCode() != 0){
          throw new RuntimeException(command.getErrorMessage());
        }
      }finally{
        command.destroy();
      }
      return new EclipseInfo(sites, features, dependencies);
    }

    public List getSites()
    {
      return sites;
    }

    public List getDependencies()
    {
      return dependencies;
    }

    /**
     * Filters the supplied list of dependencies to determine which are already
     * installed or need to be upgraded.
     */
    private void filterDependencies()
      throws Exception
    {
      for (Iterator ii = dependencies.iterator(); ii.hasNext();){
        Dependency dependency = (Dependency)ii.next();
        Feature feature = (Feature)features.get(dependency.getId());

        // temp hack, should be able to remove this starting at galileo (pdt
        // 2.1.0)
        if (feature == null &&
            "org.eclipse.php".equals(dependency.getId()))
        {
          feature = (Feature)features.get("org.eclipse.php_feature");
        }

        boolean include = dependency.eval(feature);
        if (!include){
          ii.remove();
        }
      }
    }
  }

  private static class Dependency
  {
    private String id;
    private String[] sites;
    private boolean upgrade;
    private Feature feature;
    private String version;
    private String featureUrl;
    private String featureVersion;

    public Dependency(String id, String version, String[] sites) {
      this.id = id;
      this.version = version;
      this.sites = sites;
    }

    public String[] getSites() {
      return sites;
    }

    public String getId() {
      return id;
    }

    public String getVersion() {
      return version;
    }

    public boolean isUpgrade() {
      return upgrade;
    }

    public Feature getFeature() {
      return feature;
    }

    public String getFeatureUrl() {
      return featureUrl;
    }

    public String getFeatureVersion() {
      return featureVersion;
    }

    public boolean eval(Feature feature)
      throws Exception
    {
      this.feature = feature;
      if(feature != null){
        int result = compareVersions(this.version, feature.getVersion());
        if (result >= 0){
          return false;
        }
        this.upgrade = true;
      }
      String[] urlVersion = findUrlVersion();
      this.featureUrl = urlVersion[0];
      this.featureVersion = urlVersion[1];
      return true;
    }

    private int compareVersions(String v1, String v2){
      String[] dv = StringUtils.split(v1, ".");
      String[] fv = StringUtils.split(v2, ".");
      for (int ii = 0; ii < dv.length; ii++){
        int dp = Integer.parseInt(dv[ii]);
        int fp = Integer.parseInt(fv[ii]);
        if(dp != fp){
          return fp - dp;
        }
      }
      return 0;
    }

    private String[] findUrlVersion()
      throws Exception
    {
      DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
      String resolvedUrl = null;
      String resolvedVersion = null;
      for(int ii = 0; ii < sites.length; ii++){
        BufferedInputStream in = null;
        try{
          in = new BufferedInputStream(
              new URL(sites[ii] + "site.xml").openStream());
          Document document = builder.parse(in);
          NodeList nodes = document.getElementsByTagName("feature");
          for(int jj = 0; jj < nodes.getLength(); jj++){
            Element feature = (Element)nodes.item(jj);
            if (this.id.equals(feature.getAttribute("id"))){
              String fv = feature.getAttribute("version");
              Matcher matcher = Feature.VERSION.matcher(fv);
              matcher.find();
              fv = matcher.group(1);

              int result = -1;
              if(resolvedVersion == null){
                result = compareVersions(this.version, fv);
              }else{
                result = compareVersions(resolvedVersion, fv);
              }
              if (result >= 0){
                resolvedUrl = sites[ii];
                resolvedVersion = feature.getAttribute("version");
              }
            }
          }

          if(resolvedUrl != null){
            return new String[]{resolvedUrl, resolvedVersion};
          }
        }finally{
          IOUtils.closeQuietly(in);
        }
      }
      return new String[]{null, null};
    }
  }

  private static class Feature
  {
    public static final Pattern VERSION =
      Pattern.compile("^(\\d+\\.\\d+\\.\\d+)(\\..*)?");

    private String id;
    private String version;
    private File site;
    private boolean enabled;

    public Feature(String id, String version, File site, boolean enabled) {
      this.id = id;
      this.site = site;
      this.enabled = enabled;

      Matcher matcher = VERSION.matcher(version);
      matcher.find();
      this.version = matcher.group(1);
    }

    public String getId() {
      return id;
    }

    public String getVersion() {
      return version;
    }

    public File getSite() {
      return this.site;
    }

    public boolean isEnabled() {
      return enabled;
    }
  }

  private static class FeatureNameComparator
    implements Comparator
  {
    private static ArrayList NAMES = new ArrayList();
    static{
      NAMES.add("featureList.jdt");
      NAMES.add("featureList.ant");
      NAMES.add("featureList.maven");
      NAMES.add("featureList.wst");
      NAMES.add("featureList.cdt");
      NAMES.add("featureList.pdt");
      NAMES.add("featureList.python");
    }

    public int compare(Object ob1, Object ob2) {
      return NAMES.indexOf(ob1) - NAMES.indexOf(ob2);
    }
  }
}
