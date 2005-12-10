/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.util.spring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;

/**
 * Extension to spring ResourceBundleMessageSource that doesn't use the thread
 * classloader.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ResourceBundleMessageSource
  extends org.springframework.context.support.ResourceBundleMessageSource
{
  private static final Logger logger =
    Logger.getLogger(ResourceBundleMessageSource.class);

  private String[] basenames;
  private ResourceBundle bundle;

  /**
   * {@inheritDoc}
   */
  protected ResourceBundle getResourceBundle (String basename, Locale locale)
  {
    try{
      return ResourceBundle.getBundle(basename, locale);
    }catch(Exception e){
      logger.error(
        "Error retrieving bundle '" + basename + "' with locale " + locale, e);
    }
    return null;
  }

  /**
   * Gets an aggragate ResourceBundle containing all bundles for the default
   * locale.
   *
   * @return The ResourceBundle.
   */
  public ResourceBundle getResourceBundle ()
  {
    if(bundle == null){
      try{
        Properties properties = new Properties();
        for(int ii = 0; ii < basenames.length; ii++){
          ResourceBundle rb = getResourceBundle(
              basenames[ii], Locale.getDefault());
          Enumeration keys = rb.getKeys();
          while(keys.hasMoreElements()){
            String key = (String)keys.nextElement();
            properties.put(key, rb.getString(key));
          }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        properties.store(out, null);
        bundle = new PropertyResourceBundle(
            new ByteArrayInputStream(out.toByteArray()));
      }catch(Exception e){
        throw new RuntimeException(e);
      }
    }
    return bundle;
  }

  /**
   * {@inheritDoc}
   */
  public void setBasename (String _basename)
  {
    setBasenames(new String[]{_basename});
  }

  /**
   * {@inheritDoc}
   */
  public void setBasenames (String[] _basenames)
  {
    basenames = _basenames;
    super.setBasenames(_basenames);
  }
}
