/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.search;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.wcohen.ss.Levenstein;

import com.wcohen.ss.api.StringDistance;

/**
 * Given a file pattern, finds all files that match that pattern.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "locate_file",
  options =
    "REQUIRED p pattern ARG," +
    "REQUIRED s scope ARG," +
    "OPTIONAL n project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL i case_insensitive NOARG"
)
public class LocateFileCommand
  extends AbstractCommand
{
  public static final String SCOPE_PROJECT = "project";
  public static final String SCOPE_WORKSPACE = "workspace";
  public static final String SCOPE_LIST = "list";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String pattern = commandLine.getValue(Options.PATTERN_OPTION);
    String scope = commandLine.getValue(Options.SCOPE_OPTION);
    String projectName = commandLine.getValue(Options.NAME_OPTION);

    List<Result> results = null;
    if (SCOPE_LIST.equals(scope)){
      results = executeLocateFromFileList(commandLine, pattern, scope);
    }else{
      results = executeLocateFromEclipse(commandLine, pattern, scope);
    }

    FilePathComparator comparator =
      new FilePathComparator(pattern, projectName);
    Collections.sort(results, comparator);
    return results;
  }

  @Override
  public void cleanup(CommandLine commandLine)
  {
    // no-op
  }

  private List<Result> executeLocateFromFileList(
      CommandLine commandLine, String pattern, String scope)
    throws Exception
  {
    ArrayList<Result> results = new ArrayList<Result>();
    String fileName = commandLine.getValue(Options.FILE_OPTION);
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new FileReader(fileName));
      int flags = 0;
      if (commandLine.hasOption(Options.CASE_INSENSITIVE_OPTION)){
        flags = Pattern.CASE_INSENSITIVE;
      }
      Matcher matcher = Pattern.compile(pattern, flags).matcher("");
      String line = null;
      while ((line = reader.readLine()) != null){
        if (matcher.reset(line).find()){
          results.add(new Result(FileUtils.getBaseName(line), line, null, null));
        }
      }
    }finally{
      IOUtils.closeQuietly(reader);
    }
    return results;
  }

  private List<Result> executeLocateFromEclipse(
      CommandLine commandLine, String pattern, String scope)
    throws Exception
  {
    ArrayList<IProject> projects = new ArrayList<IProject>();

    String projectName = commandLine.getValue(Options.NAME_OPTION);
    if (projectName != null){
      IProject project = ProjectUtils.getProject(projectName, true);
      projects.add(project);
      IProject[] depends = project.getReferencedProjects();
      for (IProject p : depends){
        if(!p.isOpen()){
          p.open(null);
        }
        projects.add(p);
      }
    }

    if (SCOPE_WORKSPACE.equals(scope)){
      IProject[] all = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject p : all){
        if(p.isOpen() && !projects.contains(p)){
          projects.add(p);
        }
      }
    }

    FileMatcher matcher = new FileMatcher(
        pattern, commandLine.hasOption(Options.CASE_INSENSITIVE_OPTION));
    for (IProject resource : projects){
      resource.accept(matcher, 0);
    }

    return matcher.getResults();
  }

  public static class Result
  {
    public String name;
    public String path;
    public String project;
    public String projectPath;

    /**
     * Constructs a new instance.
     *
     * @param name The name of the file.
     * @param path The absolute path of the file.
     * @param project The name of the project the file is in.
     * @param projectPath The path of the file in the project.
     */
    public Result(
        String name,
        String path,
        String project,
        String projectPath)
    {
      this.name = name;
      this.path = path;
      this.project = project;
      this.projectPath = projectPath;
    }
  }

  private static class FileMatcher
    implements IResourceProxyVisitor
  {
    private static final Pattern FIND_BASE = Pattern.compile("^.*/([^\\]].*)");

    private static final ArrayList<String> IGNORE_DIRS =
      new ArrayList<String>();
    static {
      IGNORE_DIRS.add("CVS");
      IGNORE_DIRS.add(".bzr");
      IGNORE_DIRS.add(".git");
      IGNORE_DIRS.add(".hg");
      IGNORE_DIRS.add(".svn");
    }

    private static final ArrayList<String> IGNORE_EXTS =
      new ArrayList<String>();
    static {
      IGNORE_EXTS.add("class");
      IGNORE_EXTS.add("gif");
      IGNORE_EXTS.add("jpeg");
      IGNORE_EXTS.add("jpg");
      IGNORE_EXTS.add("png");
      IGNORE_EXTS.add("pyc");
      IGNORE_EXTS.add("swp");
    }

    private Matcher matcher;
    private boolean includesPath;
    private Matcher baseMatcher;
    private ArrayList<Result> results = new ArrayList<Result>();
    private ArrayList<String> seen = new ArrayList<String>();

    /**
     * Constructs a new instance.
     *
     * @param pattern The pattern for this instance.
     */
    public FileMatcher (String pattern, boolean ignoreCase)
    {
      int flags = 0;
      if (ignoreCase){
        flags = Pattern.CASE_INSENSITIVE;
      }
      this.matcher = Pattern.compile(pattern, flags).matcher("");

      Matcher baseMatcher = FIND_BASE.matcher(pattern);
      if (baseMatcher.find()){
        String base = baseMatcher.group(1);
        this.baseMatcher = Pattern.compile(base).matcher("");
        this.includesPath = true;
      }
    }

    @Override
    public boolean visit(IResourceProxy proxy)
      throws CoreException
    {
      if (results.size() >= 100){
        return false;
      }

      int type = proxy.getType();
      String name = proxy.getName();

      if (type == IResource.PROJECT){
        return true;
      }else if (type == IResource.FOLDER && IGNORE_DIRS.contains(name)){
        return false;
      }else if (type == IResource.FOLDER){
        return true;
      }else if (type == IResource.FILE){
        String ext = FileUtils.getExtension(name).toLowerCase();
        if (IGNORE_EXTS.contains(ext)){
          return false;
        }
      }

      IResource resource = null;
      if (includesPath){
        if (!baseMatcher.reset(name).matches()){
          return true;
        }
        resource = proxy.requestResource();
        name = resource.getFullPath().toOSString().replace('\\', '/');
      }

      if (matcher.reset(name).matches()){
        if (resource == null){
          resource = proxy.requestResource();
        }
        IPath raw = resource.getLocation();
        if (raw != null){
          String rel = resource.getFullPath().toOSString().replace('\\', '/');
          String path = raw.toOSString().replace('\\', '/');
          Result entry = new Result(
              FileUtils.getBaseName(rel),
              path, resource.getProject().getName(), rel);
          if (!seen.contains(path)){
            results.add(entry);
            seen.add(path);
          }
        }
      }

      return true;
    }

    public List<Result> getResults()
    {
      return results;
    }
  }

  private static class FilePathComparator
    implements Comparator<Result>
  {
    private StringDistance distance;
    private String pattern;
    private String projectName;
    private Map<String, Double> scores = new HashMap<String, Double>();

    /**
     * Constructs a new instance.
     *
     * @param pattern The pattern for this instance.
     * @param projectName The possibly null current project name.
     */
    public FilePathComparator(String pattern, String projectName)
    {
      this.distance = new Levenstein();
      this.pattern = pattern;
      this.pattern = this.pattern.replaceAll("\\.\\*\\??", "");
      this.projectName = projectName;
    }

    @Override
    public int compare(Result o1, Result o2)
    {
      double score1 = score(o1);
      double score2 = score(o2);

      return (int)Math.round(score1 - score2);
    }

    public double score(Result result)
    {
      String path = result.projectPath != null ? result.projectPath : result.path;
      if (this.scores.containsKey(path)){
        return this.scores.get(path);
      }
      double score = 0 - this.distance.score(this.pattern, path);

      // weight files in the current project more favorably
      if (projectName != null && result.project != null){
        if (result.project.equals(projectName)){
          score -= score * .1;
        }
      }

      this.scores.put(path, score);
      return score;
    }

    @Override
    public boolean equals(Object obj)
    {
      return super.equals(obj);
    }
  }
}
