/**
 * Copyright (c) 2004 - 2005
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
import java.io.InputStreamReader;
import java.io.StringWriter;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.eclim.util.VelocityFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

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

  private String variables = "classpath_variables";

  /**
   * Initialize the java env.
   */
  public void initialize ()
  {
    logger.info("Initializing java environment");
    // initialize variables.
    initializeJreSrc();
    String[] vars = JavaCore.getClasspathVariableNames();
    for(int ii = 0; ii < vars.length; ii++){
      logger.info("Variable {} = {}", vars[ii],
          JavaCore.getClasspathVariable(vars[ii]));
    }
    initializeVars(variables);

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
        String name = libraryPath.lastSegment();

        // eclipse didn't find src.zip, so search other known locations.
        if (name.equals("rt.jar") &&
            locations[ii].getSystemLibrarySourcePath().isEmpty())
        {
          IPath jreSrc = libraryPath.removeLastSegments(3)
            .append("share" + IPath.SEPARATOR + "src.zip");
          if(jreSrc.toFile().exists()){
            logger.debug("Setting '{}' to '{}'",
                JavaRuntime.JRESRC_VARIABLE, jreSrc);
          }
          newLocations[ii] = new LibraryLocation(
              locations[ii].getSystemLibraryPath(),
              jreSrc,
              locations[ii].getPackageRootPath(),
              locations[ii].getJavadocLocation());
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
    try{
      InputStreamReader reader = new InputStreamReader(
          getClass().getResourceAsStream(file));

      StringWriter writer = new StringWriter();

      Map values = new HashMap();
      for(Iterator ii = System.getProperties().keySet().iterator(); ii.hasNext();){
        String key = (String)ii.next();
        values.put(key.replace('.', '_'), System.getProperty(key));
      }
      VelocityFormat.evaluate(values, reader, writer);

      Properties properties = new Properties();
      properties.load(new ByteArrayInputStream(writer.toString().getBytes()));

      for(Iterator ii = properties.keySet().iterator(); ii.hasNext();){
        String name = (String)ii.next();
        IPath path = new Path(properties.getProperty(name));
        logger.debug("Setting classpath variable '{}' to path '{}'", name, path);
        JavaCore.setClasspathVariable(name, path, null);
      }
    }catch(Exception e){
      logger.error("", e);
    }
  }
}
