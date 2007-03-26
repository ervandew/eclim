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
package org.eclim.installer;

import java.io.InputStream;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Singleton which loads a list of eclipse features from the eclipse callisto
 * site.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class EclipseFeatures
{
  private static EclipseFeatures instance;
  private static Map features = new HashMap();

  private EclipseFeatures (String featuresXml)
    throws Exception
  {
    DocumentBuilder builder =
      DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputStream in = new URL(featuresXml).openStream();
    try {
      Document document = builder.parse(in);
      NodeList list =
        document.getDocumentElement().getElementsByTagName("feature");
      for(int ii = 0; ii < list.getLength(); ii++){
        Element element = (Element)list.item(ii);
        String id = element.getAttribute("id");
        features.put(id, new Feature(id, element.getAttribute("version")));
      }
    }finally{
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Gets the EclipseFeatures instance.
   *
   * @param properties Dependencies properties.
   *
   * @return The EclipseFeatures instance.
   */
  public static EclipseFeatures getInstance (Properties properties)
    throws Exception
  {
    if(instance == null){
      String featuresXml = properties.getProperty("eclipse.features");
      instance = new EclipseFeatures(featuresXml);
    }
    return instance;
  }

  /**
   * Gets the latest version id for the supplied feature.
   *
   * @param feature The feature.
   * @return The latest version id.
   */
  public String getVersion (String feature)
  {
    return ((Feature)features.get(feature)).getVersion();
  }

  private static class Feature
  {
    private String id;
    private String version;

    public Feature (String id, String version)
    {
      this.id = id;
      this.version = version;
    }

    public String getId ()
    {
      return id;
    }

    public String getVersion ()
    {
      return version;
    }
  }
}
