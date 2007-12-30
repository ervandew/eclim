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

import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;

/**
 * Simple implementation of IValidationContext.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ValidationContext
  implements IValidationContext
{
  private String[] uris;

  public ValidationContext (String uri)
  {
    this.uris = new String[]{uri};
  }

  /**
   * {@inheritDoc}
   * @see IValidationContext#getURIs()
   */
  public String[] getURIs()
  {
    return uris;
  }

  /**
   * {@inheritDoc}
   * @see IValidationContext#loadModel(String)
   */
  public Object loadModel (String name)
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see IValidationContext#loadModel(String,Object[])
   */
  public Object loadModel (String name, Object[] params)
  {
    return null;
  }
}
