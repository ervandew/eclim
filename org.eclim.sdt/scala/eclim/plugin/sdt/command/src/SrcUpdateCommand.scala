/**
 * Copyright (C) 2011 - 2017 Eric Van Dewoestine
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
package eclim.plugin.sdt.command.src

import scala.collection.JavaConverters._

import scala.collection.mutable.ListBuffer

import eclim.plugin.sdt.util.ScalaUtils

import org.apache.commons.lang.StringUtils

import org.eclim.annotation.Command

import org.eclim.command.CommandLine
import org.eclim.command.Error
import org.eclim.command.Options

import org.eclim.plugin.core.command.AbstractCommand

import org.eclim.plugin.core.util.ProjectUtils

import org.eclim.util.file.FileOffsets

import org.eclipse.core.resources.IncrementalProjectBuilder

import org.eclipse.core.runtime.NullProgressMonitor

/**
 * Command that updates the requested scala src file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "scala_src_update",
  options = "REQUIRED p project ARG,REQUIRED f file ARG,OPTIONAL v validate NOARG,OPTIONAL b build NOARG")
class SrcUpdateCommand
  extends AbstractCommand
{
  override
  def execute(commandLine: CommandLine): Object = {
    val file = commandLine.getValue(Options.FILE_OPTION)
    val projectName = commandLine.getValue(Options.PROJECT_OPTION)
    val project = ProjectUtils.getProject(projectName)
    val src = ScalaUtils.getSourceFile(projectName, file)
    val validate = commandLine.hasOption(Options.VALIDATE_OPTION)

    if (validate){
      // forcing a build prevents race condition after updating the changed
      // file before the problems are available.
      project.build(
          IncrementalProjectBuilder.INCREMENTAL_BUILD,
          new NullProgressMonitor)

      val problems = src.getProblems()

      val errors: ListBuffer[Error] = ListBuffer()
      if (problems != null){
        val path = ProjectUtils.getFilePath(project, file)
        val offsets = FileOffsets.compile(path)
        for(problem <- problems){
          val lineColumn = offsets.offsetToLineColumn(problem.getSourceStart)
          errors += new Error(
              problem.getMessage,
              path,
              lineColumn(0),
              lineColumn(1),
              problem.isWarning)
        }
      }
      errors.asJava
    }else{
      StringUtils.EMPTY
    }
  }
}
