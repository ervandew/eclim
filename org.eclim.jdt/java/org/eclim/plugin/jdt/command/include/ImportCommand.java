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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;
import java.util.Collections;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;

import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.search.TypeNameMatch;

import org.eclipse.jdt.internal.corext.codemanipulation.AddImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationMessages;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.ui.SharedASTProvider;

import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Command to add an import to a java source file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_import",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG," +
    "OPTIONAL t type ARG"
)
public class ImportCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String type = commandLine.getValue(Options.TYPE_OPTION);
    int offset = getOffset(commandLine);
    ICompilationUnit src = JavaUtils.getCompilationUnit(projectName, file);
    IProject project = src.getJavaProject().getProject();

    TextEdit edits = null;
    int oldLength = src.getBuffer().getLength();
    if (type != null){
      CompilationUnit astRoot = SharedASTProvider
        .getAST(src, SharedASTProvider.WAIT_YES, null);

      edits = new MultiTextEdit();
      ImportRewrite importRewrite = StubUtility.createImportRewrite(astRoot, true);
      ImportRewriteContext context = new ContextSensitiveImportRewriteContext(
          astRoot, offset, importRewrite);
      String res = importRewrite.addImport(type, context);
      if (type.equals(res)){
        return CodeGenerationMessages.AddImportsOperation_error_importclash;
      }

      TextEdit rewrite = importRewrite.rewriteImports(null);
      edits.addChild(rewrite);
      JavaModelUtil.applyEdit(src, edits, true, null);

    }else{
      ChooseImport query = new ChooseImport(project, type);
      try{
        AddImportsOperation op = new AddImportsOperation(
            src, offset, 1, query, true /* save */, true /* apply */);
        op.run(null);
        edits = op.getResultingEdit();
        if (edits == null){
          IStatus status = op.getStatus();
          return status.getSeverity() != IStatus.OK ? status.getMessage() : null;
        }
      }catch(OperationCanceledException oce){
        return query.choices;
      }
    }

    TextEdit groupingEdit = importGroupingEdit(src, edits.getOffset());
    if (groupingEdit != null){
      JavaModelUtil.applyEdit(src, groupingEdit, true, null);
    }

    if (src.isWorkingCopy()) {
      src.commitWorkingCopy(false, null);
    }

    if (edits.getOffset() < offset){
      offset += src.getBuffer().getLength() - oldLength;
    }
    return Position.fromOffset(
        ProjectUtils.getFilePath(projectName, file), null, offset, 0);
  }

  private TextEdit importGroupingEdit(ICompilationUnit src, int offset)
    throws Exception
  {
    int separationLevel = getPreferences().getIntValue(
          src.getJavaProject().getProject(),
          "org.eclim.java.import.package_separation_level");
    String lineDelim = src.findRecommendedLineSeparator();
    CompilationUnit astRoot = SharedASTProvider
      .getAST(src, SharedASTProvider.WAIT_YES, null);
    ASTNode node = NodeFinder.perform(astRoot, offset, 1);
    MultiTextEdit edit = new MultiTextEdit();
    if (node != null && node.getNodeType() == ASTNode.IMPORT_DECLARATION){
      ImportDeclaration imprt = (ImportDeclaration)node;
      int end = node.getStartPosition() + node.getLength() + lineDelim.length();
      ASTNode next = NodeFinder.perform(astRoot, end, 1);

      if (next != null && next.getNodeType() == ASTNode.IMPORT_DECLARATION){
        ImportDeclaration nextImprt = (ImportDeclaration)next;
        if (!ImportUtils.importsInSameGroup(separationLevel, imprt, nextImprt)){
          edit.addChild(new InsertEdit(end, lineDelim));
        }
      }

      ASTNode prev = NodeFinder.perform(
          astRoot, offset - (lineDelim.length() + 1), 1);
      if (prev != null && prev.getNodeType() == ASTNode.IMPORT_DECLARATION){
        ImportDeclaration prevImprt = (ImportDeclaration)prev;
        if (!ImportUtils.importsInSameGroup(separationLevel, imprt, prevImprt)){
          end = prev.getStartPosition() + prev.getLength() + lineDelim.length();
          edit.addChild(new InsertEdit(end, lineDelim));
        }
      }
    }
    return edit.getChildrenSize() > 0 ? edit : null;
  }

  private class ChooseImport
    implements AddImportsOperation.IChooseImportQuery
  {
    public ArrayList<String> choices;
    private IProject project;
    private String type;

    public ChooseImport(IProject project, String type)
    {
      this.project = project;
      this.type = type;
    }

    @Override
    public TypeNameMatch chooseImport(TypeNameMatch[] choices, String name)
    {
      if (type != null){
        for (TypeNameMatch match : choices){
          if (type.equals(match.getFullyQualifiedName())){
            return match;
          }
        }
      }

      if (this.choices == null){ // just in case to prevent infinite recursive loop
        try{
          this.choices = new ArrayList<String>(choices.length);
          for (TypeNameMatch match : choices){
            String fqn = match.getFullyQualifiedName();
            if (!ImportUtils.isImportExcluded(project, fqn) &&
                !this.choices.contains(fqn))
            {
              this.choices.add(fqn);
            }
          }
          if (this.choices.size() == 1){
            type = this.choices.get(0);
            return chooseImport(choices, name);
          }
          Collections.sort(this.choices);
        }catch(Exception e){
          throw new RuntimeException(e);
        }
      }
      return null;
    }
  }
}
