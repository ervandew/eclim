package org.vimplugin.listeners;

import org.vimplugin.VimEvent;
import org.vimplugin.VimException;
import org.vimplugin.VimPlugin;
import org.vimplugin.VimServer;
import org.vimplugin.editors.VimEditor;

/**
 * User switched to a different buffer.
 */
public class BufferEnter
  implements IVimListener
{
  /**
   * Updates the eclipse tab for the newly focused buffer.
   *
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   */
  public void handleEvent(final VimEvent ve) throws VimException {
    String event = ve.getEvent();
    String argument = null;

    // vim has a fileClosed event, but it is not implemented.
    if (event.equals("keyCommand") &&
        (argument = ve.getArgument(0)).startsWith("\"bufferEnter ")){
      String filePath = argument.substring(13, argument.length() - 1);
      filePath = filePath.substring(1, filePath.length() - 1);
      VimServer server = VimPlugin.getDefault()
        .getVimserver(ve.getConnection().getVimID());
      for (VimEditor veditor : server.getEditors()) {
        if (veditor.getBufferID() == ve.getBufferID() || server.isEmbedded()) {
          veditor.setTitleTo(filePath);
        }
      }
    }
  }
}
