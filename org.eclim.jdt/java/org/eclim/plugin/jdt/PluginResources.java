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
package org.eclim.plugin.jdt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Properties;

import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

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

import org.eclim.util.file.FileUtils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

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
  private static final String[] SRC_LOCATIONS = {
    "src.zip",
    "share/src.zip",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/src.jar",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/src.zip",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/share/src.zip",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/../src.zip",
    SystemUtils.JAVA_HOME.replace('\\', '/') + "/../share/src.zip",
  };

  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    logger.debug("Initializing java environment");

    // initialize variables.
    initializeJreSrc();
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
      "JDT org.eclipse.jdt.core.compiler.source 1\\.[3-8]\n" +
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
   * Performs additional logic to locate jre src zip file in alternate locations
   * not checked by eclipse.
   */
  protected void initializeJreSrc()
  {
    String jarName = Os.isFamily(Os.FAMILY_MAC) ? "classes.jar" : "rt.jar";
    // doing a straight JavaCore.setClasspathVariable() doesn't work, so we need
    // to modify the library path of the default vm install.
    try{
      IVMInstall vm = JavaRuntime.getDefaultVMInstall();
      LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vm);
      LibraryLocation[] newLocations = new LibraryLocation[locations.length];
      for(int ii = 0; ii < locations.length; ii++){
        IPath libraryPath = locations[ii].getSystemLibraryPath();

        // eclipse didn't find src.zip, so search other known locations.
        if (libraryPath.lastSegment().equals(jarName) &&
            (locations[ii].getSystemLibrarySourcePath().isEmpty() ||
             !locations[ii].getSystemLibrarySourcePath().toFile().exists()))
        {
          IPath jreSrc = null;

          logger.debug("Attempting to locate jre src.zip for JAVA_HOME: {}",
              SystemUtils.JAVA_HOME);
          for (int jj = 0; jj < SRC_LOCATIONS.length; jj++){
            String location = SRC_LOCATIONS[jj];

            // absolute path
            if (location.startsWith("/") ||
                location.indexOf(':') != -1)
            {
              jreSrc = new Path(location);

            // relative path
            }else{
              jreSrc = libraryPath.removeLastSegments(3).append(location);
            }

            logger.debug("Trying location: {}", jreSrc);
            if(jreSrc.toFile().exists()){
              break;
            }
          }

          // other possibilities on windows machines:
          // library path: C:/.../jre<version>/
          // src archive:  C:/.../jdk<version>/src.zip
          //   or
          // library path: C:/.../jre<major>/
          // src archive:  C:/.../jdk1.<major>.<minor>_<patch>/src.zip
          if (!jreSrc.toFile().exists() && Os.isFamily(Os.FAMILY_WINDOWS)){
            String path = libraryPath.toOSString()
              .replaceFirst("\\\\(lib\\\\)rt.jar", "");

            // first scenerio
            String altHome = path.replaceFirst(
                "jre(\\d+\\.\\d+\\.\\d+_\\d+)", "jdk$1");
            if (!altHome.equals(path)){
              jreSrc = new Path(altHome).append("src.zip");
            }

            // second scenerio
            if (!jreSrc.toFile().exists()){
              String base = FileUtils.getBaseName(path);
              final String major = base.replaceFirst("^jre(\\d)$", "$1");
              if (!major.equals(base)){
                File dir = new File(FileUtils.getFullPath(path));
                String[] jdks = dir.list(new FilenameFilter(){
                  private final Pattern JDK =
                    Pattern.compile("jdk\\d+\\." + major + "\\.\\d+_\\d+");
                  public boolean accept(File dir, String name){
                    return JDK.matcher(name).matches();
                  }
                });
                for (String jdk : jdks){
                  jreSrc = new Path(dir.toString()).append(jdk).append("src.zip");
                  if (jreSrc.toFile().exists()){
                    break;
                  }
                }
              }
            }
          }

          // jre src found.
          if(jreSrc.toFile().exists()){
            logger.info("Setting '{}' to '{}'",
                JavaRuntime.JRESRC_VARIABLE, jreSrc);
            newLocations[ii] = new LibraryLocation(
                locations[ii].getSystemLibraryPath(),
                jreSrc,
                locations[ii].getPackageRootPath(),
                locations[ii].getJavadocLocation());

          // jre src not found.
          }else{
            logger.debug(
                "Unable to locate jre src.zip for JAVA_HOME: " +
                SystemUtils.JAVA_HOME);
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
