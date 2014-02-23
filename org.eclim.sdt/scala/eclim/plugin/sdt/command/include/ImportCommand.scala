/**
 * Copyright (C) 2011 - 2013 Eric Van Dewoestine
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
package eclim.plugin.sdt.command.include

import eclim.plugin.sdt.util.ScalaUtils

import org.eclim.annotation.Command
import org.eclim.command.CommandLine
import org.eclim.command.Options
import org.eclim.plugin.core.command.AbstractCommand

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.core.search.TypeNameMatch
import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector

import scala.collection.JavaConversions

/**
 * Command which provides import proposals.
 *
 * @author Fangmin Lv
 */
@Command(
  name = "scala_import",
  options = "REQUIRED p project ARG,REQUIRED f file ARG,REQUIRED o offset ARG,REQUIRED e encoding ARG,REQUIRED t type ARG"
)
class ImportCommand
  extends AbstractCommand
{
  override def execute(commandLine: CommandLine): Object = {
    val project = commandLine.getValue(Options.PROJECT_OPTION)
    val file = commandLine.getValue(Options.FILE_OPTION)
    val offset = getOffset(commandLine)
    val missType = commandLine.getValue(Options.TYPE_OPTION)
    val src = ScalaUtils.getSourceFile(project, file)

    val resultCollector = new java.util.ArrayList[TypeNameMatch]
    val scope = SearchEngine.createJavaSearchScope(Array[IJavaElement](src.getJavaProject))
    val typesToSearch = Array(missType.toArray)
    new SearchEngine().searchAllTypeNames(null, typesToSearch, scope,
        new TypeNameMatchCollector(resultCollector), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor)
    val ret = JavaConversions.asScalaBuffer(resultCollector) map { typeFound => typeFound.getFullyQualifiedName() }
    JavaConversions.bufferAsJavaList(ret)
  }
}
