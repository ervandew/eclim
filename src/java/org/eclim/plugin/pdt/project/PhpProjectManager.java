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
package org.eclim.plugin.pdt.project;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclim.plugin.pdt.PluginResources;

import org.eclim.project.ProjectManager;

import org.eclim.util.XmlUtils;

import org.eclipse.core.resources.IProject;

/**
 * Implementation of {@link ProjectManager} for php projects.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class PhpProjectManager
  implements ProjectManager
{
  private static final String PROJECT_OPTIONS_XSD =
    "/resources/schema/eclipse/projectOptions.xsd";

  /**
   * {@inheritDoc}
   */
  public void create (IProject _project, CommandLine _commandLine)
    throws Exception
  {
  }

  /**
   * {@inheritDoc}
   */
  public Error[] update (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    // validate that .classpath xml is well formed and valid.
    String projectOptions = _project.getFile(".projectOptions")
      .getRawLocation().toOSString();
    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    Error[] errors = XmlUtils.validateXml(
        _project.getName(),
        projectOptions,
        resources.getResource(PROJECT_OPTIONS_XSD).toString());
    if(errors.length > 0){
      return errors;
    }

    /*IClasspathEntry[] entries = javaProject.readRawClasspath();
    errors = setClasspath(javaProject, entries, dotclasspath);

    if(errors.length > 0){
      return errors;
    }*/
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void refresh (IProject _project, CommandLine _commandLine)
    throws Exception
  {
  }

  /**
   * {@inheritDoc}
   */
  public void delete (IProject _project, CommandLine _commandLine)
    throws Exception
  {
  }
}
