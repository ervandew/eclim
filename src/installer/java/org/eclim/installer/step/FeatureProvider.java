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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.FilenameFilter;

import org.formic.Installer;

import org.formic.wizard.step.shared.Feature;

/**
 * Provider to supply avaiable features to FeatureListStep.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class FeatureProvider
  implements org.formic.wizard.step.shared.FeatureProvider, PropertyChangeListener
{
  /*private static final String[] FEATURES =
    {"ant", "maven", "jdt", "wst", "pdt", "python"};

  private static final boolean[] FEATURES_ENABLED =
    {true, true, true, false, false, false};

  private static final String[][] FEATURES_DEPENDS =
    {{"jdt"}, null, null, null, {"wst"}, null};*/

  private static final String[] FEATURES =
    {"ant", "maven", "jdt", "wst", "python"};

  private static final boolean[] FEATURES_ENABLED =
    {true, true, true, false, false};

  private static final String[][] FEATURES_DEPENDS =
    {{"jdt"}, null, null, null, null};

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.step.shared.FeatureProvider#getFeatures()
   */
  public Feature[] getFeatures ()
  {
    boolean[] enabled = new boolean[FEATURES.length];
    for (int ii = 0; ii < FEATURES.length; ii++){
      String path = Installer.getProject()
        .replaceProperties("${eclipse.home}/plugins/");
      final String pluginPath = "org.eclim." + FEATURES[ii] + "_";
      String[] list = new File(path).list(new FilenameFilter(){
        public boolean accept (File file, String name) {
          return name.startsWith(pluginPath);
        }
      });

      enabled[ii] = list.length > 0 ? true : FEATURES_ENABLED[ii];
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
  public void propertyChange (PropertyChangeEvent evt)
  {
    // do nothing for now.
  }
}
