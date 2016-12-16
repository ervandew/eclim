/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.dependency;

import org.eclim.Services;
import org.eclim.annotation.Command;
import org.eclim.command.CommandException;
import org.eclim.command.CommandException.ErrorType;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.logging.Logger;
import org.eclim.plugin.core.command.AbstractCommand;
import org.eclim.plugin.core.util.PathUtil;
import org.eclim.plugin.core.util.PathUtilException;

@Command(
      name = "remove_dependency",
      options =
        "REQUIRED p project ARG," +
        "REQUIRED f relativeFilePath ARG"
)

/**
 * Command to remove a dependency entry from your eclipse '.classpath' file of
 * the <code>project</code>.
 *
 * Example use case: You first delete a example.jar file from the file system
 * over the {@code FileDeleteCommand} and then remove the file path from your
 * .classpath file such that eclipse knows that the dependency is no more there.
 *
 * Warning: you have to update the <code>project</code> after calling this
 * command. Do this over the 'project_update' command.
 *
 * @author Lukas Roth
 *
 */
public class RemoveDependencyCommand extends AbstractCommand
{
  private static final Logger logger = Logger
      .getLogger(RemoveDependencyCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
      throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String relativeFilePath = commandLine.getValue(Options.FILE_OPTION);
    return removeDependency(projectName, relativeFilePath);
  }

  public Object removeDependency(String projectName,
      String relativeFilePath)
  {
    String absoluteFilePath;
    String projectPath;
    try {
      absoluteFilePath = PathUtil.getAbsolutePath(projectName, relativeFilePath);
      projectPath = PathUtil.getProjectPath(projectName);
    } catch (PathUtilException e) {
      return new CommandException(e, ErrorType.CLIENT_ERROR);
    }
    try {
      callClasspathFileManipulator(absoluteFilePath,
          projectPath + "/" + ".classpath");
    } catch (ClasspathFileManipulatorException e) {
      logger.error(Services.getMessage("dependency.classpath.error"), e);
      return new CommandException(
          Services.getMessage("dependency.classpath.error"),
          ErrorType.SYSTEM_ERROR);
    }
    return Services.getMessage("dependency.remove.jar.success", relativeFilePath);
  }

  private void callClasspathFileManipulator(String dependencyFilePath,
      String classpathFilePath)
      throws ClasspathFileManipulatorException
  {
    SimpleClasspathFileManipulator classpathFileManipulator
        = new SimpleClasspathFileManipulator();
    classpathFileManipulator.removeJarDependency(dependencyFilePath,
        classpathFilePath);
  }
}
