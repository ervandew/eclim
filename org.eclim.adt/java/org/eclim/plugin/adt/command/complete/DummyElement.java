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
package org.eclim.plugin.adt.command.complete;

import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.DOMException;

/**
 * Dummy Element/Node implementation
 *
 * @author Daniel Leong
 */
public class DummyElement
  extends IIOMetadataNode
{

  String text;

  public DummyElement(final String nodeName)
  {
    super(nodeName);
  }

  @Override
  public void setTextContent(final String textContent)
      throws DOMException
  {
    this.text = textContent;
  }

  @Override
  public String getTextContent()
    throws DOMException
  {
    return this.text;
  }

}
