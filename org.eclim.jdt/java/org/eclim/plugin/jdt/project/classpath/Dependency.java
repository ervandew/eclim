/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.project.classpath;

import org.eclipse.core.runtime.IPath;

/**
 * Represents a dependency from a build file or other external source.
 *
 * @author Eric Van Dewoestine
 */
public class Dependency
{
  public static final String JAR = ".jar";
  public static final String SEPARATOR = "/";
  public static final String VERSION_SEPARATOR = "-";

  private String organization;
  private String name;
  private String version;
  private IPath path;
  private boolean variable;

  /**
   * Constructs a new instance.
   *
   * @param org The organization the dependency originates from.
   * @param name The name.
   * @param version The version.
   * @param path The root IPath where the dependecy is located.  Construtor
   * will call path.append() with the constructed dependency file name.
   */
  public Dependency(String org, String name, String version, IPath path)
  {
    this.organization = org;
    this.name = name;
    this.version = version;

    this.path = path.append(resolveArtifact());
  }

  /**
   * Gets the organization for this instance.
   *
   * @return The organization.
   */
  public String getOrganization()
  {
    return this.organization;
  }

  /**
   * Sets the organization for this instance.
   *
   * @param organization The organization.
   */
  public void setOrganization(String organization)
  {
    this.organization = organization;
  }

  /**
   * Get the name of the dependency.
   *
   * @return name as String.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Set name.
   *
   * @param name the value to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Get the version of the dependency.
   *
   * @return version as String.
   */
  public String getVersion()
  {
    return this.version;
  }

  /**
   * Set version.
   *
   * @param version the value to set.
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  /**
   * Gets the path for this instance.
   *
   * @return The path.
   */
  public IPath getPath()
  {
    return this.path;
  }

  /**
   * Sets the path for this instance.
   *
   * @param path The path.
   */
  public void setPath(IPath path)
  {
    this.path = path;
  }

  /**
   * Gets whether this dependency is relative to a classpath variable.
   *
   * @return true if relative to a variable, false otherwise.
   */
  public boolean isVariable()
  {
    return this.variable;
  }

  /**
   * Sets whether this dependency is relative to a classpath variable.
   *
   * @param variable true if relative to a variable, false otherwise.
   */
  public void setVariable(boolean variable)
  {
    this.variable = variable;
  }

  /**
   * Resolves the artifact to a path relative to the dependency's root.
   *
   * @return The resolved artifact path.
   */
  public String resolveArtifact()
  {
    return toString();
  }

  /**
   * Converts this dependency into a usable String.
   * <p>
   * Ex.<br>
   * For a dependency with the name 'commons-lang' and a version '1.0.2' this
   * method will return 'commons-lang-1.0.2.jar'.
   * </p>
   * Subclasses are free to override this method if necessary.
   *
   * @return The string representation.
   */
  public String toString()
  {
    StringBuffer buffer = new StringBuffer(getName());
    if(getVersion() != null && getVersion().trim().length() > 0){
      buffer.append(VERSION_SEPARATOR).append(getVersion());
    }
    buffer.append(JAR);
    return buffer.toString();
  }
}
