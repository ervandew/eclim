package org.eclim.plugin.kotlin.command.complete;

import java.util.ArrayList;
import java.util.List;

import org.eclim.annotation.Command;
import org.eclim.command.CommandLine;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;
import org.eclim.plugin.core.util.ProjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jetbrains.kotlin.ui.builder.AspectsUtils;
import org.jetbrains.kotlin.ui.editors.KotlinFileEditor;
import org.jetbrains.kotlin.ui.editors.codeassist.KotlinCompletionProcessor;

@Command(name    = "kotlin_complete",
         options = "REQUIRED p project  ARG,"
                 + "REQUIRED f file     ARG,"
                 + "REQUIRED o offset   ARG,"
                 + "REQUIRED e encoding ARG,"
                 + "REQUIRED l layout   ARG,"
                 + "OPTIONAL j javaDoc  NOARG")
public final class CodeCompleteCommand extends org.eclim.plugin.jdt.command.complete.CodeCompleteCommand {

    @Override
    protected Object getResponse(List<CodeCompleteResult> results) {
        return results;
    }

    @Override
    protected List<CodeCompleteResult> getCompletionResults(CommandLine commandLine,
                                                            String project,
                                                            String file,
                                                            int offset) throws Exception {
        final IFile ifile = ProjectUtils.getFile(ProjectUtils.getProject(project, true), file);
        final List<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();

        if (!AspectsUtils.isKotlinFile(ifile)) return results;

        final IWorkbenchPage page       = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorPart editorPart    = IDE.openEditor(page, ifile, false);
        final KotlinFileEditor ktEditor = (KotlinFileEditor) editorPart;
        final ISourceViewer viewer      = ktEditor.getViewer();

        final KotlinCompletionProcessor completer = new KotlinCompletionProcessor(ktEditor, null, true);
        final ICompletionProposal[] proposals     = completer.computeCompletionProposals(viewer, offset);

        ( (ITextEditor) ktEditor).close(false);

        if (proposals == null) return results;

        for (ICompletionProposal proposal : proposals) {
            String moreInfo   = proposal.getAdditionalProposalInfo();
            String completion = proposal.getDisplayString();
            String shortDesc  = proposal.getDisplayString();
            String desc       = proposal.getDisplayString();

            if (moreInfo != null) completion = moreInfo;

            results.add(new CodeCompleteResult(completion, shortDesc, desc));
        }

        return results;
    }
}
