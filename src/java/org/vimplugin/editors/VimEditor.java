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
package org.vimplugin.editors;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.reflect.Field;

import java.net.URI;

import org.eclim.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.preference.PreferenceDialog;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;

import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.ui.editors.text.TextEditor;

import org.eclipse.ui.texteditor.IDocumentProvider;

import org.vimplugin.DisplayUtils;
import org.vimplugin.VimConnection;
import org.vimplugin.VimPlugin;
import org.vimplugin.VimServer;

import org.vimplugin.preferences.PreferenceConstants;

/**
 * Provides an Editor to Eclipse which is backed by a Vim instance.
 */
public class VimEditor
  extends TextEditor
{
  private static final Logger logger = Logger.getLogger(VimEditor.class);

  /** ID of the VimServer. */
  protected int serverID;

  /** Buffer ID in Vim instance. */
  private int bufferID;

  protected Canvas editorGUI;

  protected IDocument document;

  protected VimDocumentProvider documentProvider;

  protected boolean dirty;

  protected boolean alreadyClosed = false;

  private IFile selectedFile;

  private Composite parent;

  /**
   * The field to grab for Windows/Win32.
   */
  public static final String win32WID = "handle";

  /**
   * The field to grab for Linux/GTK2.
   */
  public static final String linuxWID = "embeddedHandle";

  /**
   * a shell to open {@link MessageDialog MessageDialogs}.
   */
  private Shell shell;

  /**
   * The constructor.
   */
  public VimEditor() {
    super();
    bufferID = -1; // not really necessary but set it to an invalid buffer
    setDocumentProvider(documentProvider = new VimDocumentProvider());
    serverID = VimPlugin.getDefault().createVimServer();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    this.parent = parent;

    VimPlugin plugin = VimPlugin.getDefault();

    if (!plugin.gvimAvailable()) {
      shell = parent.getShell();
      MessageDialog dialog = new MessageDialog(
          shell, "Vimplugin", null,
          plugin.getMessage("gvim.not.found.dialog"),
          MessageDialog.ERROR,
          new String[]{IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0)
      {
        protected void buttonPressed(int buttonId) {
          super.buttonPressed(buttonId);
          if (buttonId == IDialogConstants.OK_ID){
            PreferenceDialog prefs = PreferencesUtil.createPreferenceDialogOn(
                shell, "org.vimplugin.preferences.VimPreferences", null, null);
            if (prefs != null){
              prefs.open();
            }
          }
        }
      };
      dialog.open();

      if (!plugin.gvimAvailable()) {
        throw new RuntimeException(plugin.getMessage("gvim.not.found"));
      }
    }

    boolean embed = plugin.getPreferenceStore()
      .getBoolean(PreferenceConstants.P_EMBED);
    if (embed && !plugin.gvimNbSupported()){
      throw new RuntimeException(plugin.getMessage("gvim.nb.not.enabled"));
    }

    //set some flags
    alreadyClosed = false;
    dirty = false;

    // nice background (only needed for external)
    editorGUI = new Canvas(parent, SWT.EMBEDDED);
    Color color = new Color(parent.getDisplay(), new RGB(0x10, 0x10, 0x10));
    editorGUI.setBackground(color);

    String projectPath = null;
    String filePath = null;
    IEditorInput input = getEditorInput();
    if (input instanceof IFileEditorInput){
      selectedFile = ((IFileEditorInput)input).getFile();

      IProject project = selectedFile.getProject();
      IPath path = project.getRawLocation();
      if(path == null){
        String name = project.getName();
        path = ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
        path = path.append(name);
      }
      projectPath = path.toPortableString();

      filePath = selectedFile.getRawLocation().toPortableString();
      if (filePath.toLowerCase().indexOf(projectPath.toLowerCase()) != -1){
        filePath = filePath.substring(projectPath.length() + 1);
      }
    }else{
      URI uri = ((IURIEditorInput)input).getURI();
      filePath = uri.toString().substring("file:".length());
      filePath = filePath.replaceFirst("^/([A-Za-z]:)", "$1");
    }

    if (filePath != null){
      //create a vim instance
      createVim(projectPath, editorGUI);

      //get bufferId
      bufferID = VimPlugin.getDefault().getNumberOfBuffers();
      bufferID++;
      VimPlugin.getDefault().setNumberOfBuffers(bufferID);

      //let vim edit the file.
      VimConnection vc = VimPlugin.getDefault().getVimserver(serverID).getVc();
      vc.command(bufferID, "editFile", "\"" + filePath + "\"");
      vc.command(bufferID, "startDocumentListen", "");
    }
  }

  /**
   * Create a vim instance figuring out if it should be external or embedded.
   *
   * @param workingDir
   * @param parent
   */
  private void createVim(String workingDir, Composite parent) {
    VimPlugin plugin = VimPlugin.getDefault();
    boolean embed = plugin.getPreferenceStore()
      .getBoolean(PreferenceConstants.P_EMBED);

    if (embed) {
      try {
        createEmbeddedVim(workingDir, parent);
      } catch (Exception e) {
        message(plugin.getMessage("embed.fallback"), e);
        createExternalVim(workingDir, parent);
      }
    } else {
      createExternalVim(workingDir, parent);
    }
    VimPlugin.getDefault().getVimserver(serverID).getEditors().add(this);
  }

  /**
   * Create an external Vim instance.
   *
   * @param workingDir
   * @param parent
   */
  private void createExternalVim(String workingDir, Composite parent) {
    VimPlugin.getDefault().getVimserver(serverID).start(workingDir);
  }

  /**
   * Creates an embedded Vim instance (platform-dependent!). Gets the Window
   * ID/Handle of the SWT Widget given, uses reflection since the code is
   * platform specific and this allows us to distribute just one plugin for
   * all platforms.
   *
   * @param workingDir
   * @param parent
   * @throws Exception
   */
  private void createEmbeddedVim(String workingDir, Composite parent)
    throws Exception
  {
    long wid = 0;

    Class<?> c = parent.getClass();
    Field f = null;

    if (Platform.getOS().equals(Platform.OS_LINUX)) {
      f = c.getField(VimEditor.linuxWID);
    } else if (Platform.getOS().equals(Platform.OS_WIN32)) {
      f = c.getField(VimEditor.win32WID);
    } else {
      f = c.getField(VimEditor.win32WID);
    }

    wid = f.getLong(parent);

    int h = parent.getClientArea().height;
    int w = parent.getClientArea().width;
    VimPlugin.getDefault().getVimserver(serverID).start(workingDir, wid);
    VimPlugin.getDefault().getVimserver(serverID).getVc()
      .command(bufferID, "setLocAndSize", h + " " + w);
  }

  /**
   * This function will be called by vimserver when it gets an event
   * disconnect or killed It doesn't ask to save modifications since vim takes
   * care of that.
   */
  public void forceDispose() {
    final VimEditor vime = this;
    Display display = getSite().getShell().getDisplay();
    display.asyncExec(new Runnable() {
      public void run() {
        if (vime != null && !vime.alreadyClosed) {
          vime.setDirty(false);
          vime.showBusy(true);
          vime.close(false);
          getSite().getPage().closeEditor(vime, false);
          // vime.alreadyClosed = true;
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.editors.text.TextEditor#dispose()
   */
  @Override
  public void dispose() {
    // TODO: calling close ourselves here doesn't seem right.
    // Note: this close raises NPE if gvim is not available. why is it
    // needed?
    close(true);

    if (editorGUI != null) {
      editorGUI.dispose();
      editorGUI = null;
    }

    document = null;

    super.dispose();
  }

  /**
   * This function will be called when we close the window If the
   * <code>save</code> is true then we call command save If the current
   * buffer is the last one the vim will be closed, else only the buffer will
   * be closed.
   */
  @Override
  public void close(boolean save) {
    if (this.alreadyClosed) {
      super.close(false);
      return;
    }

    VimPlugin plugin = VimPlugin.getDefault();

    alreadyClosed = true;
    VimServer server = plugin.getVimserver(serverID);
    server.getEditors().remove(this);

    if (save && dirty) {
      server.getVc().command(bufferID, "save", "");
      dirty = false;
      firePropertyChange(PROP_DIRTY);
    }

    if (plugin.getVimserver(serverID).getEditors().size() > 0) {
      server.getVc().command(bufferID, "close", "");
    } else {
      try {
        VimConnection conn = server.getVc();
        if (conn != null){
          server.getVc().function(bufferID, "saveAndExit", "");
        }
        plugin.stopVimServer(serverID);
      } catch (IOException e) {
        message(plugin.getMessage("server.stop.failed"), e);
      }
    }

    super.close(false);
  }

  /*
   * public void setHighlightRange(int offset,int length,boolean moveCursor){
   * System.out.println("--Highlighted-"+offset+length+"-- OK!");
   * if(moveCursor){ VimPlugin.getDefault().getVimserver().getVc().command(
   * bufferID, "setDot", "2/1"); } }
   */

  /**
   * We can't modify the file in the eclipse source viewer.. We use that only
   * for showing error messages...
   */
  @Override
  public boolean isEditable() {
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSave(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void doSave(IProgressMonitor monitor) {
    VimPlugin.getDefault().getVimserver(serverID).getVc()
      .command(bufferID, "save", "");
    dirty = false;
    firePropertyChange(PROP_DIRTY);
  }

  /**
   * Since we have a copy of edited text in document, we can perform saveAs
   * operation
   */
  @Override
  public void doSaveAs() {
    performSaveAs(null);
  }

  /**
   * Initialisation goes here..
   */
  @Override
  public void init(IEditorSite site, IEditorInput input)
    throws PartInitException
  {
    setSite(site);
    setInput(input);
    try {
      document = documentProvider.createDocument(input);
    } catch (Exception e) {
      VimPlugin plugin = VimPlugin.getDefault();
      message(plugin.getMessage("document.create.failed"), e);
    }
  }

  /**
   * Returns <code>true</code> if the file was modified, else return
   * <code>false</code>
   */
  @Override
  public boolean isDirty() {
    if (alreadyClosed){
      getSite().getPage().closeEditor(this, false);
    }
    return dirty;
  }

  /**
   * Returns <code>true</code> if save as is allowed, else return
   * <code>false</code>
   */
  @Override
  public boolean isSaveAsAllowed() {
    return true;
  }

  /**
   * Makes the present editor dirty. Thus the IDE knows that file was
   * modified. Asynchronous call of firePropertyChangeAsync.
   *
   * @param result
   */
  public void setDirty(boolean result) {
    dirty = result;

    final VimEditor vime = this;
    Display display = getSite().getShell().getDisplay();
    display.asyncExec(new Runnable() {
      public void run() {
        vime.firePropertyChangeAsync(PROP_DIRTY);
      }
    });
  }

  /**
   * Needed to allow calls from asyncronous threads. Simply delegates to
   * super.firePropertyChange
   *
   * @param prop the property to change
   */
  public void firePropertyChangeAsync(int prop) {
    super.firePropertyChange(prop);
  }

  /**
   * Sets focus (brings to top in Vim) to the buffer.. this function will be
   * called when user activates this editor window
   */
  @Override
  public void setFocus() {
    if (alreadyClosed) {
      getSite().getPage().closeEditor(this, false);
      return;
    }

    VimPlugin plugin = VimPlugin.getDefault();
    boolean embed = plugin.getPreferenceStore()
      .getBoolean(PreferenceConstants.P_EMBED);

    // let the parent composite handle setting the focus on the tab first.
    if (embed){
      parent.setFocus();
    }

    VimConnection conn = plugin.getVimserver(serverID).getVc();

    // get the current offset which "setDot" requires.
    String offset = "0";
    try{
      String cursor = conn.function(bufferID, "getCursor", "");
      if (cursor == null){
        // the only case that i know of where this happens is if the file is
        // open somewhere else and gvim is prompting the user as to how to
        // proceed.  Exit now or the gvim prompt will be sent to the background.
        return;
      }
    }catch(IOException ioe){
      logger.error("Unable to get cursor position.", ioe);
    }
    // Brings the corresponding buffer to top
    conn.command(bufferID, "setDot", offset);
    // Brings the vim editor window to top
    conn.command(bufferID, "raise", "");

    // to fully focus gvim, we need to simulate a mouse click.
    // Should this be optional, via a preference? There is the potential for
    // weirdness here.
    if (embed && parent.getDisplay().getActiveShell() != null){
      Rectangle bounds = parent.getBounds();
      final Point point = parent.toDisplay(
          bounds.x + 5, bounds.y + bounds.height - 25);
      new Thread(){
        public void run()
        {
          DisplayUtils.doClick(parent.getDisplay(), point.x, point.y, true);
        }
      }.start();
    }
  }

  /**
   * Sets the editor window title to path.. need to change path to file name..
   *
   * @param path
   */
  public void setTitleTo(String path) {
    String filename = path.substring(path.lastIndexOf(File.separator) + 1);
    setPartName(filename);
    setContentDescription(path);
    firePropertyChange(PROP_TITLE);
  }

  // /////// Handling Document content.. ///////////

  /**
   * Returns the document provider
   */
  @Override
  public IDocumentProvider getDocumentProvider() {
    return documentProvider;
  }

  /**
   * Sets the document content to given text
   *
   * @param text The text for the editor.
   */
  public void setDocumentText(String text) {
    document.set(text);
    setDirty(true);
  }

  /**
   * Remove the backslashes from the given string.
   *
   * @param text String to remove from
   * @return The processed string
   */
  private String removeBackSlashes(String text) {
    if (text.length() <= 2)
      return text;
    int offset = 0, length = text.length(), offset1 = 0;
    String newText = "";
    while (offset < length) {
      offset1 = text.indexOf('\\', offset);
      if (offset1 < 0) {
        newText = newText + text.substring(offset);
        break;
      }
      newText = newText + text.substring(offset, offset1)
          + text.substring(offset1 + 1, offset1 + 2);
      offset = offset1 + 2;
    }
    return newText;
  }

  /**
   * Inserts text into document FIXME Not working properly.. both
   * insertDocument and removeDocument have some implementation problems..
   * TODO: More details please?
   *
   * @param text The text to insert.
   * @param offset The offset to insert it at.
   */
  public void insertDocumentText(String text, int offset) {
    text = removeBackSlashes(text);

    //System.out.println(text + " INSERT " + offset);

    try {
      String first = document.get(0, offset);
      String last = document.get(offset, document.getLength() - offset);
      if (text.equals(new String("\\n"))) {
        //System.out.println("Insert new Line");
        first = first + System.getProperty("line.separator") + last;
      } else
        first = first + text + last;
      document.set(first);
      setDirty(true);
      //System.out.println(first);
    } catch (BadLocationException e) {
      VimPlugin plugin = VimPlugin.getDefault();
      message(plugin.getMessage("document.insert.failed"), e);
    }
  }

  /**
   * Removes text in the document
   *
   * @param offset The offset of the cursor in the text.
   * @param length The amount of text to remove.
   */
  public void removeDocumentText(int offset, int length) {
    //System.out.println(offset + " REMOVE " + length);
    try {
      String first = document.get(0, offset);
      String last = document.get(offset + length, document.getLength()
          - offset - length);
      first = first + last;
      //System.out.println(first);
      document.set(first);
      setDirty(true);
    } catch (BadLocationException e) {
      VimPlugin plugin = VimPlugin.getDefault();
      message(plugin.getMessage("document.remove.failed"), e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.editors.text.TextEditor#createActions()
   */
  @Override
  protected void createActions() {
    super.createActions();
  }

  /**
   * @return the bufferID
   */
  public int getBufferID() {
    return bufferID;
  }

  /**
   * simple one-liner to display error-messages using {@link MessageDialog}.
   * @param message the string to display
   */
  private void message(String message, Throwable e) {

    //convert stacktrace to string
    String stacktrace;
    StringWriter sw = null;
    PrintWriter pw = null;
    try {
      sw = new StringWriter();
      pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      stacktrace = sw.toString();
    } finally {
      try {
        if (pw != null)
          pw.close();
        if (sw != null)
          sw.close();
      } catch (IOException ignore) {
      }
    }

    MessageDialog.openError(shell, "Vimplugin", message + stacktrace);
  }

  private void message(String s) {
    MessageDialog.openError(shell, "Vimplugin", s);
  }
}
