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
package org.eclim.plugin.wst.command.complete;

import org.eclim.annotation.Command;

import org.eclipse.wst.css.ui.StructuredTextViewerConfigurationCSS;

import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;

/**
 * Command to handle css code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "css_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class CssCodeCompleteCommand
  extends WstCodeCompleteCommand
{
  @Override
  protected Class<? extends StructuredTextViewerConfiguration>
    getViewerConfigurationClass()
  {
    return StructuredTextViewerConfigurationCSS.class;
  }
}
