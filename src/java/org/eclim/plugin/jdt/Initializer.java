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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.SystemUtils;

import org.apache.log4j.Logger;

import org.eclim.util.StringUtils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Initializes the java env.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Initializer
{
  private static final Logger logger = Logger.getLogger(Initializer.class);

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
   * Initialize the java env.
   */
  public void initialize ()
  {
    logger.info("Initializing java environment");

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
  }

  /**
   * Shutdown the java env.
   */
  public void shutdown ()
    throws Exception
  {
    logger.info("Shutting down java environment");
    JavaCore.getJavaCore().stop(null);
  }

  /**
   * Performs additional logic to locate jre src zip file in alternate locations
   * not checked by eclipse.
   */
  protected void initializeJreSrc ()
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
             !locations[ii].getSystemLibrarySourcePath().toFile().exists()))
        {
          IPath jreSrc = null;

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
   * @param _variable The prefix of the property file.
   */
  protected void initializeVars (String _variable)
  {
    String file = "/" + _variable + ".properties";
    logger.info("Loading classpath variables from '{}'.", file);
    InputStream in = null;
    try{
      in = getClass().getResourceAsStream(file);
      StringWriter writer = new StringWriter();
      IOUtils.copy(in, writer);
      String propertiesString = writer.toString();

      Map values = new HashMap();
      for(Iterator ii = System.getProperties().keySet().iterator(); ii.hasNext();){
        String key = (String)ii.next();
        values.put(key, System.getProperty(key).replace('\\', '/'));
      }
      propertiesString =
        StringUtils.replacePlaceholders(propertiesString, values);

      Properties properties = new Properties();
      properties.load(new ByteArrayInputStream(propertiesString.getBytes()));

      for(Iterator ii = properties.keySet().iterator(); ii.hasNext();){
        String name = (String)ii.next();
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
