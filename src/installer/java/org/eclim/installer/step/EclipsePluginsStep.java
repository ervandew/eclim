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

import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

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
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.formic.util.Extractor;

import com.jgoodies.looks.plastic.PlasticTheme;

import foxtrot.Worker;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.eclim.installer.URLProgressInputStream;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.InstallCommand;
import org.eclim.installer.step.command.ListCommand;
import org.eclim.installer.step.command.OutputHandler;
import org.eclim.installer.step.command.UninstallCommand;

import org.eclim.installer.theme.DesertBlue;

import org.formic.Installer;

import org.formic.util.dialog.gui.GuiDialogs;

import org.formic.wizard.step.gui.InstallStep;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

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
  private static final String FEATURE = "  Feature: ";
  private static final String SITE = "Site: file:";
  private static final String DEPENDENCIES = "/resources/dependencies.xml";

  private static final Color ERROR_COLOR = new Color(255, 201, 201);

  private String taskName = "";

  private JPanel stepPanel;
  private JPanel featuresPanel;
  private JLabel messageLabel;
  private ImageIcon errorIcon;
  private DefaultTableModel tableModel;
  private List<Dependency> dependencies;
  private PlasticTheme theme;

  private String primaryUpdateSite;

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

      // handle step re-entry.
      taskProgress.setIndeterminate(true);
      if (featuresPanel != null){
        stepPanel.remove(featuresPanel);
      }

      EclipseInfo info = (EclipseInfo)Worker.post(new foxtrot.Task(){
        public Object run()
          throws Exception
        {
          overallLabel.setText(
            "Installing eclim installer feature (may take a few moments).");
          installInstallerPlugin();
          EclipseInfo info = new EclipseInfo(getDependencies());
          return info;
        }
      });

      dependencies = info.getDependencies();
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

        for (Dependency dependency : dependencies){
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

  /**
   * Installs the eclipse plugin used to install eclipse features.
   */
  private void installInstallerPlugin()
    throws Exception
  {
    File updateDir = Installer.tempDir("update");
    Extractor.extractResource("/files/installer-site.zip", updateDir);

    String url = "file://" + updateDir;
    Command command = new InstallCommand(
        null, url, "org.eclim.installer", "org.eclipse.equinox.p2.director");

    try{
      command.start();
      command.join();
      if(command.getReturnCode() != 0){
        RuntimeException re = new RuntimeException(
            "error: " + command.getErrorMessage() +
            " out: " + command.getResult());

        Installer.getProject().log(
            "Error installing eclim installer feature, " +
            "attempting uninstall/reinstall.");

        // attempt to uninstall the feature
        Command uninstall = new UninstallCommand(
            null, url, "org.eclim.installer", "org.eclipse.equinox.p2.director");
        uninstall.start();
        uninstall.join();

        // now try to install again
        command = new InstallCommand(
            null, url, "org.eclim.installer", "org.eclipse.equinox.p2.director");
        command.start();
        command.join();
        if(command.getReturnCode() != 0){
          throw re;
        }
      }
      Installer.getProject().setProperty("eclim.feature.installed", "true");
      Installer.getProject().setProperty("eclim.feature.location", url);
    }finally{
      command.destroy();
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
    throws Exception
  {
    return new InstallCommand(
        this, dependency.getFeatureUrl(), dependency.getId());
  }

  /**
   * Gets a list of required dependencies based on the chosen set of eclim
   * features to be installed.
   *
   * @return List of dependencies.
   */
  private List<Dependency> getDependencies()
    throws Exception
  {
    Document document = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder().parse(
          EclipsePluginsStep.class.getResource(DEPENDENCIES).toString());

    primaryUpdateSite = document.getDocumentElement().getAttribute("primary");

    // determine which dependencies are required
    ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
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
            ArrayList<String> sites = new ArrayList<String>();
            NodeList siteList = node.getElementsByTagName("site");
            for(int kk = 0; kk < siteList.getLength(); kk++){
              Element site = (Element)siteList.item(kk);
              sites.add(site.getAttribute("url"));
            }

            dependencies.add(new Dependency(
                  node.getAttribute("id"),
                  node.getAttribute("version"),
                  sites.toArray(new String[sites.size()])));
          }
        }
      }
    }
    return dependencies;
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
            for (Dependency dependency : dependencies){
              Feature feature = dependency.getFeature();
              if (feature != null && !feature.getSite().canWrite()){
                GuiDialogs.showWarning(Installer.getString(
                    "eclipsePlugins.install.features.permission.denied"));
                return Boolean.FALSE;
              }
            }

            overallProgress.setMaximum(dependencies.size());
            overallProgress.setValue(0);

            int removeIndex = 0;
            for (Iterator<Dependency> ii = dependencies.iterator(); ii.hasNext();){
              Dependency dependency = ii.next();
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
                    throw new RuntimeException(
                        "error: " + command.getErrorMessage() +
                        " out: " + command.getResult());
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

  private class EclipseInfo
  {
    private List<File> sites;
    private Map<String,Feature> features;
    private List<Dependency> dependencies;
    private Map<String,String> availableFeatures;

    /**
     * Contstruct a new EclipseInfo instance containing available local plugins
     * sites, installed features, and required dependencies.
     *
     * @param List of dependencies.
     */
    public EclipseInfo(List<Dependency> dependencies)
      throws Exception
    {
      this.dependencies = dependencies;
      this.features = new HashMap<String,Feature>();
      this.sites = new ArrayList<File>();
      this.availableFeatures = new HashMap<String,String>();

      overallProgress.setMaximum(3);

      // run eclipse to get a list of existing installed features
      overallLabel.setText("Analyzing installed features...");
      Command command = new ListCommand(new OutputHandler(){
        File site = null;
        public void process(String line){
          Installer.getProject().log(line);
          if(line.startsWith(SITE)){
            site = new org.formic.util.File(line.substring(SITE.length()));
            sites.add(site);
          }else if(line.startsWith(FEATURE)){
            String[] attrs = StringUtils.split(line.substring(FEATURE.length()));
            features.put(attrs[0], new Feature(attrs[0], attrs[1], site));
          }
        }
      });
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

      // load up available features from the primary update site.
      overallLabel.setText(
          "Loading available features from the primary update site...");

      BufferedInputStream in = null;
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

      // download compositeContent.jar to determine location on content.jar
      final String[] location = new String[1];
      try{
        overallProgress.setValue(1);
        System.out.println("Downloading compositeContent.jar");
        taskLabel.setText("Downloading compositeContent.jar");
        in = new BufferedInputStream(
            new URLProgressInputStream(
              taskProgress,
              new URL(primaryUpdateSite + "compositeContent.jar").openConnection()));

        JarInputStream jin = new JarInputStream(in);
        JarEntry entry = jin.getNextJarEntry();
        while (!entry.getName().equals("compositeContent.xml")){
          entry = jin.getNextJarEntry();
        }
        parser.parse(jin, new DefaultHandler(){
          public void startElement(
              String uri, String localName, String qName, Attributes attributes)
            throws SAXException
          {
            if(qName.equals("child")){
              location[0] = attributes.getValue("location");
            }
          }
        });
      }finally{
        IOUtils.closeQuietly(in);
      }

      try{
        overallProgress.setValue(2);
        System.out.println("Downloading " + location[0] + "/content.jar");
        taskLabel.setText("Downloading " + location[0] + "/content.jar");
        in = new BufferedInputStream(
            new URLProgressInputStream(taskProgress, new URL(
                primaryUpdateSite + location[0] + "/content.jar").openConnection()));

        JarInputStream jin = new JarInputStream(in);
        JarEntry entry = jin.getNextJarEntry();
        while (!entry.getName().equals("content.xml")){
          entry = jin.getNextJarEntry();
        }
        parser.parse(jin, new DefaultHandler(){
          public void startElement(
              String uri, String localName, String qName, Attributes attributes)
            throws SAXException
          {
            if(qName.equals("unit")){
              String id = attributes.getValue("id");
              if (id.endsWith(".feature.group")){
                String version = attributes.getValue("version");
                String name = id.substring(0, id.length() - 14);
                availableFeatures.put(name, version);
              }
            }
          }
        });
      }finally{
        IOUtils.closeQuietly(in);
      }

      overallProgress.setValue(3);
      filterDependencies();
    }

    public List<File> getSites()
    {
      return sites;
    }

    public List<Dependency> getDependencies()
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
      for (Iterator<Dependency> ii = dependencies.iterator(); ii.hasNext();){
        Dependency dependency = ii.next();
        Feature feature = (Feature)features.get(dependency.getId());
        boolean include = dependency.eval(feature, availableFeatures);
        if (!include){
          ii.remove();
        }
      }
    }
  }

  private class Dependency
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

    public boolean eval(Feature feature, Map<String,String> availableFeatures)
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
      String[] urlVersion = findUrlVersion(availableFeatures);
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

    private String[] findUrlVersion(Map<String,String> availableFeatures)
      throws Exception
    {
      DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
      String resolvedUrl = null;
      String resolvedVersion = null;
      for(int ii = 0; ii < sites.length; ii++){
        if(sites[ii].equals(primaryUpdateSite)){
          if(availableFeatures.containsKey(this.id)){
            String version = (String)availableFeatures.get(this.id);
            return new String[]{primaryUpdateSite, version};
          }
        }

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

    public Feature(String id, String version, File site) {
      this.id = id;
      this.site = site;

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
  }

  private static class FeatureNameComparator
    implements Comparator<String>
  {
    private static ArrayList<String> NAMES = new ArrayList<String>();
    static{
      NAMES.add("featureList.jdt");
      NAMES.add("featureList.ant");
      NAMES.add("featureList.maven");
      NAMES.add("featureList.wst");
      NAMES.add("featureList.cdt");
      NAMES.add("featureList.pdt");
      NAMES.add("featureList.python");
    }

    public int compare(String ob1, String ob2) {
      return NAMES.indexOf(ob1) - NAMES.indexOf(ob2);
    }
  }
}
