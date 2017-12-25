/*
 * Vimplugin
 *
 * Copyright (c) 2007 - 2017 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin.editors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.reflect.Field;

import java.net.URI;

import org.eclim.logging.Logger;

import org.eclim.util.CommandExecutor;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;

import org.eclipse.jface.text.IDocument;

import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.ui.editors.text.TextEditor;

import org.eclipse.ui.internal.part.StatusPart;

import org.eclipse.ui.texteditor.AbstractTextEditor;
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

  private static final String ECLIMD_VIEW_ID = "org.eclim.eclipse.ui.EclimdView";

  /** ID of the VimServer. */
  private int serverID;

  /** Buffer ID in Vim instance. */
  private int bufferID;

  private Canvas editorGUI;
  private IFile selectedFile;
  private IDocument document;
  private VimViewer viewer;
  private VimDocumentProvider documentProvider;

  private boolean dirty;
  private boolean alreadyClosed = false;
  private long lastFocus = 0;

  private Composite parent;

  /**
   * The field to grab for Windows/Win32.
   */
  public static final String win32WID = "handle";

  /**
   * The field to grab for GTK2.
   */
  public static final String gtkWID = "embeddedHandle";

  /**
   * a shell to open {@link MessageDialog MessageDialogs}.
   */
  private Shell shell;

  private boolean embedded;
  private boolean tabbed;
  private boolean documentListen;

  /**
   * The constructor.
   */
  public VimEditor() {
    super();
    bufferID = -1; // not really necessary but set it to an invalid buffer
    setDocumentProvider(documentProvider = new VimDocumentProvider());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    this.parent = parent;
    this.shell = parent.getShell();

    VimPlugin plugin = VimPlugin.getDefault();

    if (!plugin.gvimAvailable()) {
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

    IPreferenceStore prefs = plugin.getPreferenceStore();
    tabbed = prefs.getBoolean(PreferenceConstants.P_TABBED);
    embedded = prefs.getBoolean(PreferenceConstants.P_EMBED);
    // disabling documentListen until there is a really good reason to have,
    // cause it is by far the buggest part of vim's netbeans interface.
    documentListen = false; //plugin.gvimNbDocumentListenSupported();
    if (embedded){
      if (!plugin.gvimEmbedSupported()){
        String message = plugin.getMessage(
            "gvim.not.supported",
            plugin.getMessage("gvim.embed.not.supported"));
        throw new RuntimeException(message);
      }
    }
    if (!plugin.gvimNbSupported()){
      String message = plugin.getMessage(
          "gvim.not.supported",
          plugin.getMessage("gvim.nb.not.enabled"));
      throw new RuntimeException(message);
    }

    //set some flags
    alreadyClosed = false;
    dirty = false;

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
      editorGUI = new Canvas(parent, SWT.EMBEDDED);

      //create a vim instance
      VimConnection vc = createVim(projectPath, filePath, parent);

      viewer = new VimViewer(
          bufferID, vc, editorGUI != null ? editorGUI : parent, SWT.EMBEDDED);
      viewer.getTextWidget().setVisible(false);
      viewer.setDocument(document);
      viewer.setEditable(isEditable());
      try{
        Field fSourceViewer =
          AbstractTextEditor.class.getDeclaredField("fSourceViewer");
        fSourceViewer.setAccessible(true);
        fSourceViewer.set(this, viewer);
      }catch(Exception e){
        logger.error("Unable to access source viewer field.", e);
      }

      // open eclimd view if necessary
      boolean startEclimd = plugin.getPreferenceStore()
        .getBoolean(PreferenceConstants.P_START_ECLIMD);
      if (startEclimd){
        IWorkbenchPage page = PlatformUI.getWorkbench()
          .getActiveWorkbenchWindow().getActivePage();
        try{
          if (page != null && page.findView(ECLIMD_VIEW_ID) == null){
            page.showView(ECLIMD_VIEW_ID);
          }
        }catch(PartInitException pie){
          logger.error("Unable to open eclimd view.", pie);
        }
      }

      // on initial open, our part listener isn't firing for some reason.
      if(embedded){
        plugin.getPartListener().partOpened(this);
        plugin.getPartListener().partBroughtToTop(this);
        plugin.getPartListener().partActivated(this);
      }
    }
  }

  /**
   * Create a vim instance figuring out if it should be external or embedded.
   *
   * @param workingDir
   * @param filePath
   * @param parent
   */
  private VimConnection createVim(
      String workingDir, String filePath, Composite parent)
  {
    VimPlugin plugin = VimPlugin.getDefault();

    //get bufferId
    bufferID = plugin.getNumberOfBuffers();
    plugin.setNumberOfBuffers(bufferID + 1);

    IStatus status = null;
    VimConnection vc = null;
    if (embedded) {
      try {
        vc = createEmbeddedVim(workingDir, filePath, editorGUI);
      } catch (Exception e) {
        embedded = false;
        vc = createExternalVim(workingDir, filePath, parent);

        String message = plugin.getMessage(
            e instanceof NoSuchFieldException ?
            "embed.unsupported" : "embed.fallback");
        status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, e);
      }
    } else {
      vc = createExternalVim(workingDir, filePath, parent);
      String message = plugin.getMessage("gvim.external.success");
      status = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, message);
    }

    if (status != null){
      editorGUI.dispose();
      editorGUI = null;
      new StatusPart(parent, status);
      // remove the "Show the Error Log View" button if the status is OK
      if (status.getSeverity() == IStatus.OK){
        for (Control c : parent.getChildren()){
          if (c instanceof Composite){
            for (Control ch : ((Composite)c).getChildren()){
              if (ch instanceof Button){
                ch.setVisible(false);
              }
            }
          }
        }
      }
    }

    plugin.getVimserver(serverID).getEditors().add(this);

    return vc;
  }

  /**
   * Create an external Vim instance.
   *
   * @param workingDir
   * @param filePath
   * @param parent
   */
  private VimConnection createExternalVim(
      String workingDir, String filePath, Composite parent)
  {
    VimPlugin plugin = VimPlugin.getDefault();
    boolean first = plugin.getVimserver(VimPlugin.DEFAULT_VIMSERVER_ID) == null;
    serverID = tabbed ? plugin.getDefaultVimServer() : plugin.createVimServer();
    plugin.getVimserver(serverID).start(workingDir, filePath, tabbed, first);

    VimConnection vc = plugin.getVimserver(serverID).getVc();
    vc.command(bufferID, "editFile", "\"" + filePath + "\"");

    if (documentListen){
      vc.command(bufferID, "startDocumentListen", "");
    }else{
      vc.command(bufferID, "stopDocumentListen", "");
    }
    return vc;
  }

  /**
   * Creates an embedded Vim instance (platform-dependent!). Gets the Window
   * ID/Handle of the SWT Widget given, uses reflection since the code is
   * platform specific and this allows us to distribute just one plugin for
   * all platforms.
   *
   * @param workingDir
   * @param filePath
   * @param parent
   * @throws Exception
   */
  private VimConnection createEmbeddedVim(
      String workingDir, String filePath, Composite parent)
    throws Exception
  {
    long wid = 0;

    Field f = null;
    try{
      f = Composite.class.getField(VimEditor.gtkWID);
    }catch(NoSuchFieldException nsfe){
      f = Control.class.getField(VimEditor.win32WID);
    }

    wid = f.getLong(parent);

    VimPlugin plugin = VimPlugin.getDefault();
    serverID = plugin.createVimServer();
    plugin.getVimserver(serverID).start(workingDir, wid);

    //int h = parent.getClientArea().height;
    //int w = parent.getClientArea().width;

    VimConnection vc = plugin.getVimserver(serverID).getVc();
    //vc.command(bufferID, "setLocAndSize", h + " " + w);
    vc.command(bufferID, "editFile", "\"" + filePath + "\"");
    if (documentListen){
      vc.command(bufferID, "startDocumentListen", "");
    }else{
      vc.command(bufferID, "stopDocumentListen", "");
    }
    return vc;
  }

  /**
   * This function will be called by vimserver when it gets an event
   * disconnect or killed It doesn't ask to save modifications since vim takes
   * care of that.
   */
  public void forceDispose() {
    final VimEditor editor = this;
    Display display = getSite().getShell().getDisplay();
    display.asyncExec(new Runnable() {
      public void run() {
        if (editor != null && !editor.alreadyClosed) {
          editor.setDirty(false);
          editor.showBusy(true);
          editor.close(false);
          getSite().getPage().closeEditor(editor, false);
          // editor.alreadyClosed = true;
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
    // closing the eclipse tab directly calls dispose, but not close.
    close(true);

    if (viewer != null) {
      viewer.getTextWidget().dispose();
      viewer = null;
    }

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
    if (server != null){
      server.getEditors().remove(this);

      if (save && dirty) {
        server.getVc().command(bufferID, "save", "");
        dirty = false;
        firePropertyChange(PROP_DIRTY);
      }

      if (server.getEditors().size() > 0) {
        server.getVc().command(bufferID, "close", "");
        String gvim = VimPlugin.getDefault().getPreferenceStore().getString(
            PreferenceConstants.P_GVIM);
        String[] args = new String[5];
        args[0] = gvim;
        args[1] = "--servername";
        args[2] = String.valueOf(server.getID());
        args[3] = "--remote-send";
        args[4] = "<esc>:redraw!<cr>";
        try{
          CommandExecutor.execute(args, 1000);
        }catch(Exception e){
          logger.error("Error redrawing vim after file close.", e);
        }
      } else {
        try {
          VimConnection vc = server.getVc();
          if (vc != null){
            server.getVc().function(bufferID, "saveAndExit", "");
          }
          plugin.stopVimServer(serverID);
        } catch (IOException e) {
          message(plugin.getMessage("server.stop.failed"), e);
        }
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

  private String getFilePath(IEditorInput input)
  {
    String filePath = null;
    if (input instanceof IFileEditorInput){
      selectedFile = ((IFileEditorInput)input).getFile();
      filePath = selectedFile.getRawLocation().toPortableString();
    }else{
      URI uri = ((IURIEditorInput)input).getURI();
      filePath = uri.toString().substring("file:".length());
      filePath = filePath.replaceFirst("^/([A-Za-z]:)", "$1");
    }
    return filePath;
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

    // let the parent composite handle setting the focus on the tab first.
    if (embedded){
      parent.setFocus();
    }

    VimPlugin plugin = VimPlugin.getDefault();
    VimConnection conn = plugin.getVimserver(serverID).getVc();

    // get the current offset which "setDot" requires.
    //String offset = "0";
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
    //conn.command(bufferID, "setDot", offset);
    // Brings the vim editor window to top
    conn.command(bufferID, "raise", "");

    // to fully focus gvim, we need to simulate a mouse click.
    // Should this be optional, via a preference? There is the potential for
    // weirdness here.
    if (embedded && parent.getDisplay().getActiveShell() != null){
      boolean autoClickFocus = plugin.getPreferenceStore()
        .getBoolean(PreferenceConstants.P_FOCUS_AUTO_CLICK);
      if (autoClickFocus){
        // hack: setFocus may be called more than once, so attempt to only
        // simulate a click only on the first one.
        long now = System.currentTimeMillis();
        long delta = (now - lastFocus);
        lastFocus = now;

        if (delta > 300) {
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
    }
  }

  /**
   * Sets the editor window title to path.. need to change path to file name..
   *
   * @param path
   */
  public void setTitleTo(final String path) {
    Display.getDefault().asyncExec(new Runnable(){
      public void run()
      {
        String filename = path.substring(path.lastIndexOf(File.separator) + 1);
        setPartName(filename);
      }
    });
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
      newText = newText +
        text.substring(offset, offset1) +
        (text.length() > offset1 + 1 ? text.substring(offset1 + 1, offset1 + 2) : "");
      offset = offset1 + 2;
    }
    return newText;
  }

  /**
   * Inserts text into document.
   *
   * @param text The text to insert.
   * @param offset The offset to insert it at.
   */
  public void insertDocumentText(String text, int offset) {
    text = removeBackSlashes(text);

    try {
      String contents = document.get();
      // FIXME: determine file encoding.
      offset = FileUtils.byteOffsetToCharOffset(
          new ByteArrayInputStream(contents.getBytes()), offset, null);
      String first = contents.substring(0, offset);
      String last = contents.substring(offset);
      if (text.equals(new String("\\n"))) {
        first = first + System.getProperty("line.separator") + last;
      } else {
        first = first + text + last;
      }
      document.set(first);
      setDirty(true);
    } catch(Exception e) {
      VimPlugin plugin = VimPlugin.getDefault();
      logger.error(plugin.getMessage("document.insert.failed"), e);
    }
  }

  /**
   * Removes text in the document
   *
   * @param offset The offset of the cursor in the text.
   * @param length The amount of text to remove.
   */
  public void removeDocumentText(int offset, int length) {
    try {
      String contents = document.get();
      // FIXME: determine file encoding.
      int loffset = FileUtils.byteOffsetToCharOffset(
          new ByteArrayInputStream(contents.getBytes()), offset + length, null);
      offset = FileUtils.byteOffsetToCharOffset(
          new ByteArrayInputStream(contents.getBytes()), offset, null);
      length = loffset - offset;
      String first = contents.substring(0, offset);
      String last = contents.substring(offset + length);
      first = first + last;
      document.set(first);
      setDirty(true);
    } catch(Exception e) {
      VimPlugin plugin = VimPlugin.getDefault();
      logger.error(plugin.getMessage("document.remove.failed"), e);
    }
  }

  @Override
  protected void createActions() {
    super.createActions();
  }

  @Override
  protected void doSetInput(IEditorInput input)
    throws CoreException
  {
    if(getEditorInput() != null){
      String oldFilePath = getFilePath(getEditorInput());
      String newFilePath = getFilePath(input);
      if (!oldFilePath.equals(newFilePath)){
        VimConnection vc = VimPlugin.getDefault()
          .getVimserver(serverID).getVc();

        if (input instanceof IFileEditorInput){
          IProject project = selectedFile.getProject();
          IPath path = project.getRawLocation();
          if(path == null){
            String name = project.getName();
            path = ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
            path = path.append(name);
          }
          String projectPath = path.toPortableString();

          if (newFilePath.toLowerCase().indexOf(projectPath.toLowerCase()) != -1){
            newFilePath = newFilePath.substring(projectPath.length() + 1);
          }
        }

        if (isDirty()){
          vc.remotesend("<esc>:saveas! " + newFilePath.replace(" ", "\\ ") + "<cr>");
        }else{
          vc.command(bufferID, "editFile", "\"" + newFilePath + "\"");
        }
      }
    }

    super.doSetInput(input);
  }

  @Override
  public void saveState(IMemento arg0) {
    // no-op for now.  prevents error on closing of eclipse while a VimEditor
    // instance is open.
  }

  /**
   * @return the file.
   */
  public IFile getSelectedFile() {
    return selectedFile;
  }

  /**
   * @return the gvim server id.
   */
  public int getServerID() {
    return serverID;
  }

  /**
   * @return the bufferID
   */
  public int getBufferID() {
    return bufferID;
  }

  /**
   * Determines if this editor is running an embedded gvim instance or not.
   *
   * @return True if the gvim instance is embedded, false otherwise.
   */
  public boolean isEmbedded() {
    return embedded;
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
}
