package eclim.plugin.groovy;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.core.project.ProjectNatureFactory;
import org.eclipse.jdt.core.JavaCore;

public final class PluginResources extends AbstractPluginResources
{

  public static final String GROOVY_BUNDLE_BASENAME = "org/eclim/plugin/groovy/messages";

  @Override
  public void initialize(String name)
  {
    super.initialize(name);
    ProjectNatureFactory.addNature("groovy", GroovyNature.GROOVY_NATURE);
    ProjectNatureFactory.addNature("java", JavaCore.NATURE_ID);
  }

  @Override
  protected String getBundleBaseName()
  {
    return GROOVY_BUNDLE_BASENAME;
  }

}
