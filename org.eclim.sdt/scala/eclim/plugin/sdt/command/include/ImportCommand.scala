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
package eclim.plugin.sdt.command.include

import scala.collection.JavaConverters._

import scala.tools.refactoring.implementations.AddImportStatement

import eclim.plugin.sdt.util.ScalaUtils

import org.eclim.annotation.Command

import org.eclim.command.CommandLine
import org.eclim.command.Options

import org.eclim.plugin.core.command.AbstractCommand

import org.eclim.plugin.core.util.ProjectUtils

import org.eclim.plugin.jdt.command.include.ImportUtils

import org.eclim.util.file.Position;

import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.jdt.core.IJavaElement

import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.core.search.TypeNameMatch

import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;

import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector

import org.scalaide.core.compiler.IScalaPresentationCompiler.Implicits._

import org.scalaide.core.internal.jdt.model.ScalaSourceFile

import org.scalaide.util.ScalaWordFinder

import org.scalaide.util.internal.eclipse.TextEditUtils

/**
 * Command to add an import to a scala source file.
 *
 * @author Fangmin Lv
 * @author Eric Van Dewoestine
 */
@Command(
  name = "scala_import",
  options = "REQUIRED p project ARG,REQUIRED f file ARG,REQUIRED o offset ARG,REQUIRED e encoding ARG,OPTIONAL t type ARG"
)
class ImportCommand
  extends AbstractCommand
{
  // TODO:
  //   - insert the import in order
  //   - add support for package grouping
  override def execute(commandLine: CommandLine): Object = {
    val projectName = commandLine.getValue(Options.PROJECT_OPTION)
    val file = commandLine.getValue(Options.FILE_OPTION)
    val offset = getOffset(commandLine)
    val importType = commandLine.getValue(Options.TYPE_OPTION)
    val src = ScalaUtils.getSourceFile(projectName, file)
    val project = src.getJavaProject.getProject

    if (importType == null){
      val region = ScalaWordFinder.findWord(src.getContents, offset)
      val searchType = src.getBuffer.getText(region.getOffset, region.getLength)
      val resultCollector = new java.util.ArrayList[TypeNameMatch]
      val scope = SearchEngine.createJavaSearchScope(Array[IJavaElement](src.getJavaProject))
      val typesToSearch = Array(searchType.toArray)
      new SearchEngine().searchAllTypeNames(
        null,
        typesToSearch,
        scope,
        new TypeNameMatchCollector(resultCollector),
        IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
        new NullProgressMonitor)

      val results = resultCollector.asScala.view.map { typeFound =>
        typeFound.getFullyQualifiedName()
      }.filter { fqn => !ImportUtils.isImportExcluded(project, fqn)}.sorted

      if (results.length == 1){
        addImport(file, offset, src, results.head)
      }else{
        results.asJava
      }
    }else{
      addImport(file, offset, src, importType)
    }
  }

  def addImport(file: String, offset: Int, src: ScalaSourceFile, imprt: String) = {
    // first see if the import already exists
    val existing = src.getImport(imprt)
    if (existing.exists){
      "Import already exists: " + imprt
    }else{
      var exception = None : Option[Throwable]
      val oldLength = src.getBuffer.getLength

      val changes = src.withSourceFile { (sourceFile, compiler) =>
        val r = compiler.askLoadedTyped(sourceFile, false)
        (r.get match {
          case Right(error) =>
            exception = Some(error)
            None
          case _ =>
            compiler.asyncExec {
              val refactoring = new AddImportStatement { val global = compiler }
              refactoring.addImport(src.file, imprt)
            } getOption()
        }) getOrElse Nil
      } getOrElse (Nil)

      val project = src.getJavaProject.getProject
      val document = ProjectUtils.getDocument(project, file)
      val ifile = ProjectUtils.getFile(project, file)
      val edit = TextEditUtils.createTextFileChange(ifile, changes, true).getEdit
      JavaElementUtil.applyEdit(src, edit, true, null)

      exception match {
        case Some(value) => throw value
        case None =>
          var newOffset = offset
          if (edit.getOffset < newOffset){
            newOffset += src.getBuffer.getLength - oldLength
          }
          Position.fromOffset(
              ProjectUtils.getFilePath(project, file), null, newOffset, 0);
      }
    }
  }
}
