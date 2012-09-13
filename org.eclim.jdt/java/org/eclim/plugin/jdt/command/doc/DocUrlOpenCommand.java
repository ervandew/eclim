/**
 * Copyright (C) 2012  Eric Van Dewoestine
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

import java.net.URL;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.jdt.internal.ui.actions.OpenBrowserUtil;

import org.eclipse.swt.widgets.Display;

/**
 * Command to open a javadoc url.
 *
 * Core goal of this command is to allow opening of javadocs from an archive.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "java_doc_url_open", options = "REQUIRED u url ARG")
public class DocUrlOpenCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String url = commandLine.getValue(Options.URL_OPTION);
    OpenBrowserUtil.openExternal(new URL(url), Display.getDefault());
    return null;
  }
}
