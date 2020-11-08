/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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

import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;

import org.eclipse.jdt.core.search.TypeNameMatch;

import org.eclipse.jdt.internal.core.manipulation.StubUtility;

import org.eclipse.jdt.internal.corext.codemanipulation.AddImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationMessages;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

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
      CompilationUnit astRoot = SharedASTProviderCore
        .getAST(src, SharedASTProviderCore.WAIT_YES, null);

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

    TextEdit groupingEdit = importGroupingEdit(src, edits.getOffset() + 1);
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
    CompilationUnit astRoot = SharedASTProviderCore
      .getAST(src, SharedASTProviderCore.WAIT_YES, null);
    ASTNode node = NodeFinder.perform(astRoot, offset, 1);
    MultiTextEdit edit = new MultiTextEdit();
    if (node != null && node.getNodeType() == ASTNode.IMPORT_DECLARATION){
      ImportDeclaration imprt = (ImportDeclaration)node;

      ASTNode next = getNext(astRoot, node, lineDelim);
      while (next != null && next.getNodeType() == ASTNode.IMPORT_DECLARATION){
        ImportDeclaration nextImprt = (ImportDeclaration)next;
        if (!ImportUtils.importsInSameGroup(separationLevel, imprt, nextImprt)){
          int end =
            imprt.getStartPosition() +
            imprt.getLength() +
            lineDelim.length();
          addLineDelim(astRoot, edit, end, lineDelim);
        }
        next = getNext(astRoot, next, lineDelim);
        imprt = nextImprt;
      }

      // reset imprt ref back to the one we are importing.
      imprt = (ImportDeclaration)node;
      ASTNode prev = getPrev(astRoot, node, lineDelim);
      if (prev != null && prev.getNodeType() == ASTNode.IMPORT_DECLARATION){
        ImportDeclaration prevImprt = (ImportDeclaration)prev;
        if (!ImportUtils.importsInSameGroup(separationLevel, imprt, prevImprt)){
          int end = prev.getStartPosition() + prev.getLength() + lineDelim.length();
          addLineDelim(astRoot, edit, end, lineDelim);
        }
      }
    }
    return edit.getChildrenSize() > 0 ? edit : null;
  }

  private ASTNode getNext(CompilationUnit astRoot, ASTNode node, String lineDelim)
  {
    int offset = node.getStartPosition() + node.getLength() + lineDelim.length();
    ASTNode next = NodeFinder.perform(astRoot, offset, 1);

    // if the offset is on a blank line, then the compilation unit will be
    // returned as the node, so advance 1 and try again.
    while (next != null && next.getNodeType() == ASTNode.COMPILATION_UNIT){
      next = NodeFinder.perform(astRoot, ++offset, 1);
    }

    return next;
  }

  private ASTNode getPrev(CompilationUnit astRoot, ASTNode node, String lineDelim)
  {
    int offset = node.getStartPosition() - (lineDelim.length() + 1);
    return NodeFinder.perform(astRoot, offset, 1);
  }

  /**
   * Add an InsertEdit to add a new blank line if one doesn't already exist.
   *
   * @param astRoot The CompilationUnit to modify.
   * @param edit The MultiTextEdit to add the new InsertEdit to.
   * @param offset The offset to add the new blank line at.
   * @param lineDelim The line delimiter to use.
   */
  private void addLineDelim(
      CompilationUnit astRoot, MultiTextEdit edit, int offset, String lineDelim)
  {
    ASTNode node = NodeFinder.perform(astRoot, offset, 1);
    if (node != null && node.getNodeType() == ASTNode.IMPORT_DECLARATION){
      edit.addChild(new InsertEdit(offset, lineDelim));
    }
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
