/**
 * Copyright (C) 2012 - 2017
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
package org.eclim.plugin.pydev;

import java.util.ArrayList;
import java.util.List;

import org.eclim.Services;

import org.eclim.logging.Logger;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.core.preference.PreferenceFactory;
import org.eclim.plugin.core.preference.Preferences;
import org.eclim.plugin.core.preference.PreferencesOptionHandler;

import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.pydev.project.PydevProjectManager;

import org.python.pydev.core.ICodeCompletionASTManager;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IToken;

import org.python.pydev.editor.codecompletion.revisited.CompletionCache;
import org.python.pydev.editor.codecompletion.revisited.CompletionStateFactory;

import org.python.pydev.editor.preferences.PydevEditorPrefs;

import org.python.pydev.plugin.nature.PythonNature;

import com.python.pydev.analysis.AnalysisPreferenceInitializer;

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
  public static final String NAME = "org.eclim.pydev";

  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    // add the nature alias
    ProjectNatureFactory.addNature("python", PythonNature.PYTHON_NATURE_ID);
    ProjectManagement.addProjectManager(
        PythonNature.PYTHON_NATURE_ID, new PydevProjectManager());

    PreferenceFactory.addPreferences(PythonNature.PYTHON_NATURE_ID,
    //  "PYDEV org.eclim.python.builtins {} JSON{}\n" +
      "PYDEV org.eclim.python.ignore.unresolved.imports [] JSON[]\n" +
      "PYDEV org.eclim.python.ignore.builtin.reserved [] JSON[]"
    );

    // Note: pydev doesn't currently support project level preferences
    PreferencesOptionHandler handler = new PreferencesOptionHandler(
        PythonNature.PYTHON_NATURE_ID, false);
    handler.addSupportedPreferences("org.python.pydev", new String[]{
      PydevEditorPrefs.TAB_WIDTH,
    });

    new AnalysisPreferenceInitializer().initializeDefaultPreferences();
    handler.addSupportedPreferences("com.python.pydev.analysis", new String[]{
      AnalysisPreferenceInitializer.NAMES_TO_IGNORE_UNUSED_VARIABLE,
      AnalysisPreferenceInitializer.NAMES_TO_IGNORE_UNUSED_IMPORT,
      AnalysisPreferenceInitializer.NAMES_TO_CONSIDER_GLOBALS,
      AnalysisPreferenceInitializer.SEVERITY_UNUSED_IMPORT,
      AnalysisPreferenceInitializer.SEVERITY_UNUSED_WILD_IMPORT,
      AnalysisPreferenceInitializer.SEVERITY_UNUSED_VARIABLE,
      AnalysisPreferenceInitializer.SEVERITY_UNUSED_PARAMETER,
    });
    Preferences.addOptionHandler(handler);
    PreferenceFactory.addOptions(PythonNature.PYTHON_NATURE_ID,
      "PYDEV org.python.pydev.TAB_WIDTH \\d+\n" +
      "PYDEV com.python.pydev.analysis.NAMES_TO_CONSIDER_GLOBALS\n" +
      "PYDEV com.python.pydev.analysis.NAMES_TO_IGNORE_UNUSED_VARIABLE\n" +
      "PYDEV com.python.pydev.analysis.NAMES_TO_IGNORE_UNUSED_IMPORT\n" +
      "PYDEV com.python.pydev.analysis.SEVERITY_UNUSED_IMPORT (-1|[0-2])\n" +
      "PYDEV com.python.pydev.analysis.SEVERITY_UNUSED_WILD_IMPORT (-1|[0-2])\n" +
      "PYDEV com.python.pydev.analysis.SEVERITY_UNUSED_VARIABLE (-1|[0-2])\n" +
      "PYDEV com.python.pydev.analysis.SEVERITY_UNUSED_PARAMETER (-1|[0-2])"
    );

    // pre-init the builtin completions for the python natures so that users
    // don't have to wait on this on the first save
    List<IPythonNature> natures = PythonNature.getAllPythonNatures();
    logger.debug("pre-loading python builtin completions...");
    for (IPythonNature nature : natures){
      ICompletionState completionState = CompletionStateFactory
        .getEmptyCompletionState(nature, new CompletionCache());
      ICodeCompletionASTManager manager = nature.getAstManager();
      if (manager != null){
        // this is the part that takes some time to complete
        manager.getBuiltinCompletions(completionState, new ArrayList<IToken>());
      }
    }
    logger.debug("finished loading python builtin completions.");
  }

  @Override
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/pydev/messages";
  }
}
