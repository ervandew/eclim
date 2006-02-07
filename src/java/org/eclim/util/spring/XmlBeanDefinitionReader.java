/**
 * Copyright (c) 2004 - 2006
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

import org.w3c.dom.Document;

import org.springframework.beans.BeansException;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import org.springframework.core.io.Resource;

/**
 * XmlBeanDefinitionReader extension that allows the classloader to be supplied
 * at construction.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class XmlBeanDefinitionReader
  extends org.springframework.beans.factory.xml.XmlBeanDefinitionReader
{
  private ClassLoader classloader;

  public XmlBeanDefinitionReader (
      BeanDefinitionRegistry _beanFactory, ClassLoader _classloader)
  {
    super(_beanFactory);
    classloader = _classloader;
  }

  public ClassLoader getBeanClassLoader ()
  {
    return classloader;
  }
}
