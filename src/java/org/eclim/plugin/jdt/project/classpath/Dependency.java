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
package org.eclim.plugin.jdt.project.classpath;

/**
 * Represents a dependecy from a build file or other external source.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Dependency
{
  public static final String JAR = ".jar";
  public static final String VERSION_SEPARATOR = "-";

  private String name;
  private String version;

  /**
   * Constructs a new instance.
   *
   * @param _name The name.
   * @param _version The version.
   */
  public Dependency (String _name, String _version)
  {
    this.name = _name;
    this.version = _version;
  }

  /**
   * Converts this dependency into a usable String.
   * <p/>
   * Ex.<br/>
   * For a dependency with the name 'commons-lang' and a version '1.0.2' this
   * method will return 'commons-lang-1.0.2.jar'.
   * <p/>
   * Subclasses are free to override this method if necessary.
   *
   * @return The string representation.
   */
  public String toString ()
  {
    StringBuffer buffer = new StringBuffer(getName());
    if(getVersion() != null && getVersion().trim().length() > 0){
      buffer.append(VERSION_SEPARATOR).append(getVersion());
    }
    buffer.append(JAR);
    return buffer.toString();
  }

  /**
   * Get the name of the dependency.
   *
   * @return name as String.
   */
  public String getName ()
  {
    return this.name;
  }

  /**
   * Set name.
   *
   * @param _name the value to set.
   */
  public void setName (String _name)
  {
    this.name = _name;
  }

  /**
   * Get the version of the dependency.
   *
   * @return version as String.
   */
  public String getVersion ()
  {
    return this.version;
  }

  /**
   * Set version.
   *
   * @param _version the value to set.
   */
  public void setVersion (String _version)
  {
    this.version = _version;
  }
}
