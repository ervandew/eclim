/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.format;

import java.io.ByteArrayOutputStream;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.core.formatter.CodeFormatter;

import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;

import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;

import org.eclipse.jface.text.Document;

import org.eclipse.text.edits.TextEdit;

/**
 * Command used to format source code in the way Source / Format menu does it.
 *
 * @author Anton Sharonov
 * @author Eric Van Dewoestine
 */
public class FormatCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int bByteOffset = commandLine.getIntValue(Options.BOFFSET_OPTION);
    int eByteOffset = commandLine.getIntValue(Options.EOFFSET_OPTION);
    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    DefaultCodeFormatter formatter =
      new DefaultCodeFormatter(JavaCore.getOptions());
    int kind = CodeFormatter.K_COMPILATION_UNIT |
      CodeFormatter.F_INCLUDE_COMMENTS;

    String source = src.getBuffer().getContents();
    String vimEncoding = "UTF-8";
    byte[] byteSource = source.getBytes(vimEncoding);
    ByteArrayOutputStream outStream = null;

    outStream = new ByteArrayOutputStream();
    outStream.write(byteSource, 0, bByteOffset);
    String sourcePrefix = outStream.toString(vimEncoding);

    outStream = new ByteArrayOutputStream();
    outStream.write(byteSource, bByteOffset, eByteOffset - bByteOffset);
    String sourceRoot = outStream.toString(vimEncoding);

    int bCharOffset = sourcePrefix.length();
    int eCharOffset = bCharOffset + sourceRoot.length();
    int charLength = eCharOffset - bCharOffset;

    String lineDelimiter = StubUtility.getLineDelimiterUsed(src);
    TextEdit edits = formatter.format(
        kind, source, bCharOffset, charLength, 0, lineDelimiter);
    if (edits == null) {
      return "no edits returned on attempt to format the source.";
    }
    Document document = new Document(src.getBuffer().getContents());
    edits.apply(document);
    src.getBuffer().setContents(document.get());
    src.save(null, false);

    return StringUtils.EMPTY;
  }
}
