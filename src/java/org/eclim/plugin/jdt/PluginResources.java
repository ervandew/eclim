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
package org.eclim.plugin.jdt;

import java.net.URL;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.jdt.preference.OptionHandler;

import org.eclim.plugin.jdt.project.JavaProjectManager;

import org.eclim.preference.Preferences;

import org.eclim.project.ProjectManagement;
import org.eclim.project.ProjectNatureFactory;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class PluginResources
  extends AbstractPluginResources
{
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.jdt";

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#initialize(String,URL)
   */
  @Override
  public void initialize (String _name, URL _resource)
  {
    super.initialize(_name, _resource);

    Preferences.addOptionHandler("org.eclipse.jdt", new OptionHandler());
    ProjectNatureFactory.addNature("java", "org.eclipse.jdt.core.javanature");
    ProjectManagement.addProjectManager(
        "org.eclipse.jdt.core.javanature", new JavaProjectManager());
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#getBundleBaseName()
   */
  protected String getBundleBaseName ()
  {
    return "org/eclim/plugin/jdt/messages";
  }
}
