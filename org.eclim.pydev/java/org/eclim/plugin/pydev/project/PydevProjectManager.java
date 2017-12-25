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
package org.eclim.plugin.pydev.project;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.project.ProjectManager;

import org.eclim.util.CollectionUtils;
import org.eclim.util.IOUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.python.pydev.core.IGrammarVersionProvider;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.core.PythonNatureWithoutProjectException;

import org.python.pydev.navigator.elements.ProjectConfigError;

import org.python.pydev.plugin.PydevPlugin;

import org.python.pydev.plugin.nature.PythonNature;

import org.python.pydev.shared_core.structure.Tuple;

/**
 * Implementation of {@link ProjectManager} for pydev projects.
 *
 * @author Eric Van Dewoestine
 */
public class PydevProjectManager
  implements ProjectManager
{
  private static final String PYDEVPROJECT = ".pydevproject";
  private static final Pattern NOT_FOUND =
    Pattern.compile(".*?: (.*) not found", Pattern.MULTILINE);
  private static final Pattern INVALID =
    Pattern.compile("Invalid .*?: (.*)", Pattern.MULTILINE);

  @SuppressWarnings("static-access")
  @Override
  public void create(IProject project, CommandLine commandLine)
  {
    String[] args = commandLine.getValues(Options.ARGS_OPTION);
    GnuParser parser = new GnuParser();
    org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
    options.addOption(
        OptionBuilder.hasArg().isRequired().withLongOpt("interpreter").create());
    org.apache.commons.cli.CommandLine cli = null;
    try{
      cli = parser.parse(options, args);
    }catch(ParseException pe){
      throw new RuntimeException(pe);
    }

    // remove the python nature added by ProjectManagement since pydev will
    // skip all the other setup if the nature is already present.
    try{
      IProjectDescription desc = project.getDescription();
      String[] natureIds = desc.getNatureIds();
      ArrayList<String> modified = new ArrayList<String>();
      CollectionUtils.addAll(modified, natureIds);
      modified.remove(PythonNature.PYTHON_NATURE_ID);
      desc.setNatureIds(modified.toArray(new String[modified.size()]));
      project.setDescription(desc, new NullProgressMonitor());

      String pythonPath = project.getFullPath().toString();
      String interpreter = cli.getOptionValue("interpreter");
      IInterpreterManager manager = PydevPlugin.getPythonInterpreterManager();
      IInterpreterInfo info = manager.getInterpreterInfo(interpreter, null);
      if (info == null){
        throw new RuntimeException("Python interpreter not found: " + interpreter);
      }

      // construct version from the interpreter chosen.
      String version = "python " +
        IGrammarVersionProvider.grammarVersionToRep.get(info.getGrammarVersion());

      // see src.org.python.pydev.plugin.PyStructureConfigHelpers
      PythonNature.addNature(
          project, null, version, pythonPath, null, interpreter, null);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }catch(MisconfigurationException me){
      throw new RuntimeException(me);
    }
  }

  @Override
  public List<Error> update(IProject project, CommandLine commandLine)
  {
    PythonNature nature = PythonNature.getPythonNature(project);
    // force a reload of .pydevproject
    nature.setProject(project);

    // call refresh to ensure the project interpreter is validated properly
    refresh(project, commandLine);

    String dotPydevProject = project.getFile(PYDEVPROJECT)
      .getRawLocation().toOSString();
    FileOffsets offsets = FileOffsets.compile(dotPydevProject);
    String contents = null;
    try{
      contents = IOUtils.toString(new FileInputStream(dotPydevProject));
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }

    Tuple<List<ProjectConfigError>, IInterpreterInfo> configErrorsAndInfo = null;
    try{
      configErrorsAndInfo = nature.getConfigErrorsAndInfo(project);
    }catch(PythonNatureWithoutProjectException pnwpe){
      throw new RuntimeException(pnwpe);
    }
    ArrayList<Error> errors = new ArrayList<Error>();
    for (ProjectConfigError e : configErrorsAndInfo.o1){
      String message = e.getLabel();
      int line = 1;
      int col = 1;
      // attempt to locate the line the error occurs on.
      for (Pattern pattern : new Pattern[]{NOT_FOUND, INVALID}){
        Matcher matcher = pattern.matcher(message);
        // extract the value that is triggering the error (path, interpreter
        // name, etc.).
        String value = null;
        if (matcher.find()){
          value = matcher.group(1).trim();
          matcher = Pattern
            .compile(">\\s*(\\Q" + value + "\\E)\\b", Pattern.MULTILINE)
            .matcher(contents);
          if(matcher.find()){
            int[] position = offsets.offsetToLineColumn(matcher.start(1));
            line = position[0];
            col = position[1];
          }
          break;
        }
      }

      errors.add(new Error(message, dotPydevProject, line, col, false));
    }

    return errors;
  }

  @Override
  public void delete(IProject project, CommandLine commandLine)
  {
  }

  @Override
  public void refresh(IProject project, CommandLine commandLine)
  {
    PythonNature pythonNature = PythonNature.getPythonNature(project);
    pythonNature.rebuildPath();
  }

  @Override
  public void refresh(IProject project, IFile file)
  {
  }
}
