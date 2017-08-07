package org.eclim.plugin.kotlin.command.complete;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
// import org.jetbrains.kotlin.ui.editors.KotlinOpenDeclarationAction;
import org.jetbrains.kotlin.ui.editors.codeassist.KotlinCompletionProcessor;
import org.jetbrains.kotlin.ui.editors.codeassist.KotlinCompletionProposal;
import org.jetbrains.kotlin.ui.builder.AspectsUtils;
import org.jetbrains.kotlin.ui.editors.*;
import org.eclim.annotation.Command;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.logging.Logger;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.jetbrains.kotlin.ui.editors.codeassist.KotlinCompletionProcessor;
import org.eclipse.jface.text.IDocument;
    
import org.eclipse.core.resources.IFile;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

// import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
// import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
// import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;

import org.eclipse.ui.*;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

@Command(name    = "kotlin_complete",
         options = "REQUIRED p project  ARG,"
                 + "REQUIRED f file     ARG,"
                 + "REQUIRED o offset   ARG,"
                 + "REQUIRED e encoding ARG,"
                 + "REQUIRED l layout   ARG,"
                 + "OPTIONAL j javaDoc  NOARG")
public final class CodeCompleteCommand extends org.eclim.plugin.jdt.command.complete.CodeCompleteCommand {

    private static final Logger LOG = Logger.getLogger(CodeCompleteCommand.class);
    
    @Override
    protected Object getResponse(List<CodeCompleteResult> results) {
        return results; // bypass the super class jdt response
    }

    @Override
    protected List<CodeCompleteResult> getCompletionResults(CommandLine commandLine,
                                                            String project,
                                                            String file,
                                                            int offset) throws Exception {
        LOG.info("We're completing");
        IFile ifile = ProjectUtils.getFile(ProjectUtils.getProject(project, true), file);
        final List<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();

        // ensure opens with Kotlin editor
        if (!AspectsUtils.isKotlinFile(ifile)) {
            LOG.info("Not a kotlin file");
            return results;
        }
        
        LOG.info("ifile -> Proceed with completion..." + ifile);

        IWorkbenchPage page       = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart kotlinEditor  = IDE.openEditor(page, ifile, false);        
        KotlinFileEditor ktEditor = (KotlinFileEditor) kotlinEditor;

        KotlinCompletionProcessor completer = new KotlinCompletionProcessor(ktEditor, null, true);
        ICompletionProposal[] proposals = completer.computeCompletionProposals(ktEditor.getViewer(), offset);

        ((ITextEditor)ktEditor).close(false);
        
        if (proposals == null)
            return results;

        for (ICompletionProposal proposal : proposals) {
            if (proposal instanceof KotlinCompletionProposal) {
                KotlinCompletionProposal ktProposal = (KotlinCompletionProposal) proposal;

                String completion = ktProposal.getReplacementString();
                String shortDesc  = ktProposal.getPresentableString();
                String desc       = ktProposal.getPresentableString();

                results.add(new CodeCompleteResult(completion, shortDesc, desc));
            } else {
                final String moreInfo = proposal.getAdditionalProposalInfo();
                if (moreInfo != null) {
                    results.add(new CodeCompleteResult(moreInfo,
                                                       proposal.getDisplayString(),
                                                       proposal.getDisplayString()));
                } else {
                    results.add(new CodeCompleteResult(proposal.getDisplayString(),
                                                       proposal.getDisplayString(),
                                                       proposal.getDisplayString()));
                }
            }
            
        }

        LOG.info("DONE...." + java.util.Arrays.toString(proposals));

        return results;
    }
}
