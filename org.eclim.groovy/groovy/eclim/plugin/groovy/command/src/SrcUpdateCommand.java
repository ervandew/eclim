package eclim.plugin.groovy.command.src;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclim.annotation.Command;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;
import org.eclim.plugin.core.command.AbstractCommand;
import org.eclim.plugin.core.util.ProjectUtils;
import org.eclim.util.file.FileOffsets;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;

@Command(name = "groovy_src_update", options = "REQUIRED p project ARG,REQUIRED f file ARG,OPTIONAL v validate NOARG,OPTIONAL b build NOARG")
public final class SrcUpdateCommand extends AbstractCommand
{

  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine) throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName, true);
    IFile ifile = ProjectUtils.getFile(project, file);

    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);

    // validate the src file.
    if(commandLine.hasOption(Options.VALIDATE_OPTION)){
      ICompilationUnit compilationUnit = JavaCore
          .createCompilationUnitFrom(ifile);
      List<IProblem> problems = getProblems(compilationUnit, null);
      List<Error> errors = new ArrayList<Error>();
      String filename = compilationUnit.getResource().getLocation()
          .toOSString().replace('\\', '/');
      FileOffsets offsets = FileOffsets.compile(filename);

      for(IProblem problem : problems){
        int[] lineColumn = offsets.offsetToLineColumn(problem.getSourceStart());
        errors.add(new Error(problem.getMessage(), filename, lineColumn[0],
            lineColumn[1], problem.isWarning()));
      }

      return errors;
    }

    return StringUtils.EMPTY;

  }

  /**
   * Gets the problems for a given src file.
   * 
   * @param src
   *          The src file.
   * @param ids
   *          Array of problem ids to accept.
   * @return The problems.
   */
  public static List<IProblem> getProblems(ICompilationUnit src, int[] ids)
      throws Exception
  {
    ICompilationUnit workingCopy = src.getWorkingCopy(null);
    ProblemRequestor requestor = new ProblemRequestor(ids);

    try{
      workingCopy.discardWorkingCopy();
      workingCopy.becomeWorkingCopy(null);
    }finally{
      workingCopy.discardWorkingCopy();
    }

    return requestor.getProblems();
  }

  /**
   * Gathers problems as a src file is processed.
   */
  public static final class ProblemRequestor implements
      org.eclipse.jdt.core.IProblemRequestor
  {
    private List<IProblem> problems = new ArrayList<IProblem>();
    private int[] ids;

    /**
     * Constructs a new instance.
     * 
     * @param ids
     *          Array of problem ids to accept.
     */
    public ProblemRequestor(int[] ids)
    {
      this.ids = ids;
    }

    /**
     * Gets a list of problems recorded.
     * 
     * @return The list of problems.
     */
    public List<IProblem> getProblems()
    {
      return problems;
    }

    /**
     * {@inheritDoc}
     */
    public void acceptProblem(IProblem problem)
    {
      if(ids != null){
        for(int ii = 0; ii < ids.length; ii++){
          if(problem.getID() == ids[ii] && problem.getID() != IProblem.Task){
            problems.add(problem);
            break;
          }
        }
      }else{
        if(problem.getID() != IProblem.Task){
          problems.add(problem);
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    public void beginReporting()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void endReporting()
    {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActive()
    {
      return true;
    }
  }
}
