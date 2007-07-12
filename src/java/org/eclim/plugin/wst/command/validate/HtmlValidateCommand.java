/**
 * Copyright (c) 2005 - 2006
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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;

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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class HtmlValidateCommand
  extends WstValidateCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      final String file = FilenameUtils.concat(
          ProjectUtils.getPath(project),
          _commandLine.getValue(Options.FILE_OPTION));

      // eclipse wst html valiation... currently not very good.
      /*IProject iproject = ProjectUtils.getProject(project, true);
      final String projectPath = FilenameUtils.getFullPath(
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

      ArrayList results = new ArrayList();
      String[] lines = StringUtils.split(out.toString(), '\n');
      for (int ii = 0; ii < lines.length; ii++){
        if(accept(lines[ii])){
          Error error = parseError(file, lines[ii]);
          if(error != null){
            results.add(error);
          }
        }
      }

      return super.filter(_commandLine,
          (Error[])results.toArray(new Error[results.size()]));
    }catch(Throwable t){
      return t;
    }
  }

  /**
   * Determines if the supplied error line should be accepted.
   *
   * @param line The error line.
   * @return True if the line is accepted, false otherwise.
   */
  private boolean accept (String line)
  {
    return
      line.indexOf(": inserting") == -1 &&
      line.indexOf(": trimming") == -1;
  }

  /**
   * Parse the supplied error line.
   *
   * @param file The file that was processed.
   * @param line The error line.
   * @return The Error result.
   */
  private Error parseError (String file, String line)
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
