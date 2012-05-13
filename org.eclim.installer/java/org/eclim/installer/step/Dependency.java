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

import java.io.BufferedInputStream;

import java.net.URL;

import java.util.Map;

import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Dependency
{
  private String id;
  private String[] sites;
  private boolean upgrade;
  private Feature feature;
  private String version;
  private String featureUrl;
  private String featureVersion;
  private String primaryUpdateSite;

  public Dependency(
      String id,
      String version,
      String[] sites,
      Feature feature,
      String primaryUpdateSite)
  {
    this.id = id;
    this.version = version;
    this.sites = sites;
    this.feature = feature;
    if(feature != null){
      this.upgrade = compareVersions(this.version, feature.getVersion()) < 0;
    }
    this.primaryUpdateSite = primaryUpdateSite;
  }

  public String getId()
  {
    return id;
  }

  public String getVersion()
  {
    return version;
  }

  public boolean isInstalled()
  {
    return feature != null;
  }

  public boolean isUpgrade()
  {
    return upgrade;
  }

  public Feature getFeature()
  {
    return feature;
  }

  public String getFeatureUrl()
  {
    return featureUrl;
  }

  public String getFeatureVersion()
  {
    return featureVersion;
  }

  public boolean eval(Map<String,String> availableFeatures)
    throws Exception
  {
    if(feature != null && !isUpgrade()){
      return false;
    }
    String[] urlVersion = findUrlVersion(availableFeatures);
    this.featureUrl = urlVersion[0];
    this.featureVersion = urlVersion[1];
    return true;
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
