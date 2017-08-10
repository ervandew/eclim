package org.eclim.plugin.kotlin.command.complete;

import java.util.ArrayList;
import java.util.Collections;
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
    protected Object getResponse(final List<CodeCompleteResult> results) {
        return results;
    }

    @Override
    protected List<CodeCompleteResult> getCompletionResults(final CommandLine commandLine,
                                                            final String project,
                                                            final String file,
                                                            final int offset) throws Exception {
        final IFile ifile = ProjectUtils.getFile(ProjectUtils.getProject(project, true), file);

        if (!AspectsUtils.isKotlinFile(ifile)) return Collections.emptyList();

        final IWorkbenchPage page       = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorPart editorPart    = IDE.openEditor(page, ifile, false);
        final KotlinFileEditor ktEditor = (KotlinFileEditor) editorPart;
        final ISourceViewer viewer      = ktEditor.getViewer();

        final KotlinCompletionProcessor completer = new KotlinCompletionProcessor(ktEditor, null, true);
        final ICompletionProposal[] proposals     = completer.computeCompletionProposals(viewer, offset);

        ( (ITextEditor) ktEditor).close(false);

        if (proposals == null) return Collections.emptyList();

        final List<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>(proposals.length);

        for (ICompletionProposal proposal : proposals) {
            final String moreInfo   = proposal.getAdditionalProposalInfo();
            final String shortDesc  = proposal.getDisplayString();
            final String desc       = proposal.getDisplayString();
            final String completion = proposal.getDisplayString();
            final String completionText = (moreInfo != null) ? completion : moreInfo;

            results.add(new CodeCompleteResult(completionText, shortDesc, desc));
        }

        return results;
    }

}
