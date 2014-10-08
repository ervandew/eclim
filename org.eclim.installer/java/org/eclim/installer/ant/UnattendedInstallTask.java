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
package org.eclim.installer.ant;

import java.util.HashMap;
import java.util.ResourceBundle;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Task;

import org.eclim.installer.step.EclipseInfo;
import org.eclim.installer.step.FeatureProvider;

import org.formic.Installer;

import org.formic.wizard.step.shared.Feature;

/**
 * Task to handle unattended installs.
 *
 * Gathers info otherwise obtained from the gui installer.
 *
 * @author Eric Van Dewoestine
 */
public class UnattendedInstallTask
  extends Task
{
  public void execute()
    throws BuildException
  {
    boolean uninstall = false;

    if (Installer.getProject() == null){
      Installer.setProject(getProject());
      // hack to deal w/ unattended install bypassing the formic installer task
      if (Installer.getResourceBundle() == null){
        Installer.setResourceBundle(ResourceBundle.getBundle("resources.install"));
      }

      String targets = getProject().getProperty(MagicNames.PROJECT_INVOKED_TARGETS);
      uninstall = "uninstall".equals(targets);
    }else{
      uninstall = Installer.isUninstall();
    }

    EclipseInfo info = (EclipseInfo)
      Installer.getContext().getValue("eclipse.info");
    if (info == null){
      try{
          log("Installing eclim installer feature (may take a few moments).");
          if (EclipseInfo.installInstallerPlugin()){
            log("Analyzing installed features...");
            info = EclipseInfo.gatherEclipseInfo();
            log("Finished analyzing your eclipse installation.");
            Installer.getContext().setValue("eclipse.info", info);
          }
      }catch(Exception e){
        throw new BuildException(e);
      }

      if (!uninstall){
        // determine features to enable
        FeatureProvider provider = new FeatureProvider();
        Feature[] features = provider.getFeatures();
        HashMap<String,Feature> featureMap = new HashMap<String,Feature>();
        for(Feature feature : features){
          featureMap.put(feature.getKey(), feature);
        }

        log("Eclim features to be installed:");
        for(Feature feature : features){
          boolean enabled = feature.isEnabled();
          if (enabled){
            enabled = dependenciesEnabled(feature, featureMap);
          }

          Installer.getContext().setValue("featureList." + feature.getKey(), enabled);
          if (enabled){
            log("  " + feature.getKey());
          }
        }
      }
    }
  }

  private boolean dependenciesEnabled(
      Feature feature, HashMap<String,Feature> featureMap)
  {
    boolean enabled = true;
    String[] dependencies = feature.getDependencies();
    if (dependencies != null){
      for(String dep : dependencies){
        Feature dependency = featureMap.get(dep);
        if(!dependency.isEnabled() || !dependenciesEnabled(dependency, featureMap)){
          enabled = false;
          break;
        }
      }
    }

    return enabled;
  }
}
