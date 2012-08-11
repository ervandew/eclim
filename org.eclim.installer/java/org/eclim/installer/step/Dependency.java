/**
 * Copyright (C) 2012  Eric Van Dewoestine
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

import org.apache.commons.lang.StringUtils;

public class Dependency
{
  private String id;
  private String site;
  private boolean upgrade;
  private Feature feature;
  private String requiredVersion;

  public Dependency(
      String id,
      String site,
      String requiredVersion,
      Feature feature)
  {
    this.id = id;
    this.site = site;
    this.requiredVersion = requiredVersion;
    this.feature = feature;
    if(feature != null){
      this.upgrade = compareVersions(this.requiredVersion, feature.getVersion()) < 0;
    }
  }

  /**
   * Gets the id of this dependency.
   *
   * @return The id string.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Gets the required version of this dependency.
   *
   * @return The version string.
   */
  public String getRequiredVersion()
  {
    return requiredVersion;
  }

  /**
   * Determines if this dependency is currently installed.
   *
   * @return true if installed, false otherwise.
   */
  public boolean isInstalled()
  {
    return feature != null;
  }

  /**
   * Determines if this dependency needs to be upgraded.
   *
   * @return true if an upgrade is necessary, false otherwise.
   */
  public boolean isUpgrade()
  {
    return upgrade;
  }

  /**
   * Gets the currently installed feature info.
   *
   * @return A Feature instance.
   */
  public Feature getFeature()
  {
    return feature;
  }

  /**
   * Gets the site to install/upgrade the dependency from.
   *
   * @return The url of the dependency site.
   */
  public String getSite()
  {
    return this.site;
  }

  private int compareVersions(String v1, String v2)
  {
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
}
