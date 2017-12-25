/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;

/**
 * Dummy implementation of IContentAssistantExtension2.
 *
 * @author Eric Van Dewoestine
 */
public class DummyContentAssistantExtension2
  implements IContentAssistant, IContentAssistantExtension2
{
// IContentAssistant

  @Override
  public void install(ITextViewer textViewer)
  {
  }

  @Override
  public void uninstall()
  {
  }

  @Override
  public String showPossibleCompletions()
  {
    return null;
  }

  @Override
  public String showContextInformation()
  {
    return null;
  }

  @Override
  public IContentAssistProcessor getContentAssistProcessor(String contentType)
  {
    return null;
  }

// IContentAssistantExtension2

  @Override
  public void addCompletionListener(ICompletionListener listener)
  {
  }

  @Override
  public void removeCompletionListener(ICompletionListener listener)
  {
  }

  @Override
  public void setRepeatedInvocationMode(boolean cycling)
  {
  }

  @Override
  public void setShowEmptyList(boolean showEmpty)
  {
  }

  @Override
  public void setStatusLineVisible(boolean show)
  {
  }

  @Override
  public void setStatusMessage(String message)
  {
  }

  @Override
  public void setEmptyMessage(String message)
  {
  }
}
