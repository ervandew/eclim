/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.src;

import java.io.File;

import java.util.ArrayList;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import org.eclipse.jdt.core.formatter.CodeFormatter;

/**
 * Command to generate a new Java Type file
 *
 * @author Daniel Leong, Eric Van Dewoestine
 */
@Command(
  name = "java_new",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED t type ARG," +
    "REQUIRED n name ARG," +
    "OPTIONAL r root ARG"
)
public class NewCommand
  extends AbstractCommand
{
  static final String TEMPLATE =
    "package %1$s;\n\n" +
    "public %2$s %3$s {\n" +
    "}";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String type = commandLine.getValue(Options.TYPE_OPTION);
    String name = commandLine.getValue(Options.NAME_OPTION);
    String srcRoot = commandLine.getValue(Options.ROOT_OPTION);

    // handle someone typing a file path instead of a fully qualified class name
    if (name.endsWith(".java")){
      name = name.substring(0, name.length() - 5);
    }
    name = name.replace('/', '.');

    int classStart = name.lastIndexOf('.');
    final String packageName = classStart >= 0 ?
      name.substring(0, classStart) : name;
    final String typeName = classStart >= 0 ?
      name.substring(classStart + 1) : name;
    final String fileName = typeName + ".java";

    IJavaProject javaProject = JavaUtils.getJavaProject(projectName);

    ArrayList<IPackageFragmentRoot> roots =
      new ArrayList<IPackageFragmentRoot>();

    // find all roots the requested package is found in.
    for (IPackageFragment f : javaProject.getPackageFragments()) {
      if (f.getElementName().equals(packageName)){
        IJavaElement parent = f.getParent();
        while (parent != null){
          if (parent instanceof IPackageFragmentRoot){
            IPackageFragmentRoot root = (IPackageFragmentRoot)parent;
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE){
              roots.add(root);
            }
            break;
          }
          parent = parent.getParent();
        }
      }
    }

    // the package isn't found in any roots
    if (roots.size() == 0){
      // no root supplied, so we have to add all src roots to a list for the
      // user to choose from.
      for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()){
        if (root.getKind() == IPackageFragmentRoot.K_SOURCE){
          roots.add(root);
        }
      }
    }

    // still no source roots, so we have to fail
    if (roots.size() == 0){
      throw new RuntimeException("No project source directories found.");
    }

    if (roots.size() > 1){
      // user chosen root supplied, so grab that one.
      if (srcRoot != null){
        roots.clear();
        for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()){
          if (root.getKind() == IPackageFragmentRoot.K_SOURCE &&
              root.getElementName().equals(srcRoot)){
            roots.add(root);
            break;
          }
        }

        if (roots.size() == 0){
          throw new RuntimeException(
              "Unable to find project source directory: " + srcRoot);
        }
      }

      if (roots.size() > 1){
        ArrayList<String> srcRoots = new ArrayList<String>();
        for (IPackageFragmentRoot root : roots){
          srcRoots.add(root.getElementName());
        }
        return srcRoots;
      }
    }

    IPackageFragmentRoot root = roots.get(0);
    IPackageFragment fragment =
      root.createPackageFragment(packageName, false, null);

    // locate the to-be created file
    File fragmentPath = fragment.getUnderlyingResource()
      .getLocation().toFile();
    final File file = new File(fragmentPath, fileName);

    // make sure eclipse is up to date, in case the user
    //  deleted the file outside of eclipse's knowledge
    fragment.getUnderlyingResource()
      .refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());

    // create!
    final String content = String.format(TEMPLATE,
        packageName, getType(type), typeName);

    // NB: If we delete the file outside of Eclipse'
    //  awareness, it will whine if we then try to
    //  recreate it. So, if we *know* the file doesn't
    //  exist, force it.
    ICompilationUnit unit = fragment.createCompilationUnit(
          fileName,
          content,
          false,
          new NullProgressMonitor());

    if (unit == null || !file.exists()) {
      throw new RuntimeException("Could not create " + file);
    }

    JavaUtils.format(
        unit, CodeFormatter.K_COMPILATION_UNIT,
        0, unit.getBuffer().getLength());

    return file.getAbsolutePath();
  }

  private String getType(String type)
  {
    if ("abstract".equals(type)) {
      return "abstract class";
    }

    return type;
  }
}
