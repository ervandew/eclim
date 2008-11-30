/*
 * Vimplugin
 *
 * Copyright (c) 2007 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin;

import org.vimplugin.editors.AbstractVimEditor;

/**
 * Resembles an event thrown by vim and caught by various listeners in vimplugin.
 */
public class VimEvent {

  /** The complete line vim threw.  */
  private final String line;

  /** the connection this event came from */
  private final VimConnection connection;

  /** Simply sets both private attributes. */
  public VimEvent(String _line,VimConnection _connection) {
    //TODO: pass at init? and store instead of at get methods.
    line = _line;
    connection = _connection;
  }

  /**
   * The generic form of an event is: "bufID:name=123 arg1 arg2".
   *
   * @return the original line vim threw.
   */
  public String getLine() {
    return line;
  }

  /**
   * The name of the event, as specified under :help netbeans.
   *
   * @return the name of the event.
   * @throws VimException if the {@link #line line} cannot be parsed (wraps {@link IndexOutOfBoundsException})
   */
  public String getEvent() throws VimException {
    int beginIndex = line.indexOf(':');
    int endIndex = line.indexOf('=');
    try {
      return line.substring(beginIndex + 1, endIndex);
    } catch (IndexOutOfBoundsException iobe) {
      throw new VimException("Could not parse line \""+line+"\"",iobe);
    }
  }

  /**
   * the argument at the specified position (starting with 0).
   *
   * @param index
   * @return the argument at the specified position.
   * @throws VimException if the {@link #line line} cannot be parsed (wraps {@link IndexOutOfBoundsException})
   */
  public String getArgument(int index) throws VimException {
    int i = 0;
    int beginIndex = -1;
    while (i <= index) {
      beginIndex = line.indexOf(" ", beginIndex + 1);
      i++;
    }
    int endIndex = beginIndex;
    if (line.charAt(beginIndex + 1) == '"') {
      while (true) {
        endIndex = line.indexOf(" ", endIndex + 1);
        if (endIndex == -1
            || (line.charAt(endIndex - 1) == '"' && beginIndex != endIndex - 2))
          break;
      }
    } else
      endIndex = line.indexOf(" ", beginIndex + 1);
    if (endIndex == -1)
      endIndex = line.length();
    try {
      return line.substring(beginIndex + 1, endIndex);
    } catch (IndexOutOfBoundsException iobe) {
      throw new VimException("Could not parse line.",iobe);
    }

  }

  /**
   * returns the bufferID. This is set by vimplugin. It is not the vim-buffer! budIDs
   * start with one. Generic events have bufId of 0.
   *
   * @return the bufferID of this event.
   * @throws VimException if the number could not be parsed from the {@link #line line} (wraps {@link NumberFormatException})
   */
  public int getBufferID() throws VimException {
    int beginIndex = line.indexOf(':');
    try {
      return Integer.parseInt(line.substring(0, beginIndex));
    } catch (NumberFormatException nfe) {
      throw new VimException("Could not parse bufferId.",nfe);
    }
  }

  /**
   * Simple Getter.
   * @return the connection this event came from.
   */
  public VimConnection getConnection() {
    return connection;
  }

  /**
   * shortcut to get the AbstractVimEditor (on eclipse side) of this Event
   * @return
   */
  public AbstractVimEditor getEditor() throws VimException {
    return VimPlugin.getDefault().getVimserver(
        this.getConnection().getVimID()).getEditor(this.getBufferID());
  }
}
