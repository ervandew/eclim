/**
 * Copyright (C) 2012 - 2017 Eric Van Dewoestine
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
package org.eclim.plugin.adt.project;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.project.ProjectManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.android.SdkConstants;

import com.android.ide.eclipse.adt.AdtConstants;
import com.android.ide.eclipse.adt.AdtPlugin;

import com.android.ide.eclipse.adt.internal.sdk.Sdk;

import com.android.ide.eclipse.adt.internal.wizards.newproject.NewProjectCreator;

import com.android.sdklib.IAndroidTarget;

/**
 * Implementation of {@link ProjectManager} for android projects.
 *
 * @author Eric Van Dewoestine
 */
public class AndroidProjectManager
  implements ProjectManager
{
  @Override
  @SuppressWarnings("static-access")
  public void create(IProject project, CommandLine commandLine)
  {
    String[] args = commandLine.getValues(Options.ARGS_OPTION);
    GnuParser parser = new GnuParser();
    org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
    options.addOption(
        OptionBuilder.hasArg().isRequired().withLongOpt("target").create());
    options.addOption(
        OptionBuilder.hasArg().isRequired().withLongOpt("package").create());
    options.addOption(
        OptionBuilder.hasArg().isRequired().withLongOpt("application").create());
    options.addOption(OptionBuilder.hasArg().withLongOpt("activity").create());
    options.addOption(OptionBuilder.withLongOpt("library").create());
    org.apache.commons.cli.CommandLine cli = null;
    try{
      cli = parser.parse(options, args);
    }catch(ParseException pe){
      throw new RuntimeException(pe);
    }

    Sdk sdk = Sdk.getCurrent();

    String targetHash = cli.getOptionValue("target");
    IAndroidTarget target = sdk.getTargetFromHashString(targetHash);

    Map<String,Object> parameters = new HashMap<String,Object>();
    parameters.put("SDK_TARGET", target);
    parameters.put("SRC_FOLDER", SdkConstants.FD_SOURCES);
    parameters.put("IS_NEW_PROJECT", true);
    parameters.put("SAMPLE_LOCATION", null);
    parameters.put("IS_LIBRARY", cli.hasOption("library"));
    parameters.put("ANDROID_SDK_TOOLS", AdtPlugin.getOsSdkToolsFolder());
    parameters.put("PACKAGE", cli.getOptionValue("package"));
    parameters.put("APPLICATION_NAME", "@string/app_name");
    parameters.put("MIN_SDK_VERSION", target.getVersion().getApiString());
    if (cli.hasOption("activity")){
      parameters.put("ACTIVITY_NAME", cli.getOptionValue("activity"));
    }

    Map<String,String> dictionary = new HashMap<String,String>();
    dictionary.put("app_name", cli.getOptionValue("application"));

    // gross: the android NewProjectCreator expects to be the one to create the
    // project, so we have to, ug, delete the project first.
    IProjectDescription description = null;
    try{
      description = project.getDescription();
      project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    // Would be nice to use public static create method, but it doesn't provide
    // the option for package name, activity name, app name, etc.
    //NewProjectCreator.create(
    //    new NullProgressMonitor(),
    //    project,
    //    target,
    //    null /* ProjectPopulator */,
    //    cli.hasOption("library") /* isLibrary */,
    //    null /* projectLocation */);

    NewProjectCreator creator = new NewProjectCreator(null, null);
    invoke(
        creator,
        "createEclipseProject",
        new Class[]{
          IProgressMonitor.class,
          IProject.class,
          IProjectDescription.class,
          Map.class,
          Map.class,
          NewProjectCreator.ProjectPopulator.class,
          Boolean.TYPE,
        },
        new NullProgressMonitor(),
        project,
        description,
        parameters,
        dictionary,
        null,
        true);

    try{
      project.getNature(AdtConstants.NATURE_DEFAULT).configure();
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  @SuppressWarnings("rawtypes")
  private Object invoke(Object obj, String name, Class[] params, Object... args)
  {
    try{
      Method method = obj.getClass().getDeclaredMethod(name, params);
      method.setAccessible(true);
      return method.invoke(obj, args);
    }catch(NoSuchMethodException nsme){
      throw new RuntimeException(nsme);
    }catch(IllegalAccessException iae){
      throw new RuntimeException(iae);
    }catch(InvocationTargetException ite){
      throw new RuntimeException(ite);
    }
  }

  @Override
  public List<Error> update(IProject project, CommandLine commandLine)
  {
    return null;
  }

  @Override
  public void delete(IProject project, CommandLine commandLine)
  {
  }

  @Override
  public void refresh(IProject project, CommandLine commandLine)
  {
  }

  @Override
  public void refresh(IProject project, IFile file)
  {
  }
}
