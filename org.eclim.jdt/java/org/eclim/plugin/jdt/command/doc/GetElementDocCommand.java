/**
 * Copyright (C) 2012 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.doc;

import java.io.StringReader;

import java.lang.reflect.Method;

import java.net.URI;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.IOUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;

import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;

import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks;

import org.eclipse.jface.internal.text.html.HTML2TextReader;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;

/**
 * Command to retrieve the java docs for an element.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_element_doc",
  options =
    "OPTIONAL p project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL l length ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL u url ARG," +
    "OPTIONAL h html NOARG"
)
public class GetElementDocCommand
  extends AbstractCommand
{
  private static final Pattern LINKS = Pattern.compile(
      "<a\\s+[^>]*?href=(['\"])(.*?)\\1[^>]*>\\s*(.*?)</a>");

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    IJavaElement[] elements = null;
    Object url = commandLine.getRawValue(Options.URL_OPTION);
    if (url != null){
      IJavaElement target = JavaElementLinks.parseURI(new URI((String)url));
      if (target != null){
        elements = new IJavaElement[]{target};
      }
    }else{
      String project = commandLine.getValue(Options.PROJECT_OPTION);
      String file = commandLine.getValue(Options.FILE_OPTION);
      int offset = getOffset(commandLine);
      int length = commandLine.getIntValue(Options.LENGTH_OPTION);

      ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
      elements = src.codeSelect(offset, length);
    }

    if(elements == null || elements.length == 0){
      return null;
    }

    Method getHoverInfo = JavadocHover.class.getDeclaredMethod(
        "getHoverInfo",
        IJavaElement[].class,
        ITypeRoot.class,
        IRegion.class,
        JavadocBrowserInformationControlInput.class);
    getHoverInfo.setAccessible(true);
    JavadocBrowserInformationControlInput info =
      (JavadocBrowserInformationControlInput)
      getHoverInfo.invoke(null, elements, null, null, null);
    if (info == null){
      return null;
    }

    if(commandLine.hasOption(Options.HTML_OPTION)){
      return info.getHtml();
    }

    ArrayList<HashMap<String,String>> links =
      new ArrayList<HashMap<String,String>>();
    Matcher matcher = LINKS.matcher(info.getHtml());
    StringBuffer buffer = new StringBuffer();
    int index = 0;
    while (matcher.find()){
      String href = matcher.group(2);
      String text = matcher.group(3);
      HashMap<String,String> link = new HashMap<String,String>();
      link.put("href", href);
      link.put("text", text);
      links.add(link);
      matcher.appendReplacement(buffer, "|$3[" + (index++) + "]|");
    }
    matcher.appendTail(buffer);

    String html = buffer.toString();
    String result = IOUtils.toString(
        new HTML2TextReader(
          new StringReader(html != null ? html : StringUtils.EMPTY),
          new TextPresentation()));
    // remove \r for windows
    result = result.replaceAll("\r", "");
    // compact excessive spacing between sig and doc.
    result = result.replaceFirst("\n\n\n", "\n\n");

    HashMap<String,Object> response = new HashMap<String,Object>();
    response.put("text", result);
    response.put("links", links);
    return response;
  }
}
