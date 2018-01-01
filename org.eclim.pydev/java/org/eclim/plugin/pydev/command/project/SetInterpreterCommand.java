/**
 * Copyright (C) 2013 - 2017 Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.project;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IPythonNature;

import org.python.pydev.plugin.nature.PythonNature;

/**
 * Command to get the python interpreter for the specified project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "python_set_interpreter",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED i interpreter ARG," +
    "OPTIONAL v version ARG"
)
public class SetInterpreterCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String interpreterNameOrPath = commandLine.getValue("i");
    String version = commandLine.getValue(Options.VERSION_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    PythonNature nature = PythonNature.getPythonNature(project);
    if (nature == null){
      throw new RuntimeException(
          Services.getMessage("python.missing.nature"));
    }

    IInterpreterInfo interpreter = null;
    IInterpreterManager manager = nature.getRelatedInterpreterManager();
    IInterpreterInfo[] existing = manager.getInterpreterInfos();
    for (IInterpreterInfo info : existing){
      if (info.getName().equals(interpreterNameOrPath) ||
          info.getExecutableOrJar().equals(interpreterNameOrPath))
      {
        interpreter = info;
        break;
      }
    }

    File path = new File(interpreterNameOrPath);
    if (interpreter == null && path.exists() && path.isFile()){
      HashSet<String> skip = new HashSet<String>();
      for (IInterpreterInfo info : existing){
        skip.add(info.getExecutableOrJar());
      }
      interpreter = manager.createInterpreterInfo(
          interpreterNameOrPath, new NullProgressMonitor(), false);
      interpreter.setName(FileUtils.getBaseName(interpreterNameOrPath));
      IInterpreterInfo[] updated = new IInterpreterInfo[existing.length + 1];
      System.arraycopy(existing, 0, updated, 0, existing.length);
      updated[updated.length - 1] = interpreter;
      manager.setInfos(updated, skip, new NullProgressMonitor());
    }else if (interpreter == null){
      throw new RuntimeException(Services.getMessage(
            "python.interpreter.not.found", interpreterNameOrPath));
    }

    if (version == null){
      version = nature.getVersion(true);
    }

    // ensure the version is valid for the new interpreter
    ArrayList<String> grammars = new ArrayList<String>();
    String[] parts = StringUtils.split(interpreter.getVersion(), ".");
    double iversion = Double.parseDouble(parts[0] + '.' + parts[1]);
    for (String grammar : IPythonNature.Versions.ALL_PYTHON_VERSIONS){
      grammar = grammar.replace("python ", "");
      double gversion = Double.parseDouble(grammar);
      if (gversion <= iversion &&
          grammar.charAt(0) == interpreter.getVersion().charAt(0))
      {
        grammars.add(grammar);
      }
    }
    if (!grammars.contains(version)){
      Collections.sort(grammars);
      version = grammars.get(grammars.size() - 1);
    }

    nature.setVersion(version, interpreter.getName());

    return Services.getMessage("python.interpreter.set", projectName);
  }
}
