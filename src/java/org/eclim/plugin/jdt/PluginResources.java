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
package org.eclim.plugin.jdt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;

import org.eclim.Services;

import org.eclim.eclipse.AbstractEclimApplication;

import org.eclim.logging.Logger;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.jdt.preference.OptionHandler;

import org.eclim.plugin.jdt.project.JavaProjectManager;

import org.eclim.preference.PreferenceFactory;
import org.eclim.preference.Preferences;

import org.eclim.project.ProjectManagement;
import org.eclim.project.ProjectNatureFactory;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

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
  public static final String NATURE = "org.eclipse.jdt.core.javanature";

  private static final Logger logger = Logger.getLogger(PluginResources.class);

  private static final String VARIABLES = "resources/classpath_variables";
  private static final String[] SRC_LOCATIONS = {
    "src.zip",
    "share/src.zip",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/src.zip",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/share/src.zip",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/../src.zip",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/../share/src.zip",
  };

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#initialize(String)
   */
  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    logger.debug("Initializing java environment");

    // initialize variables.
    initializeJreSrc();
    initializeVars(VARIABLES);

    /*java.util.Hashtable options = JavaCore.getOptions();
    options.put(
      DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR,
      JavaCore.SPACE);
    options.put(
      DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
    options.put(
      DefaultCodeFormatterConstants.FORMATTER_USE_TABS_ONLY_FOR_LEADING_INDENTATIONS,
      DefaultCodeFormatterConstants.FALSE);
    JavaCore.setOptions(options);*/

    Preferences.addOptionHandler("org.eclipse.jdt", new OptionHandler());
    ProjectNatureFactory.addNature("java", "org.eclipse.jdt.core.javanature");
    ProjectManagement.addProjectManager(
        "org.eclipse.jdt.core.javanature", new JavaProjectManager());

    PreferenceFactory.addPreferences("org.eclipse.jdt.core.javanature",
      "JDT org.eclim.java.logging.impl commons-logging " +
        "(commons-logging|log4j|slf4j|jdk|custom)\n" +
      "JDT org.eclim.java.logging.template logger.gst\n" +
      "JDT org.eclim.java.validation.ignore.warnings false (true|false)\n" +
      "JDT org.eclim.java.checkstyle.config\n" +
      "JDT org.eclim.java.checkstyle.properties\n" +
      "JDT/Javadoc org.eclim.java.doc.version\n" +
      "JDT/Javadoc org.eclim.java.doc.dest doc\n" +
      "JDT/Javadoc org.eclim.java.doc.sourcepath\n" +
      "JDT/Javadoc org.eclim.java.doc.packagenames\n" +
      "JDT/Javac org.eclim.java.compile.sourcepath\n" +
      "JDT/JUnit org.eclim.java.junit.command\n" +
      "JDT/JUnit org.eclim.java.junit.output_dir\n" +
      "JDT/JUnit org.eclim.java.junit.src_dir"
    );
    PreferenceFactory.addOptions("org.eclipse.jdt.core.javanature",
      "JDT org.eclipse.jdt.core.compiler.source 1\\.[3-6]"
    );
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#close()
   */
  @Override
  public void close()
    throws Exception
  {
    logger.debug("Shutting down java environment");
    super.close();
    if (!AbstractEclimApplication.getInstance().isHeaded()){
      JavaCore.getJavaCore().stop(null);
    }
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#getBundleBaseName()
   */
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/jdt/messages";
  }

  /**
   * Performs additional logic to locate jre src zip file in alternate locations
   * not checked by eclipse.
   */
  protected void initializeJreSrc()
  {
    // doing a straight JavaCore.setClasspathVariable() doesn't work, so we need
    // to modify the library path of the default vm install.
    try{
      IVMInstall vm = JavaRuntime.getDefaultVMInstall();
      LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vm);
      LibraryLocation[] newLocations = new LibraryLocation[locations.length];
      for(int ii = 0; ii < locations.length; ii++){
        IPath libraryPath = locations[ii].getSystemLibraryPath();

        // eclipse didn't find src.zip, so search other known locations.
        if (libraryPath.lastSegment().equals("rt.jar") &&
            (locations[ii].getSystemLibrarySourcePath().isEmpty() ||
             !locations[ii].getSystemLibrarySourcePath().toFile().exists())){
          IPath jreSrc = null;

          logger.debug("Attempting to locate jre src.zip for JAVA_HOME: {}",
              SystemUtils.JAVA_HOME);
          for (int jj = 0; jj < SRC_LOCATIONS.length; jj++){
            String location = SRC_LOCATIONS[jj];

            // absolute path
            if (location.startsWith("/") ||
                location.indexOf(':') != -1){
              jreSrc = new Path(location);

            // relative path
            }else{
              jreSrc = libraryPath.removeLastSegments(3).append(location);
            }

            logger.debug("Trying location: {}", jreSrc);
            if(jreSrc.toFile().exists()){
              logger.info("Setting '{}' to '{}'",
                  JavaRuntime.JRESRC_VARIABLE, jreSrc);
              newLocations[ii] = new LibraryLocation(
                  locations[ii].getSystemLibraryPath(),
                  jreSrc,
                  locations[ii].getPackageRootPath(),
                  locations[ii].getJavadocLocation());
              break;
            }
          }

          // jre src not found.
          if(!jreSrc.toFile().exists()){
            logger.warn("Unable to locate jre src.zip.");
            newLocations[ii] = new LibraryLocation(
                locations[ii].getSystemLibraryPath(),
                Path.EMPTY,
                locations[ii].getPackageRootPath(),
                locations[ii].getJavadocLocation());
          }
        }else{
          newLocations[ii] = locations[ii];
        }
      }
      vm.setLibraryLocations(newLocations);
    }catch(Exception e){
      logger.error("", e);
    }
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
        values.put(key, System.getProperty((String)key).replace('\\', '/'));
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
