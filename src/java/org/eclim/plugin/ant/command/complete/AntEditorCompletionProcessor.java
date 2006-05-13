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
package org.eclim.plugin.ant.command.complete;

import org.eclim.eclipse.jface.text.contentassist.DummyContentAssistantExtension2;

import org.eclipse.ant.internal.ui.model.AntModel;

/**
 * Extension to AntEditorCompletionProcessor to combat issues setting the
 * content assistant.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
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
