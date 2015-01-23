/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.formic.Installer;

import org.formic.wizard.step.shared.Feature;

/**
 * Provider to supply avaiable features to FeatureListStep.
 *
 * @author Eric Van Dewoestine
 */
public class FeatureProvider
  implements org.formic.wizard.step.shared.FeatureProvider,
             PropertyChangeListener
{
  public static final String[] FEATURES = {
    "jdt", "adt", "wst", "cdt",
    "dltk", "dltkruby", "pdt",
    "pydev", "sdt", "groovy"
  };

  private static final String[][] FEATURES_DEPENDS = {
    null, {"jdt", "wst"}, null, null,
    null, {"dltk"}, {"dltk", "wst"},
    null, {"jdt"}, {"jdt"}
  };

  private static final String[][] FEATURES_EXCLUSIVE = {
    null, null, null, null,
    null, null, null,
    null, null, null
  };

  @Override
  public Feature[] getFeatures()
  {
    EclipseInfo info = (EclipseInfo)
      Installer.getContext().getValue("eclipse.info");

    boolean[] enabled = new boolean[FEATURES.length];
    for (int ii = 0; ii < FEATURES.length; ii++){
      enabled[ii] = false;
      enabled[ii] = info.getUninstalledDependencies(FEATURES[ii]).size() == 0;
    }

    ArrayList<Feature> features = new ArrayList<Feature>();
    feature:
    for (int ii = 0; ii < FEATURES.length; ii++){
      // check if the feature has any dependency matching, if so, exclude the
      // feature if the dependency exists but doesn't match
      for(Dependency dep : info.getDependencies(FEATURES[ii])){
        if (!dep.isInstalled()){
          continue;
        }
        String match = dep.getMatchVersion();
        if (match != null && match.length() > 0){
          Matcher matcher = Pattern.compile(match)
            .matcher(dep.getFeature().getFullVersion());
          if (!matcher.find()){
            continue feature;
          }
        }
      }

      Feature feature = new Feature(
          FEATURES[ii],
          enabled[ii],
          FEATURES_DEPENDS[ii],
          FEATURES_EXCLUSIVE[ii]);

      String status = info.getStatus(FEATURES[ii]);
      if (status != null){
        feature.setAvailable(false);
        feature.setEnabled(false);
        feature.setInfo(
            Installer.getString(
              "feature.status." + status,
              Installer.getString("eclipse.version")));
      }

      features.add(feature);
    }

    return features.toArray(new Feature[features.size()]);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    // do nothing for now.
  }
}
