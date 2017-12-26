package org.eclim.plugin.kotlin.command.src;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclim.annotation.Command;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;
import org.eclim.plugin.core.command.AbstractCommand;
import org.eclim.plugin.core.util.ProjectUtils;
import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.util.file.FileOffsets;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;

@Command(
        name = "kotlin_src_update",
        options = "REQUIRED p project ARG,"
                + "REQUIRED f file ARG,"
                + "OPTIONAL v validate NOARG,"
                + "OPTIONAL b build NOARG"
      )
public final class SrcUpdateCommand extends AbstractCommand
{

  @Override
  public Object execute(final CommandLine commandLine) throws Exception
  {
    final String file = commandLine.getValue(Options.FILE_OPTION);
    final String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    final IProject project = ProjectUtils.getProject(projectName, true);
    final IFile ifile = ProjectUtils.getFile(project, file);

    // validate the src file.
    if(commandLine.hasOption(Options.VALIDATE_OPTION)){
      final ICompilationUnit src = JavaCore.createCompilationUnitFrom(ifile);
      final IProblem[] problems = JavaUtils.getProblems(src);
      final List<Error> errors = new ArrayList<Error>(problems.length);
      final String filename = src.getResource().getLocation().toOSString()
          .replace('\\', '/');
      final FileOffsets offsets = FileOffsets.compile(filename);

      for(IProblem problem : problems){
        if(problem.getID() == IProblem.Task){
          continue;
        }

        final int[] lineColumn = offsets
            .offsetToLineColumn(problem.getSourceStart());
        errors.add(new Error(problem.getMessage(), filename, lineColumn[0],
            lineColumn[1], problem.isWarning()));
      }

      if(commandLine.hasOption(Options.BUILD_OPTION)){
        project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
                      new NullProgressMonitor());
      }

      return errors;
    }

    return StringUtils.EMPTY;
  }

}
