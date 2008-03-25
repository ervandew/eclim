/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

/**
 * Implementation of IReporter to collect validation messages.
 */
public class Reporter
  implements IReporter
{
  private ArrayList<IMessage> messages = new ArrayList<IMessage>();

  /**
   * Gets the list of accumulated messages.
   *
   * @return List of messages.
   */
  public List<IMessage> getMessages ()
  {
    return messages;
  }

  public void addMessage (IValidator origin, IMessage message)
  {
    messages.add(message);
  }

  public void displaySubtask (IValidator validator, IMessage message)
  {
  }

  public boolean isCancelled ()
  {
    return false;
  }

  public void removeAllMessages (IValidator origin)
  {
  }

  public void removeAllMessages (IValidator origin, Object object)
  {
  }

  public void removeMessageSubset (IValidator validator, Object obj, String groupName)
  {
  }
}
