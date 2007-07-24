package org.eclim.installer.step;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.formic.form.console.ConsoleForm;

import org.formic.form.gui.GuiForm;

import org.formic.wizard.step.FeatureListStep.Feature;

import org.formic.wizard.step.FeatureListStep;

/**
 * Provider to supply avaiable features to FeatureListStep.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class FeatureProvider
  implements FeatureListStep.FeatureProvider, PropertyChangeListener
{
  private static final String[] FEATURES =
    {"ant", "maven", "jdt", "wst", "pdt", "python"};
    //{"ant", "maven", "jdt", "wst", "pydev"};

  private static final boolean[] FEATURES_ENABLED =
    {true, true, true, false, false, false};

  private static final String[][] FEATURES_DEPENDS =
    {null, null, null, null, {"wst"}, null};

  private GuiForm guiForm;
  private ConsoleForm consoleForm;

  /**
   * {@inheritDoc}
   * @see FeatureListStep.FeatureProvider#getFeatures()
   */
  public Feature[] getFeatures ()
  {
    Feature[] features = new Feature[FEATURES.length];
    for (int ii = 0; ii < features.length; ii++){
      features[ii] = new Feature(
          FEATURES[ii], FEATURES_ENABLED[ii], FEATURES_DEPENDS[ii]);
    }

    return features;
  }

  /**
   * {@inheritDoc}
   * @see FeatureListStep.FeatureProvider#setGuiForm(GuiForm)
   */
  public void setGuiForm (GuiForm form)
  {
    this.guiForm = form;
  }

  /**
   * {@inheritDoc}
   * @see FeatureListStep.FeatureProvider#setConsoleForm(ConsoleForm)
   */
  public void setConsoleForm (ConsoleForm form)
  {
    this.consoleForm = form;
  }

  /**
   * {@inheritDoc}
   * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange (PropertyChangeEvent evt)
  {
    // do nothing for now.
  }
}
