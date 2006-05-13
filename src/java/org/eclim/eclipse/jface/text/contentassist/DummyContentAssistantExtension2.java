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
package org.eclim.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

/**
 * Dummy implementation of IContentAssistantExtension2.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class DummyContentAssistantExtension2
  implements IContentAssistant, IContentAssistantExtension2
{
// IContentAssistant

  /**
   * {@inheritDoc}
   */
  public void install(ITextViewer textViewer)
  {
  }

  /**
   * {@inheritDoc}
   */
  public void uninstall()
  {
  }

  /**
   * {@inheritDoc}
   */
  public String showPossibleCompletions()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public String showContextInformation()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public IContentAssistProcessor getContentAssistProcessor(String contentType)
  {
    return null;
  }

// IContentAssistantExtension2

  /**
   * {@inheritDoc}
   */
  public void addCompletionListener(ICompletionListener listener)
  {
  }

  /**
   * {@inheritDoc}
   */
  public void removeCompletionListener(ICompletionListener listener)
  {
  }

  /**
   * {@inheritDoc}
   */
  public void setRepeatedInvocationMode(boolean cycling)
  {
  }

  /**
   * {@inheritDoc}
   */
  public void setShowEmptyList(boolean showEmpty)
  {
  }

  /**
   * {@inheritDoc}
   */
  public void setStatusLineVisible(boolean show)
  {
  }

  /**
   * {@inheritDoc}
   */
  public void setStatusMessage(String message)
  {
  }

  /**
   * {@inheritDoc}
   */
  public void setEmptyMessage(String message)
  {
  }
}
