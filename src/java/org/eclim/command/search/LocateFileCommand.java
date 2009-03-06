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
package org.eclim.command.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wcohen.ss.Levenstein;

import com.wcohen.ss.api.StringDistance;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Given a file pattern, finds all files that match that pattern.
 *
 * @author Eric Van Dewoestine
 */
public class LocateFileCommand
  extends AbstractCommand
{
  public static final String SCOPE_PROJECT = "project";
  public static final String SCOPE_WORKSPACE = "workspace";

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String pattern = commandLine.getValue(Options.PATTERN_OPTION);
    String projectName = commandLine.getValue(Options.NAME_OPTION);
    String scope = commandLine.getValue(Options.SCOPE_OPTION);

    ArrayList<IProject> projects = new ArrayList<IProject>();

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

    FileMatcher matcher = new FileMatcher(pattern, projectName);
    for (IProject resource : projects){
      resource.accept(matcher, 0);
    }

    return StringUtils.join(matcher.getResults(), '\n');
  }

  private static class Result
  {
    public String name;
    public String projectPath;
    public String realPath;
    public String projectName;

    /**
     * Constructs a new instance.
     *
     * @param name The name for this instance.
     * @param projectPath The projectPath for this instance.
     * @param realPath The realPath for this instance.
     * @param projectName The projectName for this instance.
     */
    public Result(
        String name,
        String projectPath,
        String realPath,
        String projectName)
    {
      this.name = name;
      this.projectPath = projectPath;
      this.realPath = realPath;
      this.projectName = projectName;
    }

    public String toString()
    {
      return new StringBuffer()
        .append(name).append('|')
        .append(projectPath).append('|')
        .append(realPath)
        .toString();
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

    private String pattern;
    private String projectName;
    private Matcher matcher;
    private boolean includesPath;
    private Matcher baseMatcher;
    private ArrayList<Result> results = new ArrayList<Result>();
    private ArrayList<String> seen = new ArrayList<String>();

    /**
     * Constructs a new instance.
     *
     * @param pattern The pattern for this instance.
     * @param projectName The possibly null current project name.
     */
    public FileMatcher (String pattern, String projectName)
    {
      this.pattern = pattern;
      this.projectName = projectName;
      this.matcher = Pattern.compile(pattern).matcher("");

      Matcher baseMatcher = FIND_BASE.matcher(pattern);
      if (baseMatcher.find()){
        String base = baseMatcher.group(1);
        this.baseMatcher = Pattern.compile(base).matcher("");
        this.includesPath = true;
      }
    }

    /**
     * {@inheritDoc}
     * @see IResourceProxyVisitor#visit(IResourceProxy)
     */
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
        IPath raw = resource.getRawLocation();
        if (raw != null){
          String rel = resource.getFullPath().toOSString().replace('\\', '/');
          String path = raw.toOSString().replace('\\', '/');
          Result entry = new Result(
              FileUtils.getBaseName(rel),
              rel, path, resource.getProject().getName());
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
      FilePathComparator comparator =
        new FilePathComparator(pattern, projectName);
      Collections.sort(results, comparator);
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

    /**
     * {@inheritDoc}
     * @see Comparator#compare(T,T)
     */
    public int compare(Result o1, Result o2)
    {
      double score1 = score(o1);
      double score2 = score(o2);

      return (int)Math.round(score1 - score2);
    }

    public double score(Result result)
    {
      String path = result.projectPath;
      if (this.scores.containsKey(path)){
        return this.scores.get(path);
      }
      double score = 0 - this.distance.score(this.pattern, path);

      // weight files in the current project more favorably
      if (projectName != null){
        if (result.projectName.equals(projectName)){
          score -= score * .1;
        }
      }

      this.scores.put(path, score);
      return score;
    }

    /**
     * {@inheritDoc}
     * @see Comparator#equals(Object)
     */
    public boolean equals(Object obj)
    {
      return super.equals(obj);
    }
  }
}
