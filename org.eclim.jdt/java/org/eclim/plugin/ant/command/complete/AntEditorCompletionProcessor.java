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
package org.eclim.plugin.ant.command.complete;

import org.eclim.eclipse.jface.text.contentassist.DummyContentAssistantExtension2;

import org.eclipse.ant.internal.ui.model.AntModel;

/**
 * Extension to AntEditorCompletionProcessor to combat issues setting the
 * content assistant.
 *
 * @author Eric Van Dewoestine
 */
public class AntEditorCompletionProcessor
  extends org.eclipse.ant.internal.ui.editor.AntEditorCompletionProcessor
{
  public AntEditorCompletionProcessor (AntModel model)
  {
    super(model);
    super.fContentAssistant = new DummyContentAssistantExtension2();
  }
}
