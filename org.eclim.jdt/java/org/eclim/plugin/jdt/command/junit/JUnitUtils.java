/**
 * Copyright (C) 2012 - 2020  Eric Van Dewoestine
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Utility methods for junit commands.
 *
 * @author Eric Van Dewoestine
 */
public class JUnitUtils
{
  private static final Pattern TESTING_CLASS_NAME =
    Pattern.compile("(?:^Test([^a-z].*)|(.*)Test$)");

  /**
   * Find the test ICompilationUnit for the supplied non-test type.
   *
   * @param javaProject The java project to search in.
   * @param type The non-test type to find the corresponding test for.
   * @return The test ICompilationUnit or null if one could not be found.
   */
  public static ICompilationUnit findTest(IJavaProject javaProject, IType type)
  {
    // possible names to try.
    String[] names = new String[]{
      type.getFullyQualifiedName() + "Test",
      type.getPackageFragment().getElementName() + ".Test" + type.getElementName(),
    };
    for (String name : names){
      IType[] results = TypeUtils.findTypes(javaProject, name);
      for (IType result : results){
        if (result.getCompilationUnit() != null){
          return result.getCompilationUnit();
        }
      }
    }
    return null;
  }

  /**
   * Find the ICompilationUnit for the supplied test type.
   *
   * @param javaProject The java project to search in.
   * @param type The test type to find the corresponding class for.
   * @return The ICompilationUnit or null if one could not be found.
   */
  public static ICompilationUnit findClass(IJavaProject javaProject, IType type)
  {
    String name = type.getElementName();
    Matcher matcher = TESTING_CLASS_NAME.matcher(name);

    IType found = null;
    try{
      // test class uses a Test prefix or suffix, so remove that and search for
      // the fully qualified result matching the same package name.
      if (matcher.matches()){
        name = matcher.group(2);
        if (name == null){
          name = matcher.group(1);
        }
        String fqn = type.getPackageFragment().getElementName() + '.' + name;
        found = javaProject.findType(fqn);
      }

      // no type found by removing Test prefix / suffix with the same package, so
      // search for the unqualified name (sans the Test prefix / suffix).
      if (found == null){
        IType[] types = findTypes(javaProject, type, name);
        if (types.length == 1){
          found = types[0];
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return found != null ? found.getCompilationUnit() : null;
  }

  private static IType[] findTypes(
      IJavaProject javaProject, IType ignore, String name)
  {
    ArrayList<IType> types = new ArrayList<IType>();
    for (IType type : TypeUtils.findTypes(javaProject, name)){
      if (!type.equals(ignore) && type.getCompilationUnit() != null){
        types.add(type);
      }
    }
    return types.toArray(new IType[types.size()]);
  }

  /**
   * Attempt to find a corresponding test method in the specified test src for
   * the given non-test method.
   *
   * @param testSrc The test ICompilationUnit to look for a test method in.
   * @param method The regular non-test method to find a corresponding test for.
   * @return The test IMethod or null if one could not be found.
   */
  public static IMethod findTestMethod(ICompilationUnit testSrc, IMethod method)
  {
    String testName = "test" + StringUtils.capitalize(method.getElementName());
    StringBuffer testNameWithParams = new StringBuffer(testName);
    appendParameterNamesToMethodName(testNameWithParams, method.getParameterTypes());
    replaceIllegalCharacters(testNameWithParams);

    StringBuffer nameWithParams = new StringBuffer(method.getElementName());
    appendParameterNamesToMethodName(nameWithParams, method.getParameterTypes());
    replaceIllegalCharacters(nameWithParams);

    String[] methodNames = new String[]{
      testNameWithParams.toString(),
      nameWithParams.toString(),
      testName,
      method.getElementName(),
    };
    try{
      for (String name : methodNames){
        IMethod test = testSrc.getTypes()[0].getMethod(name, null);
        if (test != null && test.exists()){
          return test;
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return null;
  }

  /**
   * Attempt to find a corresponding class method in the specified src for
   * the given test method.
   *
   * @param src The ICompilationUnit to look for the method in.
   * @param testMethod The test method to find the corresponding class method for.
   * @return The IMethod or null if one could not be found.
   */
  public static IMethod findClassMethod(ICompilationUnit src, IMethod testMethod)
  {
    ICompilationUnit testSrc = testMethod.getCompilationUnit();
    if (testSrc == null){
      return null;
    }

    try{
      IType testType = testSrc.getTypes()[0];

      // brute force since we can't assume a naming convention.
      IMethod[] methods = src.getTypes()[0].getMethods();
      for (IMethod method : methods){
        String testName = "test" + StringUtils.capitalize(method.getElementName());
        StringBuffer testNameWithParams = new StringBuffer(testName);
        appendParameterNamesToMethodName(
            testNameWithParams, method.getParameterTypes());
        replaceIllegalCharacters(testNameWithParams);

        StringBuffer nameWithParams = new StringBuffer(method.getElementName());
        appendParameterNamesToMethodName(nameWithParams, method.getParameterTypes());
        replaceIllegalCharacters(nameWithParams);

        String[] methodNames = new String[]{
          testNameWithParams.toString(),
          nameWithParams.toString(),
        };
        for (String name : methodNames){
          if (testMethod.equals(testType.getMethod(name, null))){
            return method;
          }
        }
      }

      for (IMethod method : methods){
        String testName = "test" + StringUtils.capitalize(method.getElementName());
        String[] methodNames = new String[]{
          testName,
          method.getElementName(),
        };
        for (String name : methodNames){
          if (testMethod.equals(testType.getMethod(name, null))){
            return method;
          }
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    return null;
  }

  /* from org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageOne */
  // CHECKSTYLE:OFF

  private static final String QUESTION_MARK_TAG = "Q";
  private static final String OF_TAG = "Of";
  private static void appendParameterNamesToMethodName(
      StringBuffer buffer, String[] parameters)
  {
    for (String parameter : parameters) {
      final StringBuffer buf = new StringBuffer(
          Signature.getSimpleName(
            Signature.toString(
              Signature.getElementType(parameter))));
      final char character = buf.charAt(0);
      if (buf.length() > 0 && !Character.isUpperCase(character)){
        buf.setCharAt(0, Character.toUpperCase(character));
      }
      buffer.append(buf.toString());
      for (int j = 0, arrayCount = Signature.getArrayCount(parameter);
           j < arrayCount; j++)
      {
        buffer.append("Array");
      }
    }
  }
  private static void replaceIllegalCharacters(StringBuffer buffer)
  {
    char character = 0;
    for (int index = buffer.length() - 1; index >= 0; index--) {
      character = buffer.charAt(index);
      if (Character.isWhitespace(character)){
        buffer.deleteCharAt(index);
      }else if (character == '<'){
        buffer.replace(index, index + 1, OF_TAG);
      }else if (character == '?'){
        buffer.replace(index, index + 1, QUESTION_MARK_TAG);
      // Skipping this for now so we don't rely on sun packages.
      /*}else if (!Character.isJavaIdentifierPart(character)) {
        // Check for surrogates
        if (!UTF16.isSurrogate(character)) {
          buffer.deleteCharAt(index);
        }*/
      }
    }
  }
  // CHECKSTYLE:ON
}
