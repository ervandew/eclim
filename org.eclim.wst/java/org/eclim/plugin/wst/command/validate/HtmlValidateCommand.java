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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.IOUtils;

/*import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IProject;

import org.eclipse.wst.html.internal.validation.HTMLValidationReporter;
import org.eclipse.wst.html.internal.validation.HTMLValidator;

import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import org.eclipse.wst.xml.core.internal.validation.XMLValidator;

import org.eclipse.wst.xml.core.internal.validation.core.ValidationMessage;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationReport;*/

import org.w3c.tidy.Tidy;

/**
 * Command to validate html files.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "html_validate",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class HtmlValidateCommand
  extends WstValidateCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    final String file = ProjectUtils.getFilePath(
        project, commandLine.getValue(Options.FILE_OPTION));

    // eclipse wst html valiation... currently not very good.
    /*IProject iproject = ProjectUtils.getProject(project, true);
    final String projectPath = FileUtils.getFullPath(
        ProjectUtils.getPath(iproject));

    Reporter reporter = new Reporter();
    HTMLValidator validator = new HTMLValidator();
    IValidationContext context = new IValidationContext(){
      public String[] getURIs(){
        return new String[]{file.substring(projectPath.length())};
      }
      public Object loadModel (String name){
        return null;
      }
      public Object loadModel (String name, Object[] params){
        return null;
      }
    };
    validator.validate(context, reporter);

    FileOffsets offsets = FileOffsets.compile(file);

    ArrayList results = new ArrayList();
    for (Iterator ii = reporter.getMessages().iterator(); ii.hasNext();){
      IMessage message = (IMessage)ii.next();
      int[] lineColumn = offsets.offsetToLineColumn(message.getOffset());
      results.add(new Error(
          message.getText(),
          file,
          lineColumn[0],
          lineColumn[1],
          false
      ));
    }*/

    // jtidy validation, currently better than wst version.
    Tidy tidy = new Tidy();
    tidy.setEmacs(true);
    tidy.setOnlyErrors(true);
    tidy.setQuiet(true);
    FileInputStream in = new FileInputStream(file);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(out, true);
    try{
      tidy.setErrout(writer);
      tidy.parse(in, out);
    }finally{
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }

    ArrayList<Error> results = new ArrayList<Error>();
    String[] lines = StringUtils.split(out.toString(), '\n');
    for (int ii = 0; ii < lines.length; ii++){
      if(accept(lines[ii])){
        Error error = parseError(file, lines[ii]);
        if(error != null){
          results.add(error);
        }
      }
    }

    return results;
  }

  /**
   * Determines if the supplied error line should be accepted.
   *
   * @param line The error line.
   * @return True if the line is accepted, false otherwise.
   */
  private boolean accept(String line)
  {
    return
      line.indexOf(": inserting") == -1 &&
      line.indexOf(": trimming") == -1 &&
      line.indexOf("unknown attribute") == -1 &&
      line.indexOf("missing <!DOCTYPE> declaration") == -1;
  }

  /**
   * Parse the supplied error line.
   *
   * @param file The file that was processed.
   * @param line The error line.
   * @return The Error result.
   */
  private Error parseError(String file, String line)
    throws Exception
  {
    String[] parts = StringUtils.split(line, ':');

    if (parts.length == 5){
      int lnum = Integer.parseInt(parts[1].replaceAll(",", ""));
      int cnum = Integer.parseInt(parts[2].replaceAll(",", ""));
      return new Error(
        parts[4].trim(), file, lnum, cnum,
        parts[3].trim().equals("Warning")
      );
    }
    return null;
  }
}
