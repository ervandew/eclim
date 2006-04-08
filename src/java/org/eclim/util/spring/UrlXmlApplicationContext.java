/**
 * Copyright (c) 2005 - 2006
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

import java.io.IOException;

import java.net.URL;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import org.springframework.beans.factory.xml.ResourceEntityResolver;

import org.springframework.context.support.AbstractXmlApplicationContext;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Application context implementation that loads the context from an xml file at
 * a specified url.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class UrlXmlApplicationContext
  extends AbstractXmlApplicationContext
{
  private String[] configs;
  private ClassLoader classloader;

  /**
   * Construct a new instance using the resource at the supplied url.
   *
   * @param _url The url.
   */
  public UrlXmlApplicationContext (URL _url, ClassLoader _classloader)
  {
    configs = new String[]{_url.toString()};
    classloader = _classloader;
    refresh();
  }

  /**
   * {@inheritDoc}
   */
  protected String[] getConfigLocations ()
  {
    return configs;
  }

  /**
   * Resolve resource paths as urls.
   *
   * @param _url url to the resource
   * @return Resource handle
   */
  protected Resource getResourceByPath (String _url)
  {
    if(_url.indexOf(':') == -1){
      return super.getResourceByPath(_url);
    }

    try{
      return new UrlResource(_url);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * Loads the bean definitions via an XmlBeanDefinitionReader.
   */
  protected void loadBeanDefinitions (DefaultListableBeanFactory beanFactory)
    throws IOException
  {
    XmlBeanDefinitionReader beanDefinitionReader =
      new XmlBeanDefinitionReader(beanFactory, classloader);

    // no change from default spring implementation...
    beanDefinitionReader.setResourceLoader(this);
    beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
    initBeanDefinitionReader(beanDefinitionReader);
    loadBeanDefinitions(beanDefinitionReader);
  }
}
