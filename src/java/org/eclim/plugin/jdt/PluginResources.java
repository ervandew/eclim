/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.jdt;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.jdt.preference.OptionHandler;

import org.eclim.plugin.jdt.project.JavaProjectManager;

import org.eclim.preference.PreferenceFactory;
import org.eclim.preference.Preferences;

import org.eclim.project.ProjectManagement;
import org.eclim.project.ProjectNatureFactory;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class PluginResources
  extends AbstractPluginResources
{
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.jdt";

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#initialize(String)
   */
  @Override
  public void initialize (String _name)
  {
    super.initialize(_name);

    Preferences.addOptionHandler("org.eclipse.jdt", new OptionHandler());
    ProjectNatureFactory.addNature("java", "org.eclipse.jdt.core.javanature");
    ProjectManagement.addProjectManager(
        "org.eclipse.jdt.core.javanature", new JavaProjectManager());

    PreferenceFactory.addPreferences("org.eclipse.jdt.core.javanature",
      "JDT org.eclim.java.logging.impl commons-logging (commons-logging|log4j|slf4j|jdk)\n" +
      "JDT org.eclim.java.validation.ignore.warnings false (true|false)\n" +
      "JDT/Javadoc org.eclim.java.doc.version \\$Revision\\$\n" +
      "JDT/JUnit org.eclim.java.junit.command\n" +
      "JDT/JUnit org.eclim.java.junit.output_dir\n" +
      "JDT/JUnit org.eclim.java.junit.src_dir"
    );
    PreferenceFactory.addOptions("org.eclipse.jdt.core.javanature",
      "JDT org.eclipse.jdt.core.compiler.source 1\\.[3-6]"
    );

    registerCommand("java_src_update",
        org.eclim.plugin.jdt.command.src.SrcUpdateCommand.class);
    registerCommand("java_src_exists",
        org.eclim.plugin.jdt.command.src.SrcFileExistsCommand.class);
    registerCommand("java_src_find",
        org.eclim.plugin.jdt.command.src.SrcFindCommand.class);
    registerCommand("java_search",
        org.eclim.plugin.jdt.command.search.SearchCommand.class);
    registerCommand("java_docsearch",
        org.eclim.plugin.jdt.command.doc.DocSearchCommand.class);
    registerCommand("java_import",
        org.eclim.plugin.jdt.command.include.ImportCommand.class);
    registerCommand("java_imports_unused",
        org.eclim.plugin.jdt.command.include.UnusedImportsCommand.class);
    registerCommand("java_complete",
        org.eclim.plugin.jdt.command.complete.CodeCompleteCommand.class);
    registerCommand("java_correct",
        org.eclim.plugin.jdt.command.correct.CodeCorrectCommand.class);
    registerCommand("java_impl",
        org.eclim.plugin.jdt.command.impl.ImplCommand.class);
    registerCommand("java_junit_impl",
        org.eclim.plugin.jdt.command.junit.JUnitImplCommand.class);
    registerCommand("java_delegate",
        org.eclim.plugin.jdt.command.delegate.DelegateCommand.class);
    registerCommand("java_bean_properties",
        org.eclim.plugin.jdt.command.bean.PropertiesCommand.class);
    registerCommand("java_constructor",
        org.eclim.plugin.jdt.command.constructor.ConstructorCommand.class);
    registerCommand("java_regex",
        org.eclim.plugin.jdt.command.regex.RegexCommand.class);
    registerCommand("java_class_prototype",
        org.eclim.plugin.jdt.command.src.ClassPrototypeCommand.class);
    registerCommand("java_classpath_variables",
        org.eclim.plugin.jdt.command.classpath.ClasspathVariablesCommand.class);
    registerCommand("java_classpath_variable_create",
        org.eclim.plugin.jdt.command.classpath.ClasspathVariableCreateCommand.class);
    registerCommand("java_classpath_variable_delete",
        org.eclim.plugin.jdt.command.classpath.ClasspathVariableDeleteCommand.class);
    registerCommand("javadoc_comment",
        org.eclim.plugin.jdt.command.doc.CommentCommand.class);
    registerCommand("log4j_validate",
        org.eclim.plugin.jdt.command.log4j.ValidateCommand.class);
    registerCommand("webxml_validate",
        org.eclim.plugin.jdt.command.webxml.ValidateCommand.class);
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#getBundleBaseName()
   */
  protected String getBundleBaseName ()
  {
    return "org/eclim/plugin/jdt/messages";
  }
}
