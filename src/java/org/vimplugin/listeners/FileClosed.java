package org.vimplugin.listeners;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.vimplugin.VimConnection;
import org.vimplugin.VimEvent;
import org.vimplugin.VimException;
import org.vimplugin.VimPlugin;
import org.vimplugin.VimServer;
import org.vimplugin.editors.VimEditor;

/**
 * The File was closed.
 */
public class FileClosed
  implements IVimListener
{
  /**
   * Closes the associated eclipse tab when a vim tab is closed.
   *
   * @see org.vimplugin.listeners.IVimListener#handleEvent(org.vimplugin.VimEvent)
   */
  public void handleEvent(final VimEvent ve) throws VimException {
    String event = ve.getEvent();
    String argument = null;

    // vim has a fileClosed event, but it is not implemented.
    if (event.equals("keyCommand") &&
        (argument = ve.getArgument(0)).startsWith("\"fileClosed ")){
      IPath filePath = new Path(argument.substring(12, argument.length() - 1));
      VimPlugin plugin = VimPlugin.getDefault();
      VimConnection vc = ve.getConnection();
      VimServer server = plugin.getVimserver(vc.getVimID());
      for (VimEditor editor : server.getEditors()){
        IPath location = editor.getSelectedFile().getRawLocation();
        if (filePath.equals(location)){
          editor.forceDispose();
        }
      }
    }
  }
}
