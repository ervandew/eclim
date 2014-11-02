/**
 * Copyright (C) 2012 - 2014 Eric Van Dewoestine
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
package eclim.plugin.sdt.command.search

import java.io.File

import scala.collection.JavaConversions

import eclim.plugin.sdt.util.ScalaUtils

import org.eclim.annotation.Command

import org.eclim.command.CommandLine
import org.eclim.command.Options

import org.eclim.plugin.core.command.AbstractCommand

import org.eclim.plugin.core.util.ProjectUtils

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Position;

import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IPackageFragmentRoot

import org.eclipse.jdt.core.search.SearchMatch

import org.eclipse.jface.text.IRegion

import org.scalaide.core.internal.hyperlink.ScalaDeclarationHyperlinkComputer

import org.scalaide.core.internal.jdt.model.ScalaClassFile
import org.scalaide.core.internal.jdt.model.ScalaCompilationUnit

import org.scalaide.core.internal.jdt.search.ScalaSelectionEngine
import org.scalaide.core.internal.jdt.search.ScalaSelectionRequestor

import org.scalaide.util.ScalaWordFinder

/**
 * Command to handle scala search requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "scala_search",
  options = "REQUIRED n project ARG,REQUIRED f file ARG,REQUIRED o offset ARG,REQUIRED l length ARG,REQUIRED e encoding ARG"
)
class SearchCommand
  extends org.eclim.plugin.jdt.command.search.SearchCommand
{
  private val HYPERLINKS = new ScalaDeclarationHyperlinkComputer

  override def execute(commandLine: CommandLine): Object = {
    val project = commandLine.getValue(Options.NAME_OPTION)
    val file = commandLine.getValue(Options.FILE_OPTION)
    val length = commandLine.getIntValue(Options.LENGTH_OPTION)
    val offset = getOffset(commandLine)

    val src = ScalaUtils.getSourceFile(project, file)
    val results = scalaLinks(src, offset, length)
    if (results != null)
      JavaConversions.seqAsJavaList(results.filter(_ != null))
    else
      null
  }

  def scalaLinks(src: ScalaCompilationUnit, offset: Int, length: Int): List[Position] = {
    val region = ScalaWordFinder.findWord(src.getContents, offset)
    HYPERLINKS.findHyperlinks(src, region) match {
      case None             => null
      case Some(List())     => javaLinks(src, region.getOffset, region.getLength)
      case Some(hyperlinks) => hyperlinks.map(link => {
        val openableOrUnitField = link.getClass.getDeclaredField("openableOrUnit")
        val textField = link.getClass.getDeclaredField("text")
        val regionField = link.getClass.getDeclaredField("region")

        openableOrUnitField.setAccessible(true)
        textField.setAccessible(true)
        regionField.setAccessible(true)

        val unit = openableOrUnitField.get(link)
        var text = textField.get(link).asInstanceOf[String]
        if (text.indexOf("Open Declaration (") != -1){
          text = text.substring(18, text.length - 1)
        }
        val region = regionField.get(link).asInstanceOf[IRegion]
        val pos = region.getOffset
        val len = region.getLength

        unit match {
          case cu: ICompilationUnit => {
            Position.fromOffset(
              cu.getResource.getLocation.toOSString.replace('\\', '/'),
              text, pos, len)
          }
          case sc: ScalaClassFile => {
            var ss = scalaSource(sc)
            if (ss != null)
              Position.fromOffset(ss, text, pos, len)
            else
              null
          }
          case _ => null
        }
      })
    }
  }

  def javaLinks(src: ScalaCompilationUnit, offset: Int, length: Int): List[Position] = {
    val environment = src.scalaProject.newSearchableEnvironment()
    val requestor = new ScalaSelectionRequestor(environment.nameLookup, src)
    val engine = new ScalaSelectionEngine(
      environment, requestor, src.getJavaProject.getOptions(true))
    engine.select(src, offset, offset + length - 1)
    requestor.getElements.map((element: IJavaElement) =>
      createPosition(src.getJavaProject.getProject, new SearchMatch(
        element, SearchMatch.A_ACCURATE,
        JavaUtils.getElementOffset(element),
        0, null,
        JavaUtils.getPrimaryElement(element).getResource
      ))
    ).toList
  }

  def scalaSource(sc: ScalaClassFile): String = {
    val root = sc.getParent.getParent.asInstanceOf[IPackageFragmentRoot]
    val srcPath = root.getSourceAttachmentPath
    if (srcPath != null){
        var name = JavaUtils.getFullyQualifiedName(sc)
        var file = name.replace('.', File.separatorChar);
        var rootPath: String = null
        val project = root.getJavaProject().getProject()

        // determine if src path is project relative or file system absolute.
        if (srcPath.isAbsolute() &&
            project.getName().equals(srcPath.segment(0)))
        {
          rootPath = ProjectUtils.getFilePath(project, srcPath.toString())
        }else{
          rootPath = srcPath.toOSString()
        }
        FileUtils.toUrl(rootPath + File.separator + file + ".scala");
    } else {
      null
    }
  }
}
