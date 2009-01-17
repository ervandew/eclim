/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

/**
 * Command to attempt to find the proper import for any undefined types.
 *
 * @author Eric Van Dewoestine
 */
public class ImportMissingCommand
  extends ImportCommand
{
  private static final Pattern GENERIC = Pattern.compile("^(.*)<.*>$");

  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);

    IJavaProject project = JavaUtils.getJavaProject(projectName);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IProblem[] problems = JavaUtils.getProblems(src);
    ArrayList<String> missing = new ArrayList<String>();
    for(IProblem problem : problems){
      if(problem instanceof DefaultProblem){
        DefaultProblem p = (DefaultProblem)problem;
        if (p.getCategoryID() == DefaultProblem.CAT_TYPE ||
            p.getCategoryID() == DefaultProblem.CAT_MEMBER)
        {
          String[] args = p.getArguments();
          if (args.length == 1){
            String cls = args[0];
            Matcher matcher = GENERIC.matcher(cls);
            if(matcher.matches()){
              cls = matcher.replaceFirst("$1");
            }
            if(!missing.contains(cls)){
              missing.add(cls);
            }
          }
        }
      }
    }

    ArrayList<ImportMissingResult> results =
      new ArrayList<ImportMissingResult>();
    for(String type : missing){
      results.add(new ImportMissingResult(type, findImport(project, type)));
    }

    return ImportMissingFilter.instance.filter(commandLine, results);
  }
}
