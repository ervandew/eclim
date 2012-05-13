/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

import java.io.File;

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
  public static final String[] FEATURES =
    {"jdt", "wst", "cdt", "dltk", "dltkruby", "pdt", "python"};

  private static final String[][] FEATURES_DEPENDS =
    {null, null, null, null, {"dltk"}, {"dltk", "wst"}, null};

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.step.shared.FeatureProvider#getFeatures()
   */
  public Feature[] getFeatures()
  {
    EclipseInfo info = (EclipseInfo)
      Installer.getContext().getValue("eclipse.info");

    boolean[] enabled = new boolean[FEATURES.length];
    for (int ii = 0; ii < FEATURES.length; ii++){
      enabled[ii] = false;

      if (FEATURES[ii].equals("python")){
        String path = Installer.getProject()
          .replaceProperties("${vim.files}/eclim/ftplugin/python");
        enabled[ii] = new File(path).exists();
        continue;
      }else{
        enabled[ii] = info.getUninstalledDependencies(FEATURES[ii]).size() == 0;
      }
    }

    Feature[] features = new Feature[FEATURES.length];
    for (int ii = 0; ii < features.length; ii++){
      features[ii] = new Feature(
          FEATURES[ii], enabled[ii], FEATURES_DEPENDS[ii]);
    }

    return features;
  }

  /**
   * {@inheritDoc}
   * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent evt)
  {
    // do nothing for now.
  }
}
