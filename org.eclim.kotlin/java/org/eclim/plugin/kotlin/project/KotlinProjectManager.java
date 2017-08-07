package org.eclim.plugin.kotlin.project;

import org.eclim.command.CommandLine;
import org.eclim.plugin.kotlin.PluginResources;
import org.eclim.plugin.jdt.project.JavaProjectManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jetbrains.kotlin.core.model.KotlinNature;

public class KotlinProjectManager extends JavaProjectManager {

    @Override
    protected void create(IProject project, String depends) throws Exception {
        // TODO Auto-generated method stub
        super.create(project, depends);
        KotlinNature.addNature(project);
    }

}
