package org.eclim.plugin.kotlin.project;

import org.eclim.command.CommandLine;
import org.eclim.plugin.jdt.project.JavaProjectManager;
import org.eclipse.core.resources.IProject;
import org.jetbrains.kotlin.core.model.KotlinNature;

public final class KotlinProjectManager extends JavaProjectManager {

    @Override
    protected void create(final IProject project,
                          final String depends) throws Exception {
        super.create(project, depends);
        KotlinNature.addNature(project);
    }

}
