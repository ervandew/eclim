/**
 * Copyright (c) 2005 - 2008
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
