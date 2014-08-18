/**
 * Copyright (C) 2012 - 2014  Eric Van Dewoestine
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

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.InfoCommand;
import org.eclim.installer.step.command.InstallCommand;
import org.eclim.installer.step.command.OutputHandler;
import org.eclim.installer.step.command.UninstallCommand;

import org.formic.Installer;

import org.formic.util.Extractor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EclipseInfo
{
  private static final Logger logger =
    LoggerFactory.getLogger(EclipseInfo.class);

  private static final String DEPENDENCIES = "/resources/dependencies.xml";
  private static final String PROFILE = "  Profile: ";
  private static final String CONFIGURATION = "  Configuration: ";
  private static final String FEATURE = "  Feature: ";

  private Map<String,Feature> installedFeatures;
  private Map<String,String> featureStatuses = new HashMap<String,String>();
  private Map<String,List<Dependency>> dependencies =
    new HashMap<String,List<Dependency>>();

  private String profileName;
  private String localPath;

  /**
   * Contstruct a new EclipseInfo instance containing installed features and
   * required dependencies.
   */
  public EclipseInfo(
      String profileName,
      String localPath,
      Map<String,Feature> installedFeatures)
    throws Exception
  {
    this.profileName = profileName;
    this.localPath = localPath;
    this.installedFeatures = installedFeatures;

    Document document = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder().parse(
          EclipseInfo.class.getResource(DEPENDENCIES).toString());
    Element root = document.getDocumentElement();
    NodeList features = root.getElementsByTagName("feature");
    for(int i = 0; i < features.getLength(); i++){
      Element feature = (Element)features.item(i);
      String id = feature.getAttribute("id");
      String status = feature.getAttribute("status");
      if (status != null && status.length() > 0){
        featureStatuses.put(id, status);
      }
      List<Dependency> dependencies = new ArrayList<Dependency>();
      NodeList deps = feature.getElementsByTagName("dependency");
      for(int j = 0; j < deps.getLength(); j++){
        Element node = (Element)deps.item(j);
        NodeList sites = node.getElementsByTagName("site");
        Element site = sites.getLength() > 0 ? (Element)sites.item(0) : null;
        dependencies.add(new Dependency(
              node.getAttribute("id"),
              site != null ? site.getAttribute("url") : null,
              node.getAttribute("version"),
              node.getAttribute("match"),
              this.installedFeatures.get(node.getAttribute("id"))));
      }
      this.dependencies.put(id, dependencies);
    }
  }

  public String getProfileName()
  {
    return profileName;
  }

  public String getLocalPath()
  {
    return localPath;
  }

  public boolean hasFeature(String name)
  {
    return this.installedFeatures.containsKey(name);
  }

  public String getStatus(String plugin)
  {
    return featureStatuses.get(plugin);
  }

  public List<Dependency> getDependencies(String plugin)
  {
    List<Dependency> deps = dependencies.get(plugin);
    if (deps == null){
      return new ArrayList<Dependency>();
    }
    return deps;
  }

  public List<Dependency> getUninstalledDependencies(String plugin)
  {
    List<Dependency> deps = new ArrayList<Dependency>();
    for(Dependency dep : getDependencies(plugin)){
      if(!dep.isInstalled()){
        deps.add(dep);
      }
    }
    return deps;
  }

  public List<Dependency> getUnsatisfiedDependencies(String plugin)
  {
    List<Dependency> deps = new ArrayList<Dependency>();
    for(Dependency dep : getDependencies(plugin)){
      if(!dep.isInstalled() || dep.isUpgrade()){
        deps.add(dep);
      }
    }
    return deps;
  }

  /**
   * Installs the eclipse plugin used to install eclipse features.
   */
  public static boolean installInstallerPlugin()
    throws Exception
  {
    File updateDir = Installer.tempDir("update");
    Extractor.extractResource("/files/installer-update-site.zip", updateDir);

    String url = "file://" + updateDir;
    Command command = new InstallCommand(
        null, url, "org.eclim.installer", "org.eclipse.equinox.p2.director");

    try{
      command.start();
      command.join();
      if(command.getReturnCode() != 0){
        if (command.isShutdown()){
          return false;
        }
        RuntimeException re = new RuntimeException(
            "error: " + command.getErrorMessage() +
            " out: " + command.getResult());

        logger.warn(
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
          if (command.isShutdown()){
            return false;
          }
          throw re;
        }
      }
      Installer.getProject().setProperty("eclim.installer.feature.installed", "true");
      Installer.getProject().setProperty("eclim.installer.feature.location", url);
    }finally{
      command.destroy();
    }
    return true;
  }

  public static EclipseInfo gatherEclipseInfo()
    throws Exception
  {
    final Map<String,Feature> installedFeatures = new HashMap<String,Feature>();

    // run eclipse to get a list of existing installed features
    Command command = new InfoCommand(new OutputHandler(){
      public void process(String line){
        logger.info(line);
        if(line.startsWith(FEATURE)){
          String[] attrs = StringUtils.split(line.substring(FEATURE.length()));
          File site = null;
          try{
            site = new File(new URL(attrs[2]).getFile());
          }catch(Exception e){
            logger.error("Failed to parse feature: " + line, e);
          }
          installedFeatures.put(attrs[0], new Feature(attrs[1], site));
        }else if(line.startsWith(CONFIGURATION)){
          String config = line.substring(CONFIGURATION.length());
          if (config.startsWith("file:")){
            config = config.substring(5);
          }
          if(Os.isFamily(Os.FAMILY_WINDOWS) && config.startsWith("/")){
            config = config.substring(1);
          }
          String local = new File(config).getParent();
          logger.info("eclipse.local=" + local);
          Installer.getContext().setValue("eclipse.local", local);
        }else if(line.startsWith(PROFILE)){
          String profile = line.substring(PROFILE.length());
          logger.info("eclipse.profile=" + profile);
          Installer.getContext().setValue("eclipse.profile", profile);
        }
      }
    });
    try{
      command.start();
      command.join();
      if(command.getReturnCode() != 0){
        if (command.isShutdown()){
          return null;
        }
        throw new RuntimeException(
            "error: " + command.getErrorMessage() +
            " out: " + command.getResult());
      }
    }finally{
      command.destroy();
    }

    return new EclipseInfo(
        (String)Installer.getContext().getValue("eclipse.profile"),
        (String)Installer.getContext().getValue("eclipse.local"),
        installedFeatures);
  }
}
