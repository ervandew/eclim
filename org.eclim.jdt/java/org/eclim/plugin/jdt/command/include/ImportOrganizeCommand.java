/**
 * Copyright (C) 2012 - 2018  Eric Van Dewoestine
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
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;

import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jdt.core.manipulation.OrganizeImportsOperation;

import org.eclipse.jdt.core.search.TypeNameMatch;

import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

import org.eclipse.jdt.ui.SharedASTProvider;

import org.eclipse.text.edits.TextEdit;

/**
 * Command to organize imports (add missing, remove unused, sort, etc.) for the
 * specified java source file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_import_organize",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG," +
    "OPTIONAL t types ARG"
)
public class ImportOrganizeCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String types = commandLine.getValue(Options.TYPE_OPTION);
    int offset = getOffset(commandLine);

    ICompilationUnit src = JavaUtils.getCompilationUnit(projectName, file);
    IProject project = src.getJavaProject().getProject();
    int oldLength = src.getBuffer().getLength();
    CompilationUnit astRoot = SharedASTProvider
      .getAST(src, SharedASTProvider.WAIT_YES, null);

    String[] typeNames = types != null ? StringUtils.split(types, ',') : null;
    ChooseImports query = new ChooseImports(project, typeNames);
    CodeGenerationSettings settings = JavaPreferencesSettings
      .getCodeGenerationSettings(src.getJavaProject());
    OrganizeImportsOperation op = new OrganizeImportsOperation(
       src, astRoot, settings.importIgnoreLowercase, true /* save */, true, query);

    TextEdit edit = op.createTextEdit(null);
    if (query.choices != null && query.choices.size() > 0){
      return query.choices;
    }

    if (edit != null){
      JavaModelUtil.applyEdit(src, edit, true, null);
      if (src.isWorkingCopy()) {
        src.commitWorkingCopy(false, null);
      }
    }

    // our own support for grouping imports based on package levels.
    TextEdit groupingEdit = ImportUtils.importGroupingEdit(src, getPreferences());
    if (groupingEdit != null){
      if (edit == null){
        edit = groupingEdit;
      }
      JavaModelUtil.applyEdit(src, groupingEdit, true, null);
      if (src.isWorkingCopy()) {
        src.commitWorkingCopy(false, null);
      }
    }

    if (edit != null){
      if (edit.getOffset() < offset){
        offset += src.getBuffer().getLength() - oldLength;
      }
      return Position.fromOffset(
          ProjectUtils.getFilePath(projectName, file), null, offset, 0);
    }

    return null;
  }

  private class ChooseImports
    implements OrganizeImportsOperation.IChooseImportQuery
  {
    public ArrayList<ArrayList<String>> choices;
    private IProject project;
    private HashSet<String> types;

    public ChooseImports(IProject project, String[] types)
    {
      this.project = project;
      if (types != null){
        this.types = new HashSet<String>(types.length);
        for (String type : types){
          this.types.add(type);
        }
      }
    }

    public TypeNameMatch[] chooseImports(
        TypeNameMatch[][] choices, ISourceRange[] ranges)
    {
      ArrayList<TypeNameMatch> chosen = new ArrayList<TypeNameMatch>();

      this.choices = new ArrayList<ArrayList<String>>();
      try{
        for (TypeNameMatch[] matches : choices){
          boolean foundChoice = false;
          if (types != null && types.size() > 0){
            for (TypeNameMatch match : matches){
              if (types.contains(match.getFullyQualifiedName())){
                foundChoice = true;
                chosen.add(match);
                break;
              }
            }
          }
          if (!foundChoice){
            ArrayList<String> names = new ArrayList<String>(matches.length);
            for (TypeNameMatch match : matches){
              String name = match.getFullyQualifiedName();
              if (!ImportUtils.isImportExcluded(project, name)){
                names.add(name);
              }
            }
            if (names.size() == 1){
              for (TypeNameMatch match : matches){
                if (names.get(0).equals(match.getFullyQualifiedName())){
                  chosen.add(match);
                  break;
                }
              }
            }else if (names.size() > 0){
              Collections.sort(names);
              this.choices.add(names);
            }
          }
        }
      }catch(Exception e){
        throw new RuntimeException(e);
      }

      return chosen.toArray(new TypeNameMatch[chosen.size()]);
    }
  }
}
