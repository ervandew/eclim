/**
 * Copyright (c) 2005 - 2008
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
package org.eclim.plugin.wst.command.validate;

import java.util.ArrayList;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.util.ProjectUtils;

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
 * @version $Revision$
 */
public class CssValidateCommand
  extends WstValidateCommand
{
  private static final String URI_PREFIX = "file:/";

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    if (!project.exists()){
      throw new RuntimeException(
          Services.getMessage("project.not.found", projectName));
    }
    String uri = toUri(projectName, _commandLine.getValue(Options.FILE_OPTION));

    ApplContext context = new ApplContext("en");
    // possible values: css1, css2, css21, css3, svg, svgbasic, svgtiny
    context.setCssVersion("css21");
    // possible values: all, aural, braille, embossed, handheld, print,
    // projection, screen, tty, tv, presentation
    context.setMedium("all");

    ArrayList<Error> errors = new ArrayList<Error>();
    try{
      DocumentParser parser = new DocumentParser(context, uri);
      StyleSheet css = parser.getStyleSheet();
      css.findConflicts(context);

      for(CssError error : css.getErrors().getErrors()){
        errors.add(new Error(
              error.getException().getMessage(),
              toFile(error.getSourceFile()),
              error.getLine(),
              1,
              false
        ));
      }

      for(Warning warning : css.getWarnings().getWarnings()){
        errors.add(new Error(
              warning.getWarningMessage(),
              toFile(warning.getSourceFile()),
              warning.getLine(),
              1,
              true
        ));
      }
    }catch(TokenMgrError tme){
      errors.add(new Error(
            tme.getMessage(),
            super.toFile(uri), // need to use the super version
            tme.getErrorLine(),
            1,
            false
      ));
    }

    return ErrorFilter.instance.filter(_commandLine, errors);
  }

  /**
   * {@inheritDoc}
   * @see WstValidateCommand#toFile(String)
   */
  @Override
  protected String toFile (String uri)
  {
    return uri.startsWith(URI_PREFIX) ?  uri.substring(URI_PREFIX.length()) : uri;
  }
}
