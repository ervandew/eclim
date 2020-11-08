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
package org.eclim.plugin.jdt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Properties;

import org.eclim.Services;

import org.eclim.logging.Logger;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.core.preference.PreferenceFactory;
import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.jdt.preference.OptionHandler;

import org.eclim.plugin.jdt.project.JavaProjectManager;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.ui.JavaUI;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine
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
   * The eclipse nature id for this plugin.
   */
  public static final String NATURE = JavaCore.NATURE_ID;

  private static final Logger logger = Logger.getLogger(PluginResources.class);

  private static final String VARIABLES = "resources/classpath_variables";

  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    logger.debug("Initializing java environment");

    // initialize variables.
    initializeVars(VARIABLES);

    Preferences.addOptionHandler("org.eclipse.jdt", new OptionHandler());
    ProjectNatureFactory.addNature("java", NATURE);
    ProjectManagement.addProjectManager(NATURE, new JavaProjectManager());

    PreferenceFactory.addPreferences(NATURE,
      "JDT org.eclim.java.logging.impl commons-logging " +
        "(commons-logging|log4j|slf4j|jdk|custom)\n" +
      "JDT org.eclim.java.logging.template logger.gst\n" +
      "JDT org.eclim.java.import.package_separation_level 1 (-1|\\d+)\n" +
      "JDT org.eclim.java.import.exclude " +
        "[\"^com\\.sun\\..*\",\"^sunw\\?\\..*\"] JSON[]\n" +
      "JDT org.eclim.java.format.strip_trialing_whitespace true (true|false)\n" +
      "JDT org.eclim.java.checkstyle.config\n" +
      "JDT org.eclim.java.checkstyle.properties\n" +
      "JDT org.eclim.java.checkstyle.onvalidate false (true|false)\n" +
      "JDT org.eclim.java.run.mainclass none ^[a-zA-Z0-9_.]*$\n" +
      "JDT org.eclim.java.run.jvmargs [] JSON[^-.*]\n" +
      "JDT org.eclim.java.search.sort [] JSON[.*]\n" +
      "JDT/Javadoc org.eclim.java.doc.version\n" +
      "JDT/Javadoc org.eclim.java.doc.dest doc\n" +
      "JDT/Javadoc org.eclim.java.doc.sourcepath\n" +
      "JDT/Javadoc org.eclim.java.doc.packagenames\n" +
      "JDT/JUnit org.eclim.java.junit.output_dir\n" +
      "JDT/JUnit org.eclim.java.junit.cwd\n" +
      "JDT/JUnit org.eclim.java.junit.jvmargs [] JSON[^-.*]\n" +
      "JDT/JUnit org.eclim.java.junit.sysprops [] JSON[^(-D)?\\S+=.*]\n" +
      "JDT/JUnit org.eclim.java.junit.envvars [] JSON[^\\w+=.*]"
    );
    // Indentation settings found in DefaultCodeFormatterConstants
    PreferenceFactory.addOptions(NATURE,
      "JDT org.eclipse.jdt.core.compiler.source ^(1\\.[3-8]|9|10)$\n" +
      "JDT org.eclipse.jdt.ui.importorder [a-zA-Z0-9_.#;]+\n" +
      "JDT/Javadoc " + JavaUI.ID_PLUGIN + ".project_javadoc_location\n" +
      "JDT/CodeComplete " +
        "org.eclipse.jdt.core.codeComplete.camelCaseMatch (enabled|disabled)\n" +
      "JDT/CodeComplete " +
        "org.eclipse.jdt.core.codeComplete.deprecationCheck (enabled|disabled)\n" +
      "JDT/CodeComplete " +
        "org.eclipse.jdt.core.codeComplete.visibilityCheck (enabled|disabled)"
    );
  }

  @Override
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/jdt/messages";
  }

  /**
   * Loads variables from property file.
   *
   * @param variable The prefix of the property file.
   */
  protected void initializeVars(String variable)
  {
    String file = "/" + variable + ".properties";
    logger.debug("Loading classpath variables from '{}'.", file);
    InputStream in = null;
    try{
      in = getClass().getResourceAsStream(file);
      String propertiesString = IOUtils.toString(in);

      HashMap<Object, String> values = new HashMap<Object, String>();
      for(Object key : System.getProperties().keySet()){
        String value = System.getProperty((String)key);
        if (value != null){
          values.put(key, value.replace('\\', '/'));
        }
      }
      propertiesString =
        StringUtils.replacePlaceholders(propertiesString, values);

      Properties properties = new Properties();
      properties.load(new ByteArrayInputStream(propertiesString.getBytes()));

      for(Object key : properties.keySet()){
        String name = (String)key;
        IPath path = new Path(properties.getProperty(name));
        logger.debug("Setting classpath variable '{}' to path '{}'", name, path);
        JavaCore.setClasspathVariable(name, path, null);
      }
    }catch(Exception e){
      logger.error("", e);
    }finally{
      IOUtils.closeQuietly(in);
    }
  }
}
