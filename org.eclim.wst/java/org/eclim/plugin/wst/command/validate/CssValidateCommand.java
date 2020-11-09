/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.wst.command.validate;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.w3c.css.css.DocumentParser;
import org.w3c.css.css.StyleSheet;

import org.w3c.css.parser.CssError;

import org.w3c.css.parser.analyzer.TokenMgrError;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Warning;

/**
 * Command to validate a css file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "css_validate",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class CssValidateCommand
  extends WstValidateCommand
{
  private static final String URI_PREFIX = "file:/";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    if (!project.exists()){
      throw new RuntimeException(
          Services.getMessage("project.not.found", projectName));
    }
    String uri = toUri(projectName, commandLine.getValue(Options.FILE_OPTION));

    ApplContext context = new ApplContext("en");
    // possible values: css1, css2, css21, css3, svg, svgbasic, svgtiny
    context.setCssVersion("css3");
    // possible values: all, aural, braille, embossed, handheld, print,
    // projection, screen, tty, tv, presentation
    context.setMedium("all");

    ArrayList<Error> errors = new ArrayList<Error>();
    try{
      DocumentParser parser = new DocumentParser(context, uri);
      StyleSheet css = parser.getStyleSheet();
      css.findConflicts(context);

      for(CssError error : css.getErrors().getErrors()){
        if (!StringUtils.EMPTY.equals(error.getException().getMessage())) {
          errors.add(new Error(
                error.getException().getMessage(),
                toFile(error.getSourceFile()),
                error.getLine(),
                1,
                false
          ));
        }
      }

      for(Warning warning : css.getWarnings().getWarnings()){
        if (!StringUtils.EMPTY.equals(warning.getWarningMessage())) {
          errors.add(new Error(
                warning.getWarningMessage(),
                toFile(warning.getSourceFile()),
                warning.getLine(),
                1,
                true
          ));
        }
      }
    }catch(TokenMgrError tme){
      errors.add(new Error(
            tme.getMessage(),
            super.toFile(uri), // need to use the super version
            1, //tme.getErrorLine(), FIXME: parse the line/col out of the message.
            1,
            false
      ));
    }

    return errors;
  }

  @Override
  protected String toFile(String uri)
  {
    uri = uri.startsWith(URI_PREFIX) ?  uri.substring(URI_PREFIX.length()) : uri;
    uri = uri.replaceFirst("^/+", "/");
    return uri;
  }
}
