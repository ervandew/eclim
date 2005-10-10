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

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
  private static final Log log =
    LogFactory.getLog(ResourceBundleMessageSource.class);

  private String basename;

  protected ResourceBundle getResourceBundle (String basename, Locale locale)
  {
    try{
      return ResourceBundle.getBundle(basename, locale);
    }catch(Exception e){
      log.error(
        "Error retrieving bundle '" + basename + "' with locale " + locale, e);
    }
    return null;
  }

  public ResourceBundle getResourceBundle ()
  {
    return getResourceBundle(basename, Locale.getDefault());
  }

  public void setBasename (String _basename)
  {
    basename = _basename;
    super.setBasename(_basename);
  }
}
