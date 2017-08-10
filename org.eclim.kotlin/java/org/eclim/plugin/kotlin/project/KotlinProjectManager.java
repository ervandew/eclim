package org.eclim.plugin.kotlin.project;

import org.eclim.command.CommandLine;
import org.eclim.plugin.jdt.project.JavaProjectManager;
import org.eclipse.core.resources.IProject;
import org.jetbrains.kotlin.core.model.KotlinNature;

public class KotlinProjectManager extends JavaProjectManager {

    @Override
    protected void create(IProject project, String depends) throws Exception {
        super.create(project, depends);
        KotlinNature.addNature(project);
    }

}
