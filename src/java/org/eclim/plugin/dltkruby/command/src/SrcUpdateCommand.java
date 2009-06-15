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
package org.eclim.plugin.dltkruby.command.src;

import java.io.InputStreamReader;

import org.eclim.annotation.Command;

import org.eclim.plugin.dltk.command.src.AbstractSrcUpdateCommand;

import org.eclim.util.IOUtils;

import org.eclipse.core.resources.IFile;

import org.eclipse.dltk.ruby.internal.parsers.jruby.DLTKRubyParser;

/**
 * Command to update and optionally validate a ruby src file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "ruby_src_update",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL v validate NOARG"
)
public class SrcUpdateCommand
  extends AbstractSrcUpdateCommand
{
  /**
   * {@inheritDoc}
   * @see AbstractSrcUpdateCommand#parse(String,IFile,Reporter)
   */
  @Override
  protected void parse(
      String filename, IFile file, AbstractSrcUpdateCommand.Reporter reporter)
    throws Exception
  {
    DLTKRubyParser parser = new DLTKRubyParser();
    InputStreamReader reader = null;
    try{
      reader = new InputStreamReader(file.getContents());
      parser.parse(filename, reader, reporter);
    }finally{
      IOUtils.closeQuietly(reader);
    }
  }
}
