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
package org.eclim.command.admin;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.server.eclipse.EclimPlugin;

import org.eclim.server.mina.Server;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Command to shutdown the eclim server.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ShutdownCommand
  extends AbstractCommand
{
  private static final Log log = LogFactory.getLog(ShutdownCommand.class);

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      Server server = (Server)Services.getService(Server.class);
      server.stop();

      Services.close();

      closeProjects();
      closePlugins();

      server.exit();
    }catch(IllegalStateException ise){
      // workspace already closed.
    }catch(Exception e){
      log.error("Error shutting down eclim:", e);
    }
    return Services.getMessage("shutdown");
  }

  /**
   * Close all projects.
   */
  public void closeProjects ()
    throws Exception
  {
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for(int ii = 0; ii < projects.length; ii++){
      IJavaProject javaProject = JavaCore.create(projects[ii]);
      if(javaProject.exists()){
        if(javaProject.hasUnsavedChanges()){
          javaProject.save(null, false);
        }
// don't need to close the projects... eclipse doesn't appear to do it and as a
// result the projects remain open for the next application start.
        //javaProject.close();
      }
      //projects[ii].close(null);
    }
  }

  /**
   * Shutdown core plugins.
   */
  public void closePlugins ()
    throws Exception
  {
    EclimPlugin.getDefault().stop(null);
    JavaCore.getJavaCore().stop(null);
    ResourcesPlugin.getWorkspace().save(true, null);
    ResourcesPlugin.getPlugin().shutdown();
    ResourcesPlugin.getPlugin().stop(null);
  }
}
