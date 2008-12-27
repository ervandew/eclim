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
  public Object loadModel(String name)
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see IValidationContext#loadModel(String,Object[])
   */
  public Object loadModel(String name, Object[] params)
  {
    return null;
  }
}
