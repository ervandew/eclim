package org.eclim.plugin.kotlin;

import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectNatureFactory;
import org.eclipse.jdt.core.JavaCore;
import org.jetbrains.kotlin.core.model.KotlinNature;
import org.eclim.plugin.kotlin.project.KotlinProjectManager;

public final class PluginResources extends AbstractPluginResources
{
  public static final String BUNDLE_BASENAME = "org/eclim/plugin/kotlin/messages";
  public static final String NATURE = KotlinNature.KOTLIN_NATURE;

  @Override
  public void initialize(final String name)
  {
    super.initialize(name);

    ProjectNatureFactory.addNature("kotlin", new String[] { 
      NATURE,
      JavaCore.NATURE_ID
    });
    ProjectManagement.addProjectManager(NATURE, new KotlinProjectManager());
  }

  @Override
  protected String getBundleBaseName()
  {
    return BUNDLE_BASENAME;
  }

}
