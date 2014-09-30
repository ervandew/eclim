
package org.eclim.plugin.adt.command.complete;

import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.DOMException;

public class DummyElement
  extends IIOMetadataNode
{

  String text;

  public DummyElement(final String nodeName) {
    super(nodeName);
  }

  @Override
  public void setTextContent(String textContent) throws DOMException {
    this.text = textContent;
  }

  @Override
  public String getTextContent() throws DOMException {
    return this.text;
  }

}
