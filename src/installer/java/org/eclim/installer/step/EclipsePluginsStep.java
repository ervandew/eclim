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

import java.text.Collator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import org.formic.Installer;

import org.formic.wizard.step.InstallStep;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.EnableCommand;
import org.eclim.installer.step.command.InstallCommand;
import org.eclim.installer.step.command.ListCommand;
import org.eclim.installer.step.command.OutputHandler;
import org.eclim.installer.step.command.UninstallCommand;
import org.eclim.installer.step.command.UpdateCommand;

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
    filterDependencies(dependencies, getFeatures());
    if(dependencies.size() == 0){
      guiOverallProgress.setMaximum(1);
      guiOverallProgress.setValue(1);
      guiTaskProgress.setMaximum(1);
      guiTaskProgress.setValue(1);
    }else{
      guiOverallProgress.setMaximum(dependencies.size());
      guiOverallProgress.setValue(0);
      for (Iterator ii = dependencies.iterator(); ii.hasNext();){
        Dependency dependency = (Dependency)ii.next();
        if(!dependency.isUpgrade()){
          guiOverallLabel.setText("Installing feature: " + dependency.getId());
        }else{
          guiOverallLabel.setText("Updating feature: " + dependency.getId());
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
        guiOverallProgress.setValue(guiOverallProgress.getValue() + 1);
      }
      guiTaskLabel.setText("");
      guiTaskProgress.setValue(guiTaskProgress.getMaximum());
      guiOverallProgress.setValue(guiOverallProgress.getMaximum());
    }
  }

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
          dependencies.add(new Dependency(dependency));
        }
      }
    }
    return dependencies;
  }

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

  private void filterDependencies (List dependencies, List features)
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
