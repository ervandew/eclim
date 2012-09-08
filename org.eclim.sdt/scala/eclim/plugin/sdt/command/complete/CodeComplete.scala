/**
 * Copyright (C) 2011 - 2012 Eric Van Dewoestine
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
package eclim.plugin.sdt.command.complete

import java.text.Collator

import java.util.Comparator
import java.util.Locale

import scala.collection.JavaConversions

import scala.collection.mutable.ListBuffer

import scala.tools.eclipse.ScalaWordFinder

import scala.tools.eclipse.completion.CompletionProposal
import scala.tools.eclipse.completion.HasArgs
import scala.tools.eclipse.completion.ScalaCompletions

import eclim.plugin.sdt.util.ScalaUtils

import org.eclim.annotation.Command

import org.eclim.command.CommandLine
import org.eclim.command.Options

import org.eclim.plugin.core.command.AbstractCommand

import org.eclim.plugin.core.command.complete.CodeCompleteResult

import org.eclim.plugin.core.util.ProjectUtils

/**
 * Command which provides completion proposals.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "scala_complete",
  options = "REQUIRED p project ARG,REQUIRED f file ARG,REQUIRED o offset ARG,REQUIRED e encoding ARG,REQUIRED l layout ARG"
)
class CodeCompleteCommand
  extends AbstractCommand
{
  private val COMPLETIONS = new ScalaCompletions()
  private val COLLATOR = Collator.getInstance(Locale.US)

  override def execute(commandLine: CommandLine): Object = {
    val project = commandLine.getValue(Options.PROJECT_OPTION)
    val file = commandLine.getValue(Options.FILE_OPTION)
    val offset = getOffset(commandLine)
    val src = ScalaUtils.getSourceFile(project, file)

    val chars = ProjectUtils.getDocument(project, file).get.toCharArray
    val region = ScalaWordFinder.findCompletionPoint(chars, offset)
    val empty: List[CompletionProposal] = List()
    val proposals = src.withSourceFile {
      COMPLETIONS.findCompletions(region)(offset, src)
    }(empty)

    val results: ListBuffer[CodeCompleteResult] = ListBuffer()
    for (proposal <- proposals.sortBy(p => (-p.relevance, p.completion))){
      var completion = proposal.completion
      val description = proposal.display
      // for short desc, shorten all fully qualified Types to just the type.
      val shortDescription = description.replaceAll(
          "[a-zA-Z]\\w*[\\w.]*\\.(\\w+[^.])", "$1")
      completion = if (proposal.hasArgs == HasArgs.NonEmptyArgs)
        completion + "(" else completion
      results += new CodeCompleteResult(
        completion, shortDescription, description)
    }
    JavaConversions.bufferAsJavaList(results)
  }
}
