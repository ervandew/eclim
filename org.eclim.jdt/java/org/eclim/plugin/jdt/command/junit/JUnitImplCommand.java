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
package org.eclim.plugin.jdt.command.junit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.command.impl.ImplCommand;

import org.eclim.plugin.jdt.command.search.SearchRequestor;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;

import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;

import org.eclipse.jdt.internal.corext.dom.ASTNodes;

import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;

import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.Messages;

import org.eclipse.jdt.internal.junit.util.JUnitStubUtility;

import org.eclipse.jdt.internal.junit.wizards.WizardMessages;

import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;

import org.eclipse.text.edits.MultiTextEdit;

/**
 * Command to handle creation of junit test stubs.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_junit_impl",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL s superType ARG," +
    "OPTIONAL m methods ARG"
)
public class JUnitImplCommand
  extends ImplCommand
{
  private static final Pattern TESTING_CLASS_NAME =
    Pattern.compile("(?:^Test([^a-z].*)|(.*)Test$)");

  private ThreadLocal<ITypeBinding> base = new ThreadLocal<ITypeBinding>();

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    IJavaProject javaProject = JavaUtils.getJavaProject(projectName);

    IType baseType = null;

    ICompilationUnit src = JavaUtils.getCompilationUnit(javaProject, file);
    IType type = src.getTypes()[0];
    String name = type.getElementName();
    Matcher matcher = TESTING_CLASS_NAME.matcher(name);

    // test class uses a Test prefix or suffix, so remove that and search for
    // the fully qualified result matching the same package name.
    if (matcher.matches()){
      name = matcher.group(2);
      String fqn = src.getParent().getElementName() + '.' + name;
      baseType = javaProject.findType(fqn);
    }

    // no type found by removing Test prefix / suffix with the same package, so
    // search for the unqualified name (sans the Test prefix / suffix).
    if (baseType == null){
      IType[] types = findTypes(javaProject, type, name);
      if (types.length > 0){
        if (types.length > 1){
          return Services.getMessage("junit.testing.class.not.resolved");
        }
        baseType = types[0];
      }
    }

    if (baseType == null){
      return Services.getMessage("junit.testing.class.not.found");
    }

    RefactoringASTParser parser =
      new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL);
    CompilationUnit cu = parser.parse(baseType.getCompilationUnit(), true);
    ITypeBinding base = ASTNodeSearchUtil
      .getTypeDeclarationNode(baseType, cu).resolveBinding();
    this.base.set(base);

    return super.execute(commandLine);
  }

  @Override
  protected List<IMethodBinding> getOverridableMethods(
      CompilationUnit cu, ITypeBinding typeBinding)
    throws Exception
  {
    String version = getPreferences().getValue(
        cu.getJavaElement().getJavaProject().getProject(),
        "org.eclim.java.junit.version");

    HashSet<String> testMethods = new HashSet<String>();
    for (IMethodBinding method : typeBinding.getDeclaredMethods()){
      int modifiers = method.getModifiers();
      if (!method.isConstructor() &&
          !Modifier.isPrivate(modifiers))
      {
        testMethods.add(method.getName());
      }
    }

    List<IMethodBinding> testable = new ArrayList<IMethodBinding>();
    ITypeBinding objectBinding =
      cu.getAST().resolveWellKnownType("java.lang.Object");
    ITypeBinding parentBinding = base.get();
    while (parentBinding != null && !parentBinding.equals(objectBinding)){
      for (IMethodBinding method : parentBinding.getDeclaredMethods()){
        String name = method.getName();
        if (version.equals("3")){
          name = "test" + StringUtils.capitalize(name);
        }
        int modifiers = method.getModifiers();
        if (!method.isConstructor() &&
            !Modifier.isPrivate(modifiers) &&
            !testMethods.contains(name))
        {
          testable.add(method);
        }
      }
      parentBinding = parentBinding.getSuperclass();
    }

    return testable;
  }

  @Override
  protected IWorkspaceRunnable getImplOperation(
      ICompilationUnit src,
      IType type,
      Set<String> chosen,
      IJavaElement sibling,
      int pos,
      CommandLine commandLine)
    throws Exception
  {
    RefactoringASTParser parser = new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL);
    CompilationUnit cu = parser.parse(type.getCompilationUnit(), true);
    ITypeBinding typeBinding = ASTNodes.getTypeBinding(cu, type);

    String superType = commandLine.getValue(Options.SUPERTYPE_OPTION);
    List<IMethodBinding> testable = getOverridableMethods(cu, typeBinding);
    List<IMethodBinding> tests = new ArrayList<IMethodBinding>();
    for (IMethodBinding binding : testable){
      ITypeBinding declBinding = binding.getDeclaringClass();
      String fqn = declBinding.getQualifiedName().replaceAll("<.*?>", "");
      if (fqn.equals(superType) && isChosen(chosen, binding)){
        tests.add(binding);
      }
    }

    if (tests.size() > 0){
      return new AddTestMethodsOperation(
          cu, typeBinding, tests.toArray(new IMethodBinding[tests.size()]));
    }
    return null;
  }

  private IType[] findTypes(IJavaProject javaProject, IType ignore, String name)
    throws Exception
  {
    SearchPattern pattern =
      SearchPattern.createPattern(name,
          IJavaSearchConstants.TYPE,
          IJavaSearchConstants.DECLARATIONS,
          SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
    IJavaSearchScope scope =
      SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject});
    SearchRequestor requestor = new SearchRequestor();
    SearchEngine engine = new SearchEngine();
    SearchParticipant[] participants =
      new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()};
    engine.search(pattern, participants, scope, requestor, null);

    ArrayList<IType> types = new ArrayList<IType>();
    if (requestor.getMatches().size() > 0){
      for (SearchMatch match : requestor.getMatches()){
        if(match.getAccuracy() != SearchMatch.A_ACCURATE){
          continue;
        }
        IJavaElement element = (IJavaElement)match.getElement();
        if (element.getElementType() == IJavaElement.TYPE){
          IType type = (IType)element;
          if (!type.equals(ignore) && type.getCompilationUnit() != null){
            types.add(type);
          }
        }
      }
    }
    return types.toArray(new IType[types.size()]);
  }

  private class AddTestMethodsOperation
    implements IWorkspaceRunnable
  {
    private CompilationUnit cu;
    private ITypeBinding typeBinding;
    private IMethodBinding[] methodBindings;

    public AddTestMethodsOperation(
        CompilationUnit cu,
        ITypeBinding typeBinding,
        IMethodBinding[] methodBindings)
    {
      this.cu = cu;
      this.typeBinding = typeBinding;
      this.methodBindings = methodBindings;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(IProgressMonitor monitor)
    {
      try{
        ICompilationUnit src = (ICompilationUnit)cu.getJavaElement();
        IJavaProject javaProject = src.getJavaProject();
        String version = getPreferences().getValue(
            cu.getJavaElement().getJavaProject().getProject(),
            "org.eclim.java.junit.version");

        MultiTextEdit edit = new MultiTextEdit();
        if (!version.equals("3")){
          ImportRewrite imports = StubUtility.createImportRewrite(cu, true);
          imports.addStaticImport("org.junit.Assert", "*", false);
          imports.addImport(JUnitCorePlugin.JUNIT4_ANNOTATION_NAME);
          edit.addChild(imports.rewriteImports(null));
        }

        AST ast = cu.getAST();
        ASTRewrite astRewrite = ASTRewrite.create(ast);
        ASTNode node = cu.findDeclaringNode(typeBinding);
        ChildListPropertyDescriptor property= ((AbstractTypeDeclaration)node)
          .getBodyDeclarationsProperty();
        ListRewrite memberRewriter = astRewrite.getListRewrite(node, property);
        HashSet<String> added = new HashSet<String>();
        for (IMethodBinding binding : methodBindings){
          String name = binding.getName();
          if (version.equals("3")){
            name = "test" + StringUtils.capitalize(name);
          }
          if (added.contains(name)){
            continue;
          }
          added.add(name);

          MethodDeclaration stub = ast.newMethodDeclaration();
          stub.setConstructor(false);
          stub.modifiers().addAll(ast.newModifiers(Modifier.PUBLIC));

          if (!version.equals("3")){
            Annotation marker = ast.newMarkerAnnotation();
            marker.setTypeName(ast.newSimpleName("Test"));
            astRewrite
              .getListRewrite(stub, MethodDeclaration.MODIFIERS2_PROPERTY)
              .insertFirst(marker, null);
          }

          stub.setName(ast.newSimpleName(name));

          Block body = ast.newBlock();
          stub.setBody(body);

          String todoTask = "";
          String todoTaskTag= JUnitStubUtility.getTodoTaskTag(javaProject);
          if (todoTaskTag != null) {
            todoTask= " // " + todoTaskTag;
          }
          String message = WizardMessages
            .NewTestCaseWizardPageOne_not_yet_implemented_string;
          body.statements().add(astRewrite.createStringPlaceholder(
                todoTask,
                ASTNode.RETURN_STATEMENT));
          body.statements().add(astRewrite.createStringPlaceholder(
                Messages.format("fail(\"{0}\");", message),
                ASTNode.RETURN_STATEMENT));

          memberRewriter.insertLast(stub, null);
        }
        edit.addChild(astRewrite.rewriteAST());

        JavaModelUtil.applyEdit(src, edit, true, null);
      }catch(Exception e){
        throw new RuntimeException(e);
      }
    }
  }
}
