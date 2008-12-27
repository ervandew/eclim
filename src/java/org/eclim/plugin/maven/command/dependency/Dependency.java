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
package org.eclim.plugin.maven.command.dependency;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a maven dependency.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class Dependency
{
  // maven element names
  public static final String GROUP_ID = "groupId";
  public static final String ARTIFACT_ID = "artifactId";
  public static final String VERSION = "version";

  // ivy element attribute names
  public static final String ORG = "org";
  public static final String NAME = "name";
  public static final String REV = "rev";

  public static final String TYPE = "type";
  public static final String REPOSITORY = "repository";
  public static final String JAR = "jar";

  private String groupId;
  private String artifactId;
  private String version;
  private String type;
  private String repository;

  /**
   * Gets the groupId.
   *
   * @return The groupId.
   */
  public String getGroupId()
  {
    return groupId;
  }

  /**
   * Sets the groupId.
   *
   * @param groupId The groupId.
   */
  public void setGroupId(String groupId)
  {
    this.groupId = groupId;
  }

  /**
   * Gets the artifactId.
   *
   * @return The artifactId.
   */
  public String getArtifactId()
  {
    return artifactId;
  }

  /**
   * Sets the artifactId.
   *
   * @param artifactId The artifactId.
   */
  public void setArtifactId(String artifactId)
  {
    this.artifactId = artifactId;
  }

  /**
   * Gets the version.
   *
   * @return The version.
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * Sets the version.
   *
   * @param version The version.
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  /**
   * Gets the type.
   *
   * @return The type.
   */
  public String getType()
  {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type The type.
   */
  public void setType(String type)
  {
    this.type = type;
  }

  /**
   * Gets the repository.
   *
   * @return The repository.
   */
  public String getRepository()
  {
    return repository;
  }

  /**
   * Sets the repository.
   *
   * @param repository The repository.
   */
  public void setRepository(String repository)
  {
    this.repository = repository;
  }

  /**
   * {@inheritDoc}
   * @see Object#hashCode()
   */
  public int hashCode()
  {
    return new HashCodeBuilder()
      .append(groupId)
      .append(artifactId)
      .append(version)
      .append(type)
      .toHashCode();
  }

  /**
   * {@inheritDoc}
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj)
  {
    if (!(obj instanceof Dependency)){
      return false;
    }

    if (this == obj){
      return true;
    }

    Dependency other = (Dependency)obj;
    return new EqualsBuilder()
      .append(groupId, other.getGroupId())
      .append(artifactId, other.getArtifactId())
      .append(version, other.getVersion())
      .append(type, other.getType())
      .isEquals();
  }

  /**
   * {@inheritDoc}
   * @see Object#toString()
   */
  public String toString()
  {
    return new ToStringBuilder(this)
      .append(GROUP_ID, getGroupId())
      .append(ARTIFACT_ID, getArtifactId())
      .append(VERSION, getVersion())
      .append(TYPE, getType())
      .toString();
  }
}
