package org.eclim.plugin.adt.command.complete;

import java.lang.reflect.Field;

import java.util.HashMap;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.eclipse.ui.EclimEditorSite;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

import com.android.ide.eclipse.adt.internal.editors.AndroidContentAssist;

import com.android.ide.eclipse.adt.internal.editors.common.CommonXmlEditor;

import com.android.ide.eclipse.adt.internal.editors.layout.LayoutContentAssist;

@Command(
  name = "android_xml_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG," +
    "REQUIRED l layout ARG"
)
public class AdtXmlCodeCompleteCommand extends AbstractCodeCompleteCommand {
    private static final Logger logger =
        Logger.getLogger(AdtXmlCodeCompleteCommand.class);


  @Override
  protected Object getResponse(List<CodeCompleteResult> results)
  {
    logger.info("Code completion! {}", results.size());
    // TODO clean this up
    HashMap<String, Object> response = new HashMap<String, Object>();
    response.put("completions", results);
    return response;
  }

  @Override
  protected ICompletionProposal[] getCompletionProposals(
      CommandLine commandLine, String projectName, String file, int offset)
    throws Exception
  {
    IProject project = ProjectUtils.getProject(projectName);
    IFile ifile = ProjectUtils.getFile(project, file);

    ISourceViewer viewer = getTextViewer(commandLine, projectName, file);
    CommonXmlEditor editor = new CommonXmlEditor();
    IEditorInput input = new FileEditorInput(ifile);
    editor.init(new EclimEditorSite(), input);

    // let the right one in
    AndroidContentAssist ca = getContentAssist(project, file);
    
    // set this the hard way so it doesn't try to query the UI
    Field mEditor = AndroidContentAssist.class.getDeclaredField("mEditor");
    mEditor.setAccessible(true);
    mEditor.set(ca, editor);

    // pre-init the Model; ADT only looks for an existing one
    IModelManager man = StructuredModelManager.getModelManager();
    IStructuredModel model = man.getModelForRead(ifile);
    IStructuredDocument doc = model.getStructuredDocument();
    viewer.setDocument(doc);

    // make sure it worked
    IStructuredModel existing = man.getExistingModelForRead(doc);
    if (existing == null) {
      logger.warn("Couldn't create existing model for doc {}", doc);
      return new ICompletionProposal[0];
    }
    logger.info("existing ={}; model={}", existing, model);


    ICompletionProposal[] props = ca.computeCompletionProposals(viewer, offset);
    logger.info("props ={}", (Object) props);

    // release after, to ensure it exists
    model.releaseFromRead();

    return props;
  }

  /** Pick the right ContentAssist based on resource type */
  AndroidContentAssist getContentAssist(IProject project,
      String file)
  {
    // TODO
    return new LayoutContentAssist();
  }

}
