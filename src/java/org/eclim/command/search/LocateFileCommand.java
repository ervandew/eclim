/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wcohen.ss.Levenstein;

import com.wcohen.ss.api.StringDistance;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.search.ui.text.FileTextSearchScope;

/**
 * Given a file pattern, finds all files that match that pattern.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class LocateFileCommand
  extends AbstractCommand
{
  public static final String SCOPE_ALL = "all";
  public static final String SCOPE_PROJECT = "project";

  private static final Pattern IGNORE =
    Pattern.compile("(/(CVS|\\.svn|\\.hg|\\.git)/)");

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String projectName = _commandLine.getValue(Options.NAME_OPTION);
    String pattern = _commandLine.getValue(Options.PATTERN_OPTION);
    String scopeName = _commandLine.getValue(Options.SCOPE_OPTION);

    FileTextSearchScope scope = null;

    if(SCOPE_PROJECT.equals(scopeName)){
      if (!_commandLine.hasOption(Options.NAME_OPTION)){
        throw new RuntimeException(
            Services.getMessage("required.options.missing", Options.NAME_OPTION));
      }
      IProject project = ProjectUtils.getProject(projectName);
      IProject[] depends = project.getReferencedProjects();
      IProject[] projects = new IProject[depends.length + 1];
      projects[0] = project;
      System.arraycopy(depends, 0, projects, 1, depends.length);

      scope = FileTextSearchScope.newSearchScope(
          projects, new String[]{pattern}, false);
    }else{
      scope = FileTextSearchScope.newWorkspaceScope(new String[]{pattern}, false);
    }

    IFile[] files = scope.evaluateFilesInScope(null);
    ArrayList<String> paths = new ArrayList<String>();
    for (IFile file : files){
      String rel = file.getFullPath().toOSString();
      String path = file.getRawLocation().toOSString();
      Matcher matcher = IGNORE.matcher(path);
      if (!matcher.find()){
        paths.add(FileUtils.getBaseName(rel) + '|' + rel + '|' + path);
        if (paths.size() > 99){
          break;
        }
      }
    }

    FilePathComparator comparator = new FilePathComparator(pattern);
    Collections.sort(paths, comparator);
    return StringUtils.join(paths, '\n');
  }

  private static class FilePathComparator
    implements Comparator<String>
  {
    private StringDistance distance;
    private String pattern;
    private Map<String,Double> scores = new HashMap<String,Double>();

    /**
     * Constructs a new instance.
     *
     * @param pattern The pattern for this instance.
     */
    public FilePathComparator (String pattern)
    {
      this.distance = new Levenstein();
      this.pattern = pattern;
      this.pattern = this.pattern.replaceAll("[*?]", "");
    }

    /**
     * {@inheritDoc}
     * @see Comparator#compare(T,T)
     */
    public int compare (String o1, String o2)
    {
      double score1 = score(FileUtils.getBaseName(o1));
      double score2 = score(FileUtils.getBaseName(o2));

      return (int)Math.round(score1 - score2);
    }

    public double score (String path)
    {
      if (this.scores.containsKey(path)){
        return this.scores.get(path);
      }
      double score = 0 - this.distance.score(this.pattern, path);
      this.scores.put(path, score);
      return score;
    }

    /**
     * {@inheritDoc}
     * @see Comparator#equals(Object)
     */
    public boolean equals (Object obj)
    {
      return super.equals(obj);
    }
  }
}
