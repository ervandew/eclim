/**
 * Copyright (C) 2012 Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.plugin.adt;

import java.io.File;

import org.eclim.Services;

import org.eclim.logging.Logger;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.adt.preference.OptionHandler;

import org.eclim.plugin.adt.project.AndroidProjectManager;

import org.eclim.plugin.core.preference.Option;
import org.eclim.plugin.core.preference.Preferences;
import org.eclim.plugin.core.preference.Validator;

import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectNatureFactory;

import com.android.ide.eclipse.adt.AdtConstants;
import com.android.ide.eclipse.adt.AdtPlugin;

import com.android.ide.eclipse.adt.internal.preferences.AdtPrefs;

import com.android.ide.eclipse.adt.internal.sdk.Sdk;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine
 */
public class PluginResources
  extends AbstractPluginResources
{
  private static final Logger logger = Logger.getLogger(PluginResources.class);

  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.adt";

  public static final String NATURE = AdtConstants.NATURE_DEFAULT;

  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    // force loading of the Sdk
    Sdk.getCurrent();

    Preferences.addOptionHandler("com.android.ide.eclipse.adt", new OptionHandler());
    ProjectNatureFactory.addNature("android", NATURE);
    ProjectManagement.addProjectManager(NATURE, new AndroidProjectManager());

    Preferences preferences = Preferences.getInstance();
    Option option = new Option();
    option.setNature(NATURE);
    option.setPath("Android");
    option.setName(AdtPrefs.PREFS_SDK_DIR);
    option.setValidator(new SdkValidator());
    preferences.addOption(option);

    String sdkLocation = AdtPrefs.getPrefs().getOsSdkFolder();
    if (sdkLocation == null || sdkLocation.length() == 0){
      logger.warn("Android SDK Location not set.");
    }else if (!new File(sdkLocation).exists()){
      logger.warn("Android SDK Location not found: " + sdkLocation);
    }
  }

  @Override
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/adt/messages";
  }

  private class SdkValidator
    extends AdtPlugin.CheckSdkErrorHandler
    implements Validator
  {
    private String message;

    @Override
    public boolean isValid(Object value)
    {
      message = null;
      return AdtPlugin.getDefault().checkSdkLocationAndId((String)value, this);
    }

    @Override
    public String getMessage(String name, Object value)
    {
      return message;
    }

    @Override
    public boolean handleError(
        AdtPlugin.CheckSdkErrorHandler.Solution solution, String message)
    {
      this.message = message;
      return false;
    }

    @Override
    public boolean handleWarning(
        AdtPlugin.CheckSdkErrorHandler.Solution solution, String message)
    {
      logger.warn(message);
      return true;
    }
  }
}
