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

import org.w3c.dom.Node;

import com.android.ide.eclipse.adt.internal.editors.AndroidContentAssist;

import com.android.ide.eclipse.adt.internal.editors.common.CommonXmlEditor;

import com.android.ide.eclipse.adt.internal.editors.layout.LayoutContentAssist;

import com.android.ide.eclipse.adt.internal.editors.layout.gle2.DomUtilities;

import com.android.utils.Pair;

@Command(
  name = "adt_xml_complete",
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
  public Object execute(CommandLine commandLine) throws Exception {
    logger.info("Execute xml completion! {}", commandLine);
    return super.execute(commandLine);
  }


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

    // AndroidSourceViewerConfig config = getViewerConfig(project, file);
    ISourceViewer viewer = getTextViewer(commandLine, projectName, file);
    // logger.info("config={}; viewer={}", config, viewer);

    CommonXmlEditor editor = new CommonXmlEditor();
    IEditorInput input =
      new FileEditorInput(ProjectUtils.getFile(project, file));
    editor.init(new EclimEditorSite(), input);

    // AndroidContentAssist ca = (AndroidContentAssist) config
    //     .getAndroidContentAssistProcessor(viewer, file);
    AndroidContentAssist ca = getContentAssist(project, file);
    logger.info("Using: {}", ca);
    
    // set this the hard way so it doesn't try to query the UI
    Field mEditor = AndroidContentAssist.class.getDeclaredField("mEditor");
    mEditor.setAccessible(true);
    mEditor.set(ca, editor);
    logger.info("Successfully set editor: {}; doc: {}", editor, viewer.getDocument());

    // IDocument doc = viewer.getDocument();
    IFile ifile = ProjectUtils.getFile(project, file);
    IModelManager man = StructuredModelManager.getModelManager();
    IStructuredModel any = man.getModelForRead(ifile);
    any.releaseFromRead();
    IStructuredDocument doc = any.getStructuredDocument();

    IStructuredModel existing = man.getExistingModelForRead(doc);
    logger.info("existing ={}; any={}", existing, any);


    Pair<Node, Node> context = DomUtilities.getNodeContext(doc, offset);
    logger.info("nodeContext ={}", context);
    viewer.setDocument(doc);

    ICompletionProposal[] props = ca.computeCompletionProposals(viewer, offset);
    logger.info("props ={}", (Object) props);

    return props;

    // ContentAssistant ca = (ContentAssistant)config.getContentAssistant(viewer);
    // logger.info("assist={}", ca);
    // Method computeCompletionProposals =
    //   ContentAssistant.class.getDeclaredMethod(
    //       "computeCompletionProposals", ITextViewer.class, Integer.TYPE);
    // logger.info("method={}", computeCompletionProposals);
    // computeCompletionProposals.setAccessible(true);

    // return (ICompletionProposal[])
    //   computeCompletionProposals.invoke(ca, viewer, offset);
  }

  AndroidContentAssist getContentAssist(IProject project,
      String file)
  {
    // TODO
    return new LayoutContentAssist();
  }

    // private AndroidSourceViewerConfig getViewerConfig(IProject project,
    //         String file) {
    //     // TODO
    //     return new AndroidSourceViewerConfig() {
    //
    //         @Override
    //         public IContentAssistProcessor getAndroidContentAssistProcessor(
    //                 ISourceViewer arg0, String arg1) {
    //             return new LayoutContentAssist();
    //         }
    //     };
    // }

  // @Override
  // protected AndroidContentAssist getContentAssistProcessor(
  //     CommandLine commandLine, String project, String file)
  //     throws Exception {
  //   // // TODO Auto-generated method stub
  //   // return super.getContentAssistProcessor(commandLine, project, file);
  //   return new AndroidContentAssist(AndroidTargetData.DESCRIPTOR_LAYOUT) {
  //
  //           @Override
  //           public ICompletionProposal[] computeCompletionProposals(
  //                   ITextViewer viewer, int offset) {
  //               ICompletionProposal[] props =
  //                   super.computeCompletionProposals(viewer, offset);
  //
  //               logger.info("Compute props on {} @ {}", viewer, offset);
  //               // logger.info("editor={}", (Object) this.mEditor);
  //               logger.info("prefix={}", extractElementPrefix(viewer, offset));
  //               // logger.info("node={}", getNode(viewer, offset));
  //
  //               return props;
  //           }
  //
  //       };
  //   }

}
